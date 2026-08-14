package com.huraira.murshid.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.huraira.murshid.R

/**
 * Android's default font (and every font we'd otherwise bundle) has no Urdu glyphs, so
 * without this, any Urdu text in Library quotes, Update content, admin-typed text, etc.
 * falls back to whatever the *device's* system font happens to substitute — which varies
 * a lot by OEM (MIUI in particular has had known shaping glitches for specific Urdu
 * joining letters/diacritics). "Noto Nastaliq Urdu" is Google's purpose-built font for
 * correct Urdu script shaping, fetched on-device via Google Play Services' font provider
 * (cached after first use) instead of bundling a multi-MB font file in the APK.
 *
 * This is applied across [Typography] below rather than to specific screens: Android's
 * own font-fallback still kicks in per-character for anything this font doesn't cover
 * (i.e. Latin/English text keeps rendering normally), so it's safe to set broadly.
 */
@OptIn(ExperimentalTextApi::class)
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@OptIn(ExperimentalTextApi::class)
private val notoNastaliqUrdu = GoogleFont("Noto Nastaliq Urdu")

@OptIn(ExperimentalTextApi::class)
val MurshidFontFamily = FontFamily(
    Font(googleFont = notoNastaliqUrdu, fontProvider = googleFontProvider)
)
