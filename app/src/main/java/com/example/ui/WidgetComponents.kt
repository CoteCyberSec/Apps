package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.CoupleSpace
import com.example.model.UiStrings
import com.example.model.WidgetCoupleSnapshot
import com.example.model.WidgetSummaryFormatter

@Composable
fun CoupleWidgetPreviewCard(
    couple: CoupleSpace,
    language: AppLanguage,
    onAddWidgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val display = remember(
        couple.partnerOneName,
        couple.partnerTwoName,
        couple.relationshipStartDateKey,
        couple.nextMeetDateKey,
        couple.nextMeetLocation,
        couple.aestheticThemeId,
        language
    ) {
        WidgetSummaryFormatter.buildDisplayData(
            WidgetCoupleSnapshot(
                partnerOneName = couple.partnerOneName,
                partnerTwoName = couple.partnerTwoName,
                relationshipStartDateKey = couple.relationshipStartDateKey,
                nextMeetDateKey = couple.nextMeetDateKey,
                nextMeetLocation = couple.nextMeetLocation,
                aestheticThemeId = couple.aestheticThemeId,
                language = language
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .testTag("couple_widget_section")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Widgets,
                    contentDescription = UiStrings.widgetSectionTitle(language),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = UiStrings.widgetSectionTitle(language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = UiStrings.widgetSectionSubtitle(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Live miniature widget preview
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = display.headerTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetPreviewTile(
                            icon = Icons.Rounded.Favorite,
                            accent = MaterialTheme.colorScheme.primary,
                            container = MaterialTheme.colorScheme.primaryContainer,
                            eyebrow = display.togetherEyebrow,
                            value = display.togetherValue,
                            unit = display.togetherUnit,
                            subtitle = display.togetherSubtitle,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        WidgetPreviewTile(
                            icon = Icons.Rounded.FlightTakeoff,
                            accent = MaterialTheme.colorScheme.tertiary,
                            container = MaterialTheme.colorScheme.tertiaryContainer,
                            eyebrow = display.reunionEyebrow,
                            value = display.reunionValue,
                            unit = display.reunionUnit,
                            subtitle = display.reunionSubtitle,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick = onAddWidgetClick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_widget_to_home_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.AddToHomeScreen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(UiStrings.addWidgetToHomeButton(language))
            }
        }
    }
}

@Composable
private fun WidgetPreviewTile(
    icon: ImageVector,
    accent: Color,
    container: Color,
    eyebrow: String,
    value: String,
    unit: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = container,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ManualWidgetPinDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Widgets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = UiStrings.widgetPinManualTitle(language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = UiStrings.widgetPinManualBody(language),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(UiStrings.gotItAction(language))
            }
        }
    )
}
