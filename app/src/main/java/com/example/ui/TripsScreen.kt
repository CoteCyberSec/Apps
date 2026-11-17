package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PhotoMetadataHelper
import com.example.model.AppLanguage
import com.example.model.ReunionDateHelper
import com.example.model.TripMemory
import com.example.model.TripPhoto
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState

private enum class TripsTabMode {
    TRIPS, BY_DATE
}

@Composable
fun TripsScreen(
    state: DuetUiState.Ready,
    language: AppLanguage,
    isBusy: Boolean,
    onOpenTrip: (tripId: String) -> Unit,
    onSaveTrip: (
        title: String,
        location: String,
        startDateKey: String,
        endDateKey: String,
        notes: String,
        existingTrip: TripMemory?,
        onCreatedTripId: (String) -> Unit
    ) -> Unit,
    onUpdatePhoto: (
        photoId: String,
        caption: String,
        takenDateKey: String,
        takenTimeLabel: String,
        takenMillis: Long,
        timestampSource: String
    ) -> Unit,
    onReactToPhoto: (photoId: String, reaction: String) -> Unit,
    onDeletePhoto: (photoId: String) -> Unit,
    onDeletePhotos: (photoIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(TripsTabMode.TRIPS) }
    var showCreateTripDialog by remember { mutableStateOf(false) }
    var inspectingPhoto by remember { mutableStateOf<TripPhoto?>(null) }
    var selectedPhotoIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showConfirmDeleteSelected by remember { mutableStateOf(false) }

    LaunchedEffect(state.tripPhotos) {
        if (selectedPhotoIds.isNotEmpty()) {
            val validIds = state.tripPhotos.mapTo(HashSet()) { it.id }
            selectedPhotoIds = selectedPhotoIds.intersect(validIds)
        }
    }

    BackHandler(enabled = mode == TripsTabMode.BY_DATE && selectedPhotoIds.isNotEmpty()) {
        selectedPhotoIds = emptySet()
    }

    val photosByTrip = remember(state.tripPhotos) {
        state.tripPhotos.groupBy { it.tripId }
    }
    val tripTitleLookup = remember(state.trips) {
        state.trips.associate { it.id to it.title }
    }
    val photosGroupedByDate = remember(state.tripPhotos) {
        state.tripPhotos
            .sortedWith(compareByDescending<TripPhoto> { it.takenDateKey }.thenBy { it.takenMillis })
            .groupBy { it.takenDateKey }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlightLand,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = UiStrings.tripsTitle(language),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = UiStrings.tripsSubtitle(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = { showCreateTripDialog = true },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("add_trip_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(UiStrings.addTripButton(language), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = mode == TripsTabMode.TRIPS,
                    onClick = {
                        mode = TripsTabMode.TRIPS
                        selectedPhotoIds = emptySet()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("trips_mode_trips")
                ) {
                    Text(UiStrings.tripsViewLabel(language))
                }
                SegmentedButton(
                    selected = mode == TripsTabMode.BY_DATE,
                    onClick = { mode = TripsTabMode.BY_DATE },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("trips_mode_by_date")
                ) {
                    Text(UiStrings.allPhotosByDateLabel(language))
                }
            }
        }

        if (mode == TripsTabMode.TRIPS) {
            if (state.trips.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = UiStrings.noTripsYet(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showCreateTripDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("empty_add_trip_button")
                            ) {
                                Text(UiStrings.addTripButton(language))
                            }
                        }
                    }
                }
            } else {
                items(state.trips, key = { it.id }) { trip ->
                    val photos = photosByTrip[trip.id].orEmpty()
                    TripSummaryCard(
                        trip = trip,
                        photos = photos,
                        language = language,
                        onClick = { onOpenTrip(trip.id) }
                    )
                }
            }
        } else {
            if (photosGroupedByDate.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = UiStrings.noTripPhotosYet(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                if (selectedPhotoIds.isNotEmpty()) {
                    item(key = "photo_selection_bar") {
                        PhotoSelectionActionBar(
                            selectedCount = selectedPhotoIds.size,
                            totalCount = state.tripPhotos.size,
                            language = language,
                            isBusy = isBusy,
                            onClear = { selectedPhotoIds = emptySet() },
                            onSelectAll = {
                                selectedPhotoIds = if (selectedPhotoIds.size == state.tripPhotos.size) {
                                    emptySet()
                                } else {
                                    state.tripPhotos.mapTo(HashSet()) { it.id }
                                }
                            },
                            onDeleteClick = { showConfirmDeleteSelected = true }
                        )
                    }
                }
                photosGroupedByDate.forEach { (dateKey, dayPhotos) ->
                    item(key = "header_$dateKey") {
                        DateSectionHeader(
                            dateKey = dateKey,
                            photoCount = dayPhotos.size,
                            language = language
                        )
                    }
                    items(dayPhotos.chunked(2), key = { row -> "row_${dateKey}_${row.first().id}" }) { rowPhotos ->
                        val isSelectionMode = selectedPhotoIds.isNotEmpty()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPhotos.forEach { photo ->
                                val isSelected = photo.id in selectedPhotoIds
                                TripPhotoTile(
                                    photo = photo,
                                    tripNameBadge = tripTitleLookup[photo.tripId],
                                    language = language,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedPhotoIds = if (isSelected) {
                                                selectedPhotoIds - photo.id
                                            } else {
                                                selectedPhotoIds + photo.id
                                            }
                                        } else {
                                            inspectingPhoto = photo
                                        }
                                    },
                                    onLongClick = {
                                        selectedPhotoIds = selectedPhotoIds + photo.id
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPhotos.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showCreateTripDialog) {
        TripEditorDialog(
            existingTrip = null,
            language = language,
            isBusy = isBusy,
            onDismiss = { showCreateTripDialog = false },
            onSave = { title, location, startKey, endKey, notes ->
                onSaveTrip(title, location, startKey, endKey, notes, null) { newTripId ->
                    showCreateTripDialog = false
                    onOpenTrip(newTripId)
                }
            }
        )
    }

    val activePhotoRaw = inspectingPhoto
    if (activePhotoRaw != null) {
        val activePhoto = state.tripPhotos.firstOrNull { it.id == activePhotoRaw.id } ?: activePhotoRaw
        TripPhotoDetailDialog(
            photo = activePhoto,
            tripTitle = tripTitleLookup[activePhoto.tripId],
            language = language,
            isBusy = isBusy,
            onDismiss = { inspectingPhoto = null },
            onSave = { caption, dateKey, timeLabel, millis, source ->
                onUpdatePhoto(activePhoto.id, caption, dateKey, timeLabel, millis, source)
                inspectingPhoto = null
            },
            onReact = { emoji -> onReactToPhoto(activePhoto.id, emoji) },
            onDelete = {
                onDeletePhoto(activePhoto.id)
                inspectingPhoto = null
            }
        )
    }

    if (showConfirmDeleteSelected && selectedPhotoIds.isNotEmpty()) {
        DeleteSelectedPhotosDialog(
            count = selectedPhotoIds.size,
            language = language,
            isBusy = isBusy,
            onDismiss = { showConfirmDeleteSelected = false },
            onConfirm = {
                onDeletePhotos(selectedPhotoIds.toList())
                selectedPhotoIds = emptySet()
                showConfirmDeleteSelected = false
            }
        )
    }
}

@Composable
private fun TripSummaryCard(
    trip: TripMemory,
    photos: List<TripPhoto>,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val dateLabel = remember(trip.startDateKey, trip.endDateKey, language) {
        when {
            trip.startDateKey.isNotBlank() && trip.endDateKey.isNotBlank() && trip.startDateKey != trip.endDateKey ->
                "${ReunionDateHelper.formatDateDisplay(trip.startDateKey, language)} – ${
                    ReunionDateHelper.formatDateDisplay(trip.endDateKey, language)
                }"
            trip.startDateKey.isNotBlank() -> ReunionDateHelper.formatDateDisplay(trip.startDateKey, language)
            else -> ""
        }
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_card_${trip.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (trip.location.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = trip.location,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (dateLabel.isNotBlank()) {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = UiStrings.photosCount(photos.size, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (trip.notes.isNotBlank()) {
                Text(
                    text = trip.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos.take(6), key = { it.id }) { photo ->
                        TripPhotoImage(
                            photo = photo,
                            contentDescription = photo.caption.ifBlank { trip.title },
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateSectionHeader(
    dateKey: String,
    photoCount: Int,
    language: AppLanguage
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = ReunionDateHelper.formatDateDisplay(dateKey, language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = UiStrings.photosCount(photoCount, language),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripPhotoTile(
    photo: TripPhoto,
    tripNameBadge: String?,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val tileShape = RoundedCornerShape(18.dp)

    Surface(
        shape = tileShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (isSelected) {
            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        },
        modifier = modifier
            .clip(tileShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .testTag("trip_photo_tile_${photo.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                TripPhotoImage(
                    photo = photo,
                    contentDescription = photo.caption.ifBlank { photo.takenDateKey },
                    modifier = Modifier.fillMaxSize()
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                    )
                }
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = buildString {
                            if (photo.takenTimeLabel.isNotBlank()) append(photo.takenTimeLabel)
                            else append(photo.takenDateKey)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (photo.reaction.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = photo.reaction,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (photo.caption.isNotBlank() || !tripNameBadge.isNullOrBlank()) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    if (!tripNameBadge.isNullOrBlank()) {
                        Text(
                            text = tripNameBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (photo.caption.isNotBlank()) {
                        Text(
                            text = photo.caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoSelectionActionBar(
    selectedCount: Int,
    totalCount: Int,
    language: AppLanguage,
    isBusy: Boolean,
    onClear: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("photo_selection_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.testTag("clear_photo_selection_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = UiStrings.cancelLabel(language),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = UiStrings.selectedPhotosCount(selectedCount, language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (totalCount > 1) {
                TextButton(
                    onClick = onSelectAll,
                    modifier = Modifier.testTag("select_all_photos_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(UiStrings.selectAllAction(language))
                }
            }
            IconButton(
                onClick = onDeleteClick,
                enabled = !isBusy && selectedCount > 0,
                modifier = Modifier.testTag("delete_selected_photos_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = UiStrings.deleteLabel(language),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DeleteSelectedPhotosDialog(
    count: Int,
    language: AppLanguage,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = UiStrings.deleteSelectedPhotosTitle(count, language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = UiStrings.deleteSelectedPhotosBody(language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_batch_delete_photos_button")
            ) {
                Text(UiStrings.deleteLabel(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiStrings.cancelLabel(language))
            }
        }
    )
}

@Composable
fun TripPhotoImage(
    photo: TripPhoto,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val modelData = remember(photo.id, photo.thumbnailBase64) {
        PhotoMetadataHelper.getLocalPhotoFileIfExists(context, photo.id)
            ?: PhotoMetadataHelper.decodeBase64ToBytes(photo.thumbnailBase64)
    }

    if (modelData != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(modelData)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditorDialog(
    existingTrip: TripMemory?,
    language: AppLanguage,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, location: String, startDateKey: String, endDateKey: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(existingTrip?.title ?: "") }
    var location by remember { mutableStateOf(existingTrip?.location ?: "") }
    var startDateKey by remember {
        mutableStateOf(existingTrip?.startDateKey?.ifBlank { ReunionDateHelper.todayDateKey() } ?: ReunionDateHelper.todayDateKey())
    }
    var endDateKey by remember { mutableStateOf(existingTrip?.endDateKey ?: "") }
    var notes by remember { mutableStateOf(existingTrip?.notes ?: "") }
    var pickingStartDate by remember { mutableStateOf(false) }
    var pickingEndDate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingTrip != null) UiStrings.editTripButton(language)
                else UiStrings.addTripButton(language),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 100) title = it },
                    label = { Text(UiStrings.tripTitleLabel(language)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_title_input")
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { if (it.length <= 100) location = it },
                    label = { Text(UiStrings.tripLocationLabel(language)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_location_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { pickingStartDate = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${UiStrings.tripStartDateLabel(language)}: ${
                                ReunionDateHelper.formatDateDisplay(startDateKey, language)
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    FilledTonalButton(
                        onClick = { pickingEndDate = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (endDateKey.isBlank()) UiStrings.tripEndDateLabel(language)
                            else ReunionDateHelper.formatDateDisplay(endDateKey, language),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 2000) notes = it },
                    label = { Text(UiStrings.tripNotesLabel(language)) },
                    placeholder = { Text(UiStrings.tripNotesHint(language)) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, location, startDateKey, endDateKey, notes) },
                enabled = title.isNotBlank() && !isBusy,
                modifier = Modifier.testTag("save_trip_button")
            ) {
                Text(UiStrings.saveProfile(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiStrings.cancelLabel(language))
            }
        }
    )

    if (pickingStartDate || pickingEndDate) {
        val currentKey = if (pickingStartDate) startDateKey else endDateKey.ifBlank { startDateKey }
        val initMillis = remember(currentKey) {
            ReunionDateHelper.dateKeyToUtcMillis(currentKey) ?: System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initMillis)
        DatePickerDialog(
            onDismissRequest = {
                pickingStartDate = false
                pickingEndDate = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            val key = ReunionDateHelper.utcMillisToDateKey(picked)
                            if (pickingStartDate) startDateKey = key else endDateKey = key
                        }
                        pickingStartDate = false
                        pickingEndDate = false
                    }
                ) {
                    Text(UiStrings.saveProfile(language))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pickingStartDate = false
                        pickingEndDate = false
                    }
                ) {
                    Text(UiStrings.cancelLabel(language))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
