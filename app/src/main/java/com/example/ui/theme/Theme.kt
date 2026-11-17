package com.example.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.CoupleAestheticCatalog
import com.example.model.CoupleAestheticPreset

@Immutable
data class DuetGoldColors(
    val accent: Color,
    val container: Color
)

val LocalDuetGoldColors = staticCompositionLocalOf {
    DuetGoldColors(
        accent = ChampagneGold,
        container = GoldContainer
    )
}

val DuetShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

internal fun resolveThemePalette(
    aestheticId: String,
    darkTheme: Boolean
): Pair<ColorScheme, DuetGoldColors> {
    val customRgb = CoupleAestheticCatalog.parseCustomRgb(aestheticId)
    if (customRgb != null) {
        return buildCustomPalette(customRgb, darkTheme)
    }
    val preset = CoupleAestheticCatalog.findPreset(aestheticId)
    return buildPresetPalette(preset, darkTheme)
}

private fun buildPresetPalette(
    preset: CoupleAestheticPreset,
    darkTheme: Boolean
): Pair<ColorScheme, DuetGoldColors> {
    return if (darkTheme) {
        val scheme = darkColorScheme(
            primary = Color(preset.darkPrimary),
            onPrimary = Color(preset.darkOnPrimary),
            primaryContainer = Color(preset.darkPrimaryContainer),
            onPrimaryContainer = Color(preset.darkOnSurface),
            secondary = Color(preset.darkSecondary),
            onSecondary = Color(preset.darkOnPrimary),
            secondaryContainer = Color(preset.darkSecondaryContainer),
            onSecondaryContainer = Color(preset.darkOnSurface),
            tertiary = Color(preset.darkTertiary),
            onTertiary = Color(preset.darkOnPrimary),
            tertiaryContainer = Color(preset.darkTertiaryContainer),
            onTertiaryContainer = Color(preset.darkOnSurface),
            background = Color(preset.darkBackground),
            onBackground = Color(preset.darkOnSurface),
            surface = Color(preset.darkSurface),
            onSurface = Color(preset.darkOnSurface),
            surfaceVariant = Color(preset.darkSurfaceVariant),
            onSurfaceVariant = Color(preset.darkOnSurfaceVariant),
            surfaceContainerLow = Color(preset.darkSurface),
            surfaceContainer = Color(preset.darkSurfaceVariant),
            outline = Color(preset.darkOutline)
        )
        val gold = DuetGoldColors(
            accent = Color(preset.darkGoldAccent),
            container = Color(preset.darkGoldContainer)
        )
        scheme to gold
    } else {
        val scheme = lightColorScheme(
            primary = Color(preset.lightPrimary),
            onPrimary = Color(preset.lightOnPrimary),
            primaryContainer = Color(preset.lightPrimaryContainer),
            onPrimaryContainer = Color(preset.lightOnSurface),
            secondary = Color(preset.lightSecondary),
            onSecondary = Color(preset.lightOnPrimary),
            secondaryContainer = Color(preset.lightSecondaryContainer),
            onSecondaryContainer = Color(preset.lightOnSurface),
            tertiary = Color(preset.lightTertiary),
            onTertiary = Color(preset.lightOnPrimary),
            tertiaryContainer = Color(preset.lightTertiaryContainer),
            onTertiaryContainer = Color(preset.lightOnSurface),
            background = Color(preset.lightBackground),
            onBackground = Color(preset.lightOnSurface),
            surface = Color(preset.lightSurface),
            onSurface = Color(preset.lightOnSurface),
            surfaceVariant = Color(preset.lightSurfaceVariant),
            onSurfaceVariant = Color(preset.lightOnSurfaceVariant),
            surfaceContainerLow = Color(preset.lightCardSurface),
            surfaceContainer = Color(preset.lightSurfaceVariant),
            outline = Color(preset.lightOutline)
        )
        val gold = DuetGoldColors(
            accent = Color(preset.lightGoldAccent),
            container = Color(preset.lightGoldContainer)
        )
        scheme to gold
    }
}

private fun hsvColor(hue: Float, sat: Float, value: Float): Color {
    val wrappedHue = ((hue % 360f) + 360f) % 360f
    return Color(
        AndroidColor.HSVToColor(
            floatArrayOf(
                wrappedHue,
                sat.coerceIn(0f, 1f),
                value.coerceIn(0f, 1f)
            )
        )
    )
}

