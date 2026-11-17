package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.LocationOn
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.LocationPhotoLookup
import com.example.model.AppLanguage
import com.example.model.CoupleSpace
import com.example.model.ReunionDateHelper
import com.example.model.UiStrings

@Composable
fun ReunionCountdownCard(
    couple: CoupleSpace,
    language: AppLanguage,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasDate = couple.nextMeetDateKey.isNotBlank()
    val daysLeft = remember(couple.nextMeetDateKey) {
        if (hasDate) ReunionDateHelper.daysUntil(couple.nextMeetDateKey) else null
    }
    var locationPhotoUrl by remember(couple.nextMeetLocation) { mutableStateOf<String?>(null) }
    var imageLoaded by remember(couple.nextMeetLocation) { mutableStateOf(false) }

    LaunchedEffect(couple.nextMeetLocation) {
        imageLoaded = false
        val loc = couple.nextMeetLocation.trim()
        locationPhotoUrl = if (loc.isNotBlank()) {
            LocationPhotoLookup.resolvePhotoUrl(loc)
        } else {
            null
        }
    }

    val hasPhoto = !locationPhotoUrl.isNullOrBlank() && imageLoaded

    Surface(
        onClick = onEditClick,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            if (hasPhoto) Color.White.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("reunion_countdown_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                )
        ) {
            if (!locationPhotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(locationPhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = couple.nextMeetLocation,
                    contentScale = ContentScale.Crop,
                    onSuccess = { imageLoaded = true },
                    onError = { imageLoaded = false },
                    modifier = Modifier
                        .matchParentSize()
                        .testTag("reunion_location_background_image")
                )

                if (imageLoaded) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1A0F13).copy(alpha = 0.56f),
                                        Color(0xFF261419).copy(alpha = 0.74f)
                                    )
                                )
                            )
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                if (!hasDate || daysLeft == null) {
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
                                    .background(
                                        if (hasPhoto) Color.White.copy(alpha = 0.20f)
                                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FlightTakeoff,
                                    contentDescription = null,
                                    tint = if (hasPhoto) Color.White else MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = UiStrings.reunionEmptyTitle(language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (hasPhoto) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = UiStrings.reunionEmptySubtitle(language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasPhoto) Color.White.copy(alpha = 0.88f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = UiStrings.editLabel(language),
                            tint = if (hasPhoto) Color.White else MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    imageVector = Icons.Rounded.FlightTakeoff,
                                    contentDescription = null,
                                    tint = if (hasPhoto) Color(0xFFFFD6BA) else MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = UiStrings.reunionTitle(language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (hasPhoto) Color(0xFFFFD6BA) else MaterialTheme.colorScheme.tertiary
                                )
                            }

                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("edit_reunion_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = UiStrings.editLabel(language),
                                    tint = if (hasPhoto) Color.White.copy(alpha = 0.92f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val headline = when {
                            daysLeft > 0 -> UiStrings.reunionDaysLeft(daysLeft, language)
                            daysLeft == 0 -> UiStrings.reunionToday(language)
                            else -> UiStrings.reunionPassed(language)
                        }

                        Text(
                            text = headline,
                            style = if (daysLeft >= 0) MaterialTheme.typography.displaySmall
                            else MaterialTheme.typography.titleLarge,
                            color = if (hasPhoto) Color.White else MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (hasPhoto) MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = ReunionDateHelper.formatDateDisplay(couple.nextMeetDateKey, language),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (couple.nextMeetLocation.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (hasPhoto) MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = couple.nextMeetLocation,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
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
fun ReunionEditorDialog(
    initialDateKey: String,
    initialLocation: String,
    language: AppLanguage,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (dateKey: String, location: String) -> Unit
) {
    var selectedDateKey by remember {
        mutableStateOf(initialDateKey.ifBlank { ReunionDateHelper.todayDateKey() })
    }
    var location by remember { mutableStateOf(initialLocation) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.FlightTakeoff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        title = {
            Text(
                text = UiStrings.reunionSectionTitle(language),
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
                        .testTag("pick_reunion_date_button")
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${UiStrings.pickDateLabel(language)}: ${
                            ReunionDateHelper.formatDateDisplay(selectedDateKey, language)
                        }"
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { if (it.length <= 100) location = it },
                    label = { Text(UiStrings.reunionLocationHint(language)) },
                    leadingIcon = {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reunion_location_input")
                )

                if (initialDateKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onSave("", "") },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(UiStrings.clearReunion(language))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDateKey, location) },
                enabled = selectedDateKey.isNotBlank() && !isBusy,
                modifier = Modifier.testTag("save_reunion_button")
            ) {
                Text(UiStrings.saveReunion(language))
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
