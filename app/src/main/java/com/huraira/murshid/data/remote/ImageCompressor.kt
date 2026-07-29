package com.huraira.murshid.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Turns a picked image [Uri] into two WebP byte arrays: a small thumbnail for grid/list
 * views and a larger "full" version capped at a sane wallpaper resolution — see the
 * "existing issue to fix" note in Prompt 2 about grids loading full-res images.
 */
object ImageCompressor {

    data class Variant(val bytes: ByteArray, val contentType: String = "image/webp")

    private const val THUMB_MAX_DIMENSION = 480
    private const val THUMB_TARGET_BYTES = 80 * 1024

    private const val FULL_MAX_DIMENSION = 1600
    private const val FULL_TARGET_BYTES = 600 * 1024

    suspend fun compressThumbAndFull(context: Context, uri: Uri): Result<Pair<Variant, Variant>> =
        withContext(Dispatchers.IO) {
            try {
                // Read the picked file into memory ONCE. Some devices/pickers (notably
                // MIUI) don't reliably support opening a second InputStream on the same
                // content:// URI — decoding from bytes we already hold sidesteps that
                // entirely instead of re-reading the URI for the bounds pass and the
                // full-decode pass separately.
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(
                        IllegalStateException("Couldn't read the selected image. Please try picking it again.")
                    )

                val original = decodeSampledBitmap(bytes, FULL_MAX_DIMENSION, FULL_MAX_DIMENSION)
                    ?: return@withContext Result.failure(
                        IllegalStateException("This image format isn't supported. Try a JPEG or PNG.")
                    )

                val full = resizeToMaxDimension(original, FULL_MAX_DIMENSION)
                val thumb = resizeToMaxDimension(original, THUMB_MAX_DIMENSION)

                val fullBytes = compressToTargetSize(full, FULL_TARGET_BYTES)
                val thumbBytes = compressToTargetSize(thumb, THUMB_TARGET_BYTES)

                if (full !== original) full.recycle()
                if (thumb !== original && thumb !== full) thumb.recycle()
                original.recycle()

                Result.success(Variant(thumbBytes) to Variant(fullBytes))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun decodeSampledBitmap(bytes: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

        boundsOptions.inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
        boundsOptions.inJustDecodeBounds = false

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun resizeToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longestEdge = max(bitmap.width, bitmap.height)
        if (longestEdge <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / longestEdge
        val newWidth = max(1, (bitmap.width * scale).toInt())
        val newHeight = max(1, (bitmap.height * scale).toInt())
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /** Compresses to WebP, stepping quality down until under [targetBytes] or the quality floor. */
    private fun compressToTargetSize(bitmap: Bitmap, targetBytes: Int, minQuality: Int = 40): ByteArray {
        var quality = 90
        var lastBytes: ByteArray = compress(bitmap, quality)
        while (lastBytes.size > targetBytes && quality > minQuality) {
            quality = max(minQuality, quality - 10)
            lastBytes = compress(bitmap, quality)
        }
        return lastBytes
    }

    private fun compress(bitmap: Bitmap, quality: Int): ByteArray {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality.coerceIn(0, 100), stream)
        return stream.toByteArray()
    }
}