private fun buildCustomPalette(
    seedArgb: Int,
    darkTheme: Boolean
): Pair<ColorScheme, DuetGoldColors> {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(seedArgb, hsv)
    val baseHue = hsv[0]
    val secondaryHue = (baseHue + 34f) % 360f
    val tertiaryHue = (baseHue + 145f) % 360f
    val goldHue = (baseHue + 28f) % 360f

    return if (darkTheme) {
        val onDarkText = hsvColor(baseHue, 0.08f, 0.95f)
        val onDarkPrimary = hsvColor(baseHue, 0.45f, 0.12f)
        val scheme = darkColorScheme(
            primary = hsvColor(baseHue, 0.36f, 0.92f),
            onPrimary = onDarkPrimary,
            primaryContainer = hsvColor(baseHue, 0.56f, 0.36f),
            onPrimaryContainer = onDarkText,
            secondary = hsvColor(secondaryHue, 0.38f, 0.90f),
            onSecondary = onDarkPrimary,
            secondaryContainer = hsvColor(secondaryHue, 0.52f, 0.34f),
            onSecondaryContainer = onDarkText,
            tertiary = hsvColor(tertiaryHue, 0.34f, 0.84f),
            onTertiary = onDarkPrimary,
            tertiaryContainer = hsvColor(tertiaryHue, 0.45f, 0.28f),
            onTertiaryContainer = onDarkText,
            background = hsvColor(baseHue, 0.30f, 0.10f),
            onBackground = onDarkText,
            surface = hsvColor(baseHue, 0.28f, 0.14f),
            onSurface = onDarkText,
            surfaceVariant = hsvColor(baseHue, 0.26f, 0.19f),
            onSurfaceVariant = hsvColor(baseHue, 0.16f, 0.80f),
            surfaceContainerLow = hsvColor(baseHue, 0.28f, 0.14f),
            surfaceContainer = hsvColor(baseHue, 0.26f, 0.19f),
            outline = hsvColor(baseHue, 0.25f, 0.30f)
        )
        val gold = DuetGoldColors(
            accent = hsvColor(goldHue, 0.40f, 0.89f),
            container = hsvColor(goldHue, 0.50f, 0.30f)
        )
        scheme to gold
    } else {
        val onLightText = hsvColor(baseHue, 0.38f, 0.16f)
        val onPrimaryLight = hsvColor(baseHue, 0.06f, 0.99f)
        val scheme = lightColorScheme(
            primary = hsvColor(baseHue, 0.62f, 0.54f),
            onPrimary = onPrimaryLight,
            primaryContainer = hsvColor(baseHue, 0.22f, 0.94f),
            onPrimaryContainer = onLightText,
            secondary = hsvColor(secondaryHue, 0.56f, 0.68f),
            onSecondary = onPrimaryLight,
            secondaryContainer = hsvColor(secondaryHue, 0.20f, 0.95f),
            onSecondaryContainer = onLightText,
            tertiary = hsvColor(tertiaryHue, 0.42f, 0.48f),
            onTertiary = onPrimaryLight,
            tertiaryContainer = hsvColor(tertiaryHue, 0.18f, 0.92f),
            onTertiaryContainer = onLightText,
            background = hsvColor(baseHue, 0.06f, 0.98f),
            onBackground = onLightText,
            surface = hsvColor(baseHue, 0.06f, 0.98f),
            onSurface = onLightText,
            surfaceVariant = hsvColor(baseHue, 0.09f, 0.94f),
            onSurfaceVariant = hsvColor(baseHue, 0.24f, 0.42f),
            surfaceContainerLow = hsvColor(baseHue, 0.07f, 0.96f),
            surfaceContainer = hsvColor(baseHue, 0.09f, 0.94f),
            outline = hsvColor(baseHue, 0.14f, 0.85f)
        )
        val gold = DuetGoldColors(
            accent = hsvColor(goldHue, 0.55f, 0.68f),
            container = hsvColor(goldHue, 0.18f, 0.95f)
        )
        scheme to gold
    }
}

@Composable
fun DuetTheme(
    aestheticId: String = CoupleAestheticCatalog.DEFAULT_ID,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val (colorScheme, goldColors) = remember(aestheticId, darkTheme) {
        resolveThemePalette(aestheticId, darkTheme)
    }
    CompositionLocalProvider(LocalDuetGoldColors provides goldColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DuetTypography,
            shapes = DuetShapes,
            content = content
        )
    }
}
