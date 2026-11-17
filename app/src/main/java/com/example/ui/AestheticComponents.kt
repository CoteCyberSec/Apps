package com.example.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.CoupleAestheticCatalog
import com.example.model.CoupleAestheticPreset
import com.example.model.UiStrings
import com.example.ui.theme.resolveThemePalette

@Composable
fun AestheticSwatchDots(
    aestheticId: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (scheme, _) = remember(aestheticId, isDark) {
        resolveThemePalette(aestheticId, isDark)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(scheme.primary)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(scheme.secondary)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(scheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
    }
}

fun describeCurrentAesthetic(aestheticId: String, language: AppLanguage): String {
    val customRgb = CoupleAestheticCatalog.parseCustomRgb(aestheticId)
    if (customRgb != null) {
        val hex = "#" + aestheticId.removePrefix("custom_").uppercase()
        return UiStrings.aestheticCustomName(hex, language)
    }
    return CoupleAestheticCatalog.findPreset(aestheticId).name.primary(language)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AestheticPickerDialog(
    currentAestheticId: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelectAesthetic: (String) -> Unit
) {
    val activeNormalizedId = currentAestheticId.ifBlank { CoupleAestheticCatalog.DEFAULT_ID }
    val initialArgb = CoupleAestheticCatalog.parseCustomRgb(activeNormalizedId)
        ?: CoupleAestheticCatalog.customQuickSwatches.first()
    val initialHue = remember(initialArgb) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(initialArgb, hsv)
        hsv[0]
    }
    var customHue by remember(initialArgb) { mutableFloatStateOf(initialHue) }
    val selectedCustomArgb = remember(customHue) {
        AndroidColor.HSVToColor(floatArrayOf(customHue.coerceIn(0f, 360f), 0.62f, 0.72f))
    }
    val customThemeId = remember(selectedCustomArgb) {
        CoupleAestheticCatalog.formatCustomThemeId(selectedCustomArgb)
    }
    val customHexLabel = remember(customThemeId) {
        "#" + customThemeId.removePrefix("custom_")
    }
    val isDark = isSystemInDarkTheme()
    val (customPreviewScheme, _) = remember(customThemeId, isDark) {
        resolveThemePalette(customThemeId, isDark)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = UiStrings.aestheticDialogTitle(language),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = UiStrings.aestheticDialogTitle(language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = UiStrings.aestheticSectionSubtitle(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = UiStrings.aestheticPresetsHeading(language),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CoupleAestheticCatalog.presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val selected = activeNormalizedId == preset.id
                                AestheticPresetTile(
                                    preset = preset,
                                    selected = selected,
                                    language = language,
                                    onClick = { onSelectAesthetic(preset.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPresets.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Text(
                    text = UiStrings.aestheticCustomHeading(language),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = customPreviewScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = if (activeNormalizedId.startsWith("custom_")) 2.dp else 1.dp,
                        color = if (activeNormalizedId.startsWith("custom_")) {
                            customPreviewScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AestheticSwatchDots(aestheticId = customThemeId)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = customPreviewScheme.primaryContainer
                            ) {
                                Text(
                                    text = customHexLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = customPreviewScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CoupleAestheticCatalog.customQuickSwatches.forEach { swatchArgb ->
                                val swatchHex = CoupleAestheticCatalog.formatCustomThemeId(swatchArgb)
                                val swatchHue = remember(swatchArgb) {
                                    val arr = FloatArray(3)
                                    AndroidColor.colorToHSV(swatchArgb, arr)
                                    arr[0]
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(swatchArgb))
                                        .border(
                                            width = 2.dp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                        .clickable { customHue = swatchHue }
                                        .testTag("custom_swatch_$swatchHex")
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFE55353),
                                            Color(0xFFE5B853),
                                            Color(0xFF53E573),
                                            Color(0xFF53C8E5),
                                            Color(0xFF6B53E5),
                                            Color(0xFFE553C2),
                                            Color(0xFFE55353)
                                        )
                                    )
                                )
                        )

                        Slider(
                            value = customHue,
                            onValueChange = { customHue = it },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = customPreviewScheme.primary,
                                activeTrackColor = customPreviewScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_hue_slider")
                        )

                        Button(
                            onClick = { onSelectAesthetic(customThemeId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = customPreviewScheme.primary,
                                contentColor = customPreviewScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("apply_custom_aesthetic_button")
                        ) {
                            Text(
                                text = UiStrings.aestheticApplyCustom(language),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(UiStrings.closeComposerAction(language))
            }
        }
    )
}

@Composable
private fun AestheticPresetTile(
    preset: CoupleAestheticPreset,
    selected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = Color(if (isDark) preset.darkSurface else preset.lightCardSurface)
    val primaryColor = Color(if (isDark) preset.darkPrimary else preset.lightPrimary)
    val secondaryColor = Color(if (isDark) preset.darkSecondary else preset.lightSecondary)
    val textColor = Color(if (isDark) preset.darkOnSurface else preset.lightOnSurface)
    val subColor = Color(if (isDark) preset.darkOnSurfaceVariant else preset.lightOnSurfaceVariant)
    val outlineColor = Color(if (isDark) preset.darkOutline else preset.lightOutline)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) primaryColor else outlineColor
        ),
        modifier = modifier.testTag("aesthetic_preset_${preset.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(secondaryColor)
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = preset.name.primary(language),
                            tint = Color(if (isDark) preset.darkOnPrimary else preset.lightOnPrimary),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            Text(
                text = preset.name.primary(language),
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preset.subtitle.primary(language),
                style = MaterialTheme.typography.labelSmall,
                color = subColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
