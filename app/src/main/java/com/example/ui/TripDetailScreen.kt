package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SelectAll
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.ReunionDateHelper
import com.example.model.TripMemory
import com.example.model.TripPhoto
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    state: DuetUiState.Ready,
    tripId: String,
    currentUserId: String,
    language: AppLanguage,
    isBusy: Boolean,
    onBack: () -> Unit,
    onUpdateTrip: (
        title: String,
        location: String,
        startDateKey: String,
        endDateKey: String,
        notes: String,
        existing: TripMemory
    ) -> Unit,
    onDeleteTrip: (tripId: String) -> Unit,
    onAddPhotos: (tripId: String, uris: List<Uri>, authorLabel: String) -> Unit,
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
    onDeletePhotos: (photoIds: List<String>) -> Unit
) {
    BackHandler(onBack = onBack)

    val trip = state.trips.firstOrNull { it.id == tripId }
    if (trip == null) {
        LaunchedEffectOnMissingTrip(onBack)
        return
    }

    val authorLabel = remember(state.coupleSpace, state.userProfile, currentUserId) {
        if (state.coupleSpace.creatorUid == currentUserId) {
            state.coupleSpace.partnerOneName.ifBlank { state.userProfile?.displayName ?: "You" }
        } else {
            state.coupleSpace.partnerTwoName.ifBlank { state.userProfile?.displayName ?: "You" }
        }
    }

    val tripPhotos = remember(state.tripPhotos, tripId) {
        state.tripPhotos
            .filter { it.tripId == tripId }
            .sortedWith(compareBy<TripPhoto> { it.takenDateKey }.thenBy { it.takenMillis })
    }
    val photosByDate = remember(tripPhotos) {
        tripPhotos.groupBy { it.takenDateKey }
    }

    var showEditTripDialog by remember { mutableStateOf(false) }
    var inspectingPhoto by remember { mutableStateOf<TripPhoto?>(null) }
    var selectedPhotoIds by remember(tripId) { mutableStateOf<Set<String>>(emptySet()) }
    var showConfirmDeleteSelected by remember { mutableStateOf(false) }

    LaunchedEffect(tripPhotos) {
        if (selectedPhotoIds.isNotEmpty()) {
            val validIds = tripPhotos.mapTo(HashSet()) { it.id }
            selectedPhotoIds = selectedPhotoIds.intersect(validIds)
        }
    }

    BackHandler(enabled = selectedPhotoIds.isNotEmpty()) {
        selectedPhotoIds = emptySet()
    }

    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(12)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddPhotos(tripId, uris, authorLabel)
        }
    }

    Scaffold(
        topBar = {
            if (selectedPhotoIds.isNotEmpty()) {
                TopAppBar(
                    title = {
                        Text(
                            text = UiStrings.selectedPhotosCount(selectedPhotoIds.size, language),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { selectedPhotoIds = emptySet() },
                            modifier = Modifier.testTag("clear_photo_selection_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = UiStrings.cancelLabel(language)
                            )
                        }
                    },
                    actions = {
                        if (tripPhotos.size > 1) {
                            TextButton(
                                onClick = {
                                    selectedPhotoIds = if (selectedPhotoIds.size == tripPhotos.size) {
                                        emptySet()
                                    } else {
                                        tripPhotos.mapTo(HashSet()) { it.id }
                                    }
                                },
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
                            onClick = { showConfirmDeleteSelected = true },
                            enabled = !isBusy,
                            modifier = Modifier.testTag("delete_selected_photos_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = UiStrings.deleteLabel(language),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("photo_selection_bar")
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = trip.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("trip_detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = UiStrings.prevQuestion(language)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showEditTripDialog = true },
                            modifier = Modifier.testTag("edit_trip_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = UiStrings.editTripButton(language)
                            )
                        }
                        IconButton(
                            onClick = {
                                onDeleteTrip(trip.id)
                                onBack()
                            },
                            modifier = Modifier.testTag("delete_trip_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = UiStrings.deleteLabel(language)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "trip_story_card") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (trip.location.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = trip.location,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (trip.startDateKey.isNotBlank()) {
                                val span = if (trip.endDateKey.isNotBlank() && trip.endDateKey != trip.startDateKey) {
                                    "${ReunionDateHelper.formatDateDisplay(trip.startDateKey, language)} – ${
                                        ReunionDateHelper.formatDateDisplay(trip.endDateKey, language)
                                    }"
                                } else {
                                    ReunionDateHelper.formatDateDisplay(trip.startDateKey, language)
                                }
                                Text(
                                    text = span,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (trip.notes.isNotBlank()) {
                            Text(
                                text = trip.notes,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = UiStrings.tripNotesHint(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    multiPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("add_trip_photos_button")
                            ) {
                                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(UiStrings.addPhotosButton(language))
                            }

                            OutlinedButton(
                                onClick = { showEditTripDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("edit_trip_notes_button")
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(UiStrings.editLabel(language))
                            }
                        }
                    }
                }
            }

            if (photosByDate.isEmpty()) {
                item(key = "empty_photos") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
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
                photosByDate.forEach { (dateKey, dayPhotos) ->
                    item(key = "day_header_$dateKey") {
                        DateSectionHeader(
                            dateKey = dateKey,
                            photoCount = dayPhotos.size,
                            language = language
                        )
                    }

                    items(dayPhotos.chunked(2), key = { row -> "day_${dateKey}_${row.first().id}" }) { rowPhotos ->
                        val isSelectionMode = selectedPhotoIds.isNotEmpty()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPhotos.forEach { photo ->
                                val isSelected = photo.id in selectedPhotoIds
                                TripPhotoTile(
                                    photo = photo,
                                    tripNameBadge = null,
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showEditTripDialog) {
        TripEditorDialog(
            existingTrip = trip,
            language = language,
            isBusy = isBusy,
            onDismiss = { showEditTripDialog = false },
            onSave = { title, location, startKey, endKey, notes ->
                onUpdateTrip(title, location, startKey, endKey, notes, trip)
                showEditTripDialog = false
            }
        )
    }

    val activePhotoRaw = inspectingPhoto
    if (activePhotoRaw != null) {
        val activePhoto = state.tripPhotos.firstOrNull { it.id == activePhotoRaw.id } ?: activePhotoRaw
        TripPhotoDetailDialog(
            photo = activePhoto,
            tripTitle = trip.title,
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
private fun LaunchedEffectOnMissingTrip(onBack: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        onBack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPhotoDetailDialog(
    photo: TripPhoto,
    tripTitle: String?,
    language: AppLanguage,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        caption: String,
        takenDateKey: String,
        takenTimeLabel: String,
        takenMillis: Long,
        timestampSource: String
    ) -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    var caption by remember(photo.id) { mutableStateOf(photo.caption) }
    var dateKey by remember(photo.id) { mutableStateOf(photo.takenDateKey) }
    var source by remember(photo.id) { mutableStateOf(photo.timestampSource) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = ReunionDateHelper.formatDateDisplay(dateKey, language) +
                        if (photo.takenTimeLabel.isNotBlank()) " • ${photo.takenTimeLabel}" else "",
                    style = MaterialTheme.typography.titleLarge
                )
                if (!tripTitle.isNullOrBlank()) {
                    Text(
                        text = tripTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripPhotoImage(
                    photo = photo,
                    contentDescription = caption.ifBlank { dateKey },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(16.dp))
                )

                EmojiReactionBar(
                    currentReaction = photo.reaction,
                    language = language,
                    targetTagPrefix = "photo_${photo.id}",
                    onReact = onReact
                )

                FilledTonalButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(UiStrings.changePhotoDate(language))
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { if (it.length <= 200) caption = it },
                    placeholder = { Text(UiStrings.photoCaptionHint(language)) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_caption_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val millis = ReunionDateHelper.dateKeyToUtcMillis(dateKey) ?: photo.takenMillis
                    onSave(caption, dateKey, photo.takenTimeLabel, millis, source)
                },
                enabled = !isBusy
            ) {
                Text(UiStrings.saveProfile(language))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text(
                        text = UiStrings.deleteLabel(language),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(UiStrings.cancelLabel(language))
                }
            }
        }
    )

    if (showDatePicker) {
        val initMillis = remember(dateKey) {
            ReunionDateHelper.dateKeyToUtcMillis(dateKey) ?: System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            dateKey = ReunionDateHelper.utcMillisToDateKey(picked)
                            source = "manual"
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
