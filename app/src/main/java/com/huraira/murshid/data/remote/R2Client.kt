package com.huraira.murshid.data.remote

import com.huraira.murshid.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Minimal Cloudflare R2 (S3-compatible) client. Signs requests with AWS Signature V4 by
 * hand instead of pulling in the full AWS Android SDK — R2 uploads are just "PUT a file,
 * get a URL back", which doesn't need a multi-service SDK dragging down app size.
 *
 * Credentials come from [BuildConfig], which itself is populated from local.properties /
 * environment variables at build time (see app/build.gradle.kts) — never hardcoded here.
 */
object R2Client {

    private const val REGION = "auto"
    private const val SERVICE = "s3"
    private const val EMPTY_PAYLOAD_HASH =
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

    private val httpClient = OkHttpClient.Builder().build()

    private val host: String
        get() = BuildConfig.R2_ENDPOINT.removePrefix("https://").removePrefix("http://").trimEnd('/')

    /** Uploads [bytes] to `key` in the configured bucket and returns the public URL to store in Firestore. */
    suspend fun upload(key: String, bytes: ByteArray, contentType: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val path = "/${BuildConfig.R2_BUCKET_NAME}/${key.trimStart('/')}"
                val amzDate = amzDate()
                val dateStamp = amzDate.substring(0, 8)
                val bodyHashHex = sha256Hex(bytes)

                val headers = linkedMapOf(
                    "host" to host,
                    "x-amz-content-sha256" to bodyHashHex,
                    "x-amz-date" to amzDate
                )
                val authHeader = sign(
                    method = "PUT",
                    path = path,
                    queryString = "",
                    headers = headers,
                    payloadHashHex = bodyHashHex,
                    amzDate = amzDate,
                    dateStamp = dateStamp
                )

                val request = Request.Builder()
                    .url("https://$host$path")
                    .put(bytes.toRequestBody(contentType.toMediaType()))
                    .header("Host", host)
                    .header("x-amz-content-sha256", bodyHashHex)
                    .header("x-amz-date", amzDate)
                    .header("Authorization", authHeader)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("R2 upload failed: HTTP ${response.code} ${response.message}")
                        )
                    }
                }
                Result.success("${BuildConfig.R2_PUBLIC_BASE_URL.trimEnd('/')}/${key.trimStart('/')}")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Deletes `key` from the bucket. Safe to call even if the object no longer exists. */
    suspend fun delete(key: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val path = "/${BuildConfig.R2_BUCKET_NAME}/${key.trimStart('/')}"
            val amzDate = amzDate()
            val dateStamp = amzDate.substring(0, 8)

            val headers = linkedMapOf(
                "host" to host,
                "x-amz-content-sha256" to EMPTY_PAYLOAD_HASH,
                "x-amz-date" to amzDate
            )
            val authHeader = sign(
                method = "DELETE",
                path = path,
                queryString = "",
                headers = headers,
                payloadHashHex = EMPTY_PAYLOAD_HASH,
                amzDate = amzDate,
                dateStamp = dateStamp
            )

            val request = Request.Builder()
                .url("https://$host$path")
                .delete()
                .header("Host", host)
                .header("x-amz-content-sha256", EMPTY_PAYLOAD_HASH)
                .header("x-amz-date", amzDate)
                .header("Authorization", authHeader)
                .build()

            httpClient.newCall(request).execute().use { response ->
                // R2 returns 204 whether or not the object existed — both are "fine".
                if (!response.isSuccessful && response.code != 404) {
                    return@withContext Result.failure(
                        IllegalStateException("R2 delete failed: HTTP ${response.code} ${response.message}")
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Converts a stored public URL (what's saved in Firestore, e.g.
     * "https://pub-xxx.r2.dev/wallpapers/thumb/<uuid>.webp") back into the object key
     * needed for [delete] ("wallpapers/thumb/<uuid>.webp"). Returns null if the URL
     * doesn't start with the configured public base URL (defensive — shouldn't happen
     * for anything this app itself uploaded).
     */
    fun keyFromPublicUrl(url: String): String? {
        val base = BuildConfig.R2_PUBLIC_BASE_URL.trimEnd('/') + "/"
        return if (url.startsWith(base)) url.removePrefix(base) else null
    }

    // ---- AWS Signature V4 ----

    private fun sign(
        method: String,
        path: String,
        queryString: String,
        headers: Map<String, String>,
        payloadHashHex: String,
        amzDate: String,
        dateStamp: String
    ): String {
        val sortedHeaderKeys = headers.keys.sorted()
        val canonicalHeaders = sortedHeaderKeys.joinToString("") { "$it:${headers.getValue(it)}\n" }
        val signedHeaders = sortedHeaderKeys.joinToString(";")

        val canonicalRequest = listOf(
            method,
            uriEncodePath(path),
            queryString,
            canonicalHeaders,
            signedHeaders,
            payloadHashHex
        ).joinToString("\n")

        val credentialScope = "$dateStamp/$REGION/$SERVICE/aws4_request"
        val stringToSign = listOf(
            "AWS4-HMAC-SHA256",
            amzDate,
            credentialScope,
            sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8))
        ).joinToString("\n")

        val signingKey = signingKey(dateStamp)
        val signature = hmacHex(signingKey, stringToSign)

        return "AWS4-HMAC-SHA256 Credential=${BuildConfig.R2_ACCESS_KEY_ID}/$credentialScope, " +
            "SignedHeaders=$signedHeaders, Signature=$signature"
    }

    private fun signingKey(dateStamp: String): ByteArray {
        val kSecret = ("AWS4" + BuildConfig.R2_SECRET_ACCESS_KEY).toByteArray(Charsets.UTF_8)
        val kDate = hmac(kSecret, dateStamp)
        val kRegion = hmac(kDate, REGION)
        val kService = hmac(kRegion, SERVICE)
        return hmac(kService, "aws4_request")
    }

    private fun hmac(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hmacHex(key: ByteArray, data: String): String =
        hmac(key, data).joinToString("") { "%02x".format(it) }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    /**
     * AWS requires strict RFC 3986 percent-encoding (unreserved: A-Za-z0-9-_.~, everything
     * else encoded, uppercase hex). Java's URLEncoder is close but not exact — e.g. it
     * leaves "*" unencoded and encodes "~" — which AWS disagrees with. This never bites us
     * in practice because every object key here is generated from UUID.randomUUID() plus
     * fixed ASCII folder names (see WallpaperRepository/LibraryRepository/UpdatesRepository),
     * never from raw admin-typed text. If that ever changes, revisit this function first.
     */
    private fun uriEncodePath(path: String): String =
        path.split("/").joinToString("/") {
            if (it.isEmpty()) it else URLEncoder.encode(it, "UTF-8").replace("+", "%20")
        }

    private fun amzDate(): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
