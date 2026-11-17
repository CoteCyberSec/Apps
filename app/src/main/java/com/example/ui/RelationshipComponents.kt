package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.CoupleSpace
import com.example.model.RelationshipTrackerHelper
import com.example.model.ReunionDateHelper
import com.example.model.UiStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelationshipTrackerCard(
    couple: CoupleSpace,
    language: AppLanguage,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = remember(couple.relationshipStartDateKey) {
        if (couple.relationshipStartDateKey.isBlank()) null
        else RelationshipTrackerHelper.calculateStats(couple.relationshipStartDateKey)
    }

    Surface(
        onClick = onEditClick,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("relationship_tracker_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.44f),
                            MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                )
                .padding(18.dp)
        ) {
            if (stats == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = UiStrings.relationshipEmptyTitle(language),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = UiStrings.relationshipEmptySubtitle(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = UiStrings.editLabel(language),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = UiStrings.relationshipEyebrow(language),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("edit_relationship_date_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = UiStrings.editLabel(language),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = UiStrings.relationshipDurationHeadline(
                            years = stats.years,
                            months = stats.months,
                            days = stats.days,
                            totalDays = stats.totalDays,
                            lang = language
                        ),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        ) {
                            Text(
                                text = UiStrings.relationshipTotalDays(stats.totalDays, language),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = UiStrings.relationshipStartedOn(
                                        ReunionDateHelper.formatDateDisplay(couple.relationshipStartDateKey, language),
                                        language
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (stats.isAnniversaryToday) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (stats.isAnniversaryToday) Icons.Rounded.Celebration
                                    else Icons.Rounded.Cake,
                                    contentDescription = null,
                                    tint = if (stats.isAnniversaryToday) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = UiStrings.nextAnniversaryTitle(
                                            number = stats.nextAnniversaryNumber,
                                            isToday = stats.isAnniversaryToday,
                                            lang = language
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = ReunionDateHelper.formatDateDisplay(stats.nextAnniversaryDateKey, language),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (stats.isAnniversaryToday) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (stats.isAnniversaryToday) {
                                        if (language == AppLanguage.RU) "Сегодня!" else "Today!"
                                    } else {
                                        UiStrings.reunionDaysLeft(stats.daysUntilNextAnniversary, language)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (stats.isAnniversaryToday) MaterialTheme.colorScheme.onTertiary
                                    else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipDateEditorDialog(
    initialDateKey: String,
    language: AppLanguage,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedDateKey by remember(initialDateKey) {
        mutableStateOf(initialDateKey.ifBlank { ReunionDateHelper.todayDateKey() })
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val previewStats = remember(selectedDateKey) {
        RelationshipTrackerHelper.calculateStats(selectedDateKey)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = UiStrings.relationshipDialogTitle(language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pick_relationship_date_button")
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${UiStrings.pickDateLabel(language)}: ${
                            ReunionDateHelper.formatDateDisplay(selectedDateKey, language)
                        }"
                    )
                }

                if (previewStats != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = UiStrings.relationshipTotalDays(previewStats.totalDays, language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${
                                    UiStrings.nextAnniversaryTitle(
                                        previewStats.nextAnniversaryNumber,
                                        previewStats.isAnniversaryToday,
                                        language
                                    )
                                } • ${
                                    ReunionDateHelper.formatDateDisplay(previewStats.nextAnniversaryDateKey, language)
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (initialDateKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onSave("") },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_relationship_date_button")
                    ) {
                        Text(UiStrings.clearDateAction(language))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDateKey) },
                enabled = selectedDateKey.isNotBlank() && !isBusy,
                modifier = Modifier.testTag("save_relationship_date_button")
            ) {
                Text(UiStrings.saveDateAction(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiStrings.cancelLabel(language))
            }
        }
    )

    if (showDatePicker) {
        val initialMillis = remember(selectedDateKey) {
            ReunionDateHelper.dateKeyToUtcMillis(selectedDateKey) ?: System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            selectedDateKey = ReunionDateHelper.utcMillisToDateKey(picked)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(UiStrings.saveProfile(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(UiStrings.cancelLabel(language))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
