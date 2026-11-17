package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.DailyDrawing
import com.example.model.DrawingStroke
import com.example.model.DrawingStrokeCodec
import com.example.model.NormalizedPoint
import com.example.model.ReunionDateHelper
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState
import kotlin.math.hypot

private val DrawingPaletteColors = listOf(
    Color(0xFF26171B), // Espresso Ink
    Color(0xFF8C3B47), // Deep Burgundy
    Color(0xFFC66B56), // Terracotta
    Color(0xFF5A7360), // Sage Olive
    Color(0xFFC89B6E), // Champagne Gold
    Color(0xFFE58C97), // Rose Blush
    Color(0xFF4A6B82), // Slate Blue
    Color(0xFFFFFDF9)  // Cream Paper Eraser
)

private val CanvasPaperColor = Color(0xFFFFFDF9)

private enum class DrawGalleryFilter {
    ALL, MINE, PARTNER
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawScreen(
    state: DuetUiState.Ready,
    currentUserId: String,
    language: AppLanguage,
    isBusy: Boolean,
    onSaveDrawing: (
        dateKey: String,
        speakerRole: String,
        authorLabel: String,
        caption: String,
        encodedStrokes: List<String>,
        existing: DailyDrawing?
    ) -> Unit,
    onReactToDrawing: (drawingId: String, reaction: String) -> Unit,
    onDeleteDrawing: (drawingId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayKey = remember { ReunionDateHelper.todayDateKey() }

    val currentUserDisplayName = remember(state.coupleSpace, state.userProfile, currentUserId) {
        if (state.coupleSpace.creatorUid == currentUserId) {
            state.coupleSpace.partnerOneName.ifBlank { state.userProfile?.displayName ?: "You" }
        } else {
            state.coupleSpace.partnerTwoName.ifBlank { state.userProfile?.displayName ?: "You" }
        }
    }

    var editingDrawing by remember { mutableStateOf<DailyDrawing?>(null) }
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    val activePoints = remember { mutableStateListOf<NormalizedPoint>() }
    var selectedColorIndex by remember { mutableIntStateOf(1) }
    var brushWidthDp by remember { mutableFloatStateOf(8f) }
    var caption by remember { mutableStateOf("") }
    var previewDrawing by remember { mutableStateOf<DailyDrawing?>(null) }
    var galleryFilter by remember { mutableStateOf(DrawGalleryFilter.ALL) }
    var isFullScreenDrawing by remember { mutableStateOf(false) }

    val filteredDrawings = remember(state.drawings, galleryFilter, currentUserId) {
        when (galleryFilter) {
            DrawGalleryFilter.ALL -> state.drawings
            DrawGalleryFilter.MINE -> state.drawings.filter {
                it.authorUid == currentUserId && it.speakerRole == "self"
            }
            DrawGalleryFilter.PARTNER -> state.drawings.filter {
                it.authorUid != currentUserId || it.speakerRole == "partner_local"
            }
        }
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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Brush,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = UiStrings.drawHeader(language),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${ReunionDateHelper.formatDateDisplay(todayKey, language)} • ${UiStrings.drawSubtitle(language)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (editingDrawing != null || strokes.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            editingDrawing = null
                            strokes.clear()
                            caption = ""
                        },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("new_drawing_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(UiStrings.drawNewCanvas(language), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Interactive Drawing Canvas with Full-Screen Toggle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.1f)
                    ) {
                        DrawingInteractiveCanvas(
                            strokes = strokes,
                            activePoints = activePoints,
                            selectedColorIndex = selectedColorIndex,
                            brushWidthDp = brushWidthDp,
                            testTagName = "daily_drawing_canvas",
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            onClick = { isFullScreenDrawing = true },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("open_fullscreen_draw_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Fullscreen,
                                    contentDescription = UiStrings.fullScreenDraw(language),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = UiStrings.fullScreenDraw(language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Color Palette
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DrawingPaletteColors.forEachIndexed { idx, color ->
                            val selected = idx == selectedColorIndex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorIndex = idx }
                                    .testTag("palette_color_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (idx == 7) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(5f to 10.dp, 9f to 16.dp, 16f to 22.dp).forEach { (widthVal, dotSize) ->
                                val selected = brushWidthDp == widthVal
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { brushWidthDp = widthVal }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(dotSize)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                                },
                                enabled = strokes.isNotEmpty(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("undo_stroke_button")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(UiStrings.undoStroke(language), style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { strokes.clear() },
                                enabled = strokes.isNotEmpty(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("clear_canvas_button")
                            ) {
                                Icon(Icons.Rounded.LayersClear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(UiStrings.clearCanvas(language), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { if (it.length <= 140) caption = it },
                        placeholder = { Text(UiStrings.drawCaptionHint(language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawing_caption_input")
                    )

                    Button(
                        onClick = {
                            val encoded = strokes.mapNotNull {
                                DrawingStrokeCodec.encode(it).takeIf { s -> s.isNotBlank() }
                            }
                            val targetExisting = editingDrawing
                            val dateKey = targetExisting?.dateKey ?: todayKey
                            onSaveDrawing(
                                dateKey,
                                "self",
                                targetExisting?.authorLabel ?: currentUserDisplayName,
                                caption,
                                encoded,
                                targetExisting
                            )
                            editingDrawing = null
                            strokes.clear()
                            caption = ""
                        },
                        enabled = strokes.isNotEmpty() && !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_drawing_button")
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (editingDrawing != null) {
                                    UiStrings.updateDrawing(language)
                                } else {
                                    UiStrings.drawSaveAsNew(language)
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // Saved Daily Gallery + Filter Chips (All / Mine / Partner's)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = UiStrings.savedDrawingsTitle(language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = galleryFilter == DrawGalleryFilter.ALL,
                        onClick = { galleryFilter = DrawGalleryFilter.ALL },
                        label = { Text(UiStrings.drawFilterAll(language)) },
                        modifier = Modifier.testTag("draw_filter_all")
                    )
                    FilterChip(
                        selected = galleryFilter == DrawGalleryFilter.MINE,
                        onClick = { galleryFilter = DrawGalleryFilter.MINE },
                        label = { Text(UiStrings.drawFilterMine(language)) },
                        modifier = Modifier.testTag("draw_filter_mine")
                    )
                    FilterChip(
                        selected = galleryFilter == DrawGalleryFilter.PARTNER,
                        onClick = { galleryFilter = DrawGalleryFilter.PARTNER },
                        label = { Text(UiStrings.drawFilterPartner(language)) },
                        modifier = Modifier.testTag("draw_filter_partner")
                    )
                }
            }
        }

        if (filteredDrawings.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = UiStrings.noDrawingsYet(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(filteredDrawings, key = { it.id }) { drawing ->
                val isSelfDrawing = drawing.authorUid == currentUserId && drawing.speakerRole == "self"
                val avatarId = AvatarCatalog.resolveForPartner(
                    couple = state.coupleSpace,
                    authorUid = drawing.authorUid,
                    speakerRole = drawing.speakerRole
                )
                SavedDrawingCard(
                    drawing = drawing,
                    avatarId = avatarId,
                    isSelfDrawing = isSelfDrawing,
                    canEditOrDelete = drawing.authorUid == currentUserId && drawing.speakerRole == "self",
                    language = language,
                    onClick = { previewDrawing = drawing },
                    onEdit = {
                        editingDrawing = drawing
                        strokes.clear()
                        strokes.addAll(DrawingStrokeCodec.decode(drawing.strokes))
                        caption = drawing.caption
                    },
                    onReact = { emoji -> onReactToDrawing(drawing.id, emoji) },
                    onDelete = { onDeleteDrawing(drawing.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    val activePreviewRaw = previewDrawing
    if (activePreviewRaw != null) {
        val activePreview = state.drawings.firstOrNull { it.id == activePreviewRaw.id } ?: activePreviewRaw
        AlertDialog(
            onDismissRequest = { previewDrawing = null },
            title = {
                val previewAvatarId = AvatarCatalog.resolveForPartner(
                    couple = state.coupleSpace,
                    authorUid = activePreview.authorUid,
                    speakerRole = activePreview.speakerRole
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PartnerAvatarBadge(
                        avatarId = previewAvatarId,
                        displayName = activePreview.authorLabel,
                        size = 40.dp,
                        showBorder = true
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = activePreview.authorLabel,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = UiStrings.drawDrawnOn(
                                ReunionDateHelper.formatDateDisplay(activePreview.dateKey, language),
                                language
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DrawingCanvasPreview(
                        encodedStrokes = activePreview.strokes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.25f)
                    )
                    if (activePreview.caption.isNotBlank()) {
                        Text(
                            text = activePreview.caption,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    EmojiReactionBar(
                        currentReaction = activePreview.reaction,
                        language = language,
                        targetTagPrefix = "drawing_preview_${activePreview.id}",
                        onReact = { emoji -> onReactToDrawing(activePreview.id, emoji) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { previewDrawing = null }) {
                    Text(UiStrings.closeComposerAction(language))
                }
            }
        )
    }

    if (isFullScreenDrawing) {
        FullScreenDrawingDialog(
            strokes = strokes,
            activePoints = activePoints,
            selectedColorIndex = selectedColorIndex,
            brushWidthDp = brushWidthDp,
            language = language,
            isEditing = editingDrawing != null,
            isBusy = isBusy,
            onSelectColor = { selectedColorIndex = it },
            onSelectBrushWidth = { brushWidthDp = it },
            onUndo = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) },
            onClear = { strokes.clear() },
            onSaveAndClose = {
                val encoded = strokes.mapNotNull {
                    DrawingStrokeCodec.encode(it).takeIf { s -> s.isNotBlank() }
                }
                val targetExisting = editingDrawing
                val dateKey = targetExisting?.dateKey ?: todayKey
                onSaveDrawing(
                    dateKey,
                    "self",
                    targetExisting?.authorLabel ?: currentUserDisplayName,
                    caption,
                    encoded,
                    targetExisting
                )
                editingDrawing = null
                strokes.clear()
                caption = ""
                isFullScreenDrawing = false
            },
            onMinimize = { isFullScreenDrawing = false }
        )
    }
}

@Composable
private fun DrawingInteractiveCanvas(
    strokes: SnapshotStateList<DrawingStroke>,
    activePoints: SnapshotStateList<NormalizedPoint>,
    selectedColorIndex: Int,
    brushWidthDp: Float,
    testTagName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CanvasPaperColor,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .testTag(testTagName)
                .pointerInput(selectedColorIndex, brushWidthDp) {
                    detectTapGestures { offset ->
                        if (size.width > 0 && size.height > 0 && strokes.size < 150) {
                            val nx = (offset.x / size.width).coerceIn(0f, 1f)
                            val ny = (offset.y / size.height).coerceIn(0f, 1f)
                            strokes.add(
                                DrawingStroke(
                                    colorIndex = selectedColorIndex,
                                    widthDp = brushWidthDp,
                                    points = listOf(
                                        NormalizedPoint(nx, ny),
                                        NormalizedPoint(
                                            (nx + 0.002f).coerceAtMost(1f),
                                            (ny + 0.002f).coerceAtMost(1f)
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
                .pointerInput(selectedColorIndex, brushWidthDp) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            activePoints.clear()
                            if (size.width > 0 && size.height > 0) {
                                activePoints.add(
                                    NormalizedPoint(
                                        (offset.x / size.width).coerceIn(0f, 1f),
                                        (offset.y / size.height).coerceIn(0f, 1f)
                                    )
                                )
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (size.width > 0 && size.height > 0 && activePoints.size < 180) {
                                val nx = (change.position.x / size.width).coerceIn(0f, 1f)
                                val ny = (change.position.y / size.height).coerceIn(0f, 1f)
                                val last = activePoints.lastOrNull()
                                if (last == null || hypot(nx - last.x, ny - last.y) >= 0.007f) {
                                    activePoints.add(NormalizedPoint(nx, ny))
                                }
                            }
                        },
                        onDragEnd = {
                            if (activePoints.isNotEmpty() && strokes.size < 150) {
                                strokes.add(
                                    DrawingStroke(
                                        colorIndex = selectedColorIndex,
                                        widthDp = brushWidthDp,
                                        points = activePoints.toList()
                                    )
                                )
                            }
                            activePoints.clear()
                        },
                        onDragCancel = {
                            activePoints.clear()
                        }
                    )
                }
        ) {
            strokes.forEach { stroke ->
                drawNormalizedStroke(stroke)
            }
            if (activePoints.isNotEmpty()) {
                drawNormalizedStroke(
                    DrawingStroke(
                        colorIndex = selectedColorIndex,
                        widthDp = brushWidthDp,
                        points = activePoints
                    )
                )
            }
        }
    }
}

@Composable
private fun FullScreenDrawingDialog(
    strokes: SnapshotStateList<DrawingStroke>,
    activePoints: SnapshotStateList<NormalizedPoint>,
    selectedColorIndex: Int,
    brushWidthDp: Float,
    language: AppLanguage,
    isEditing: Boolean,
    isBusy: Boolean,
    onSelectColor: (Int) -> Unit,
    onSelectBrushWidth: (Float) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onSaveAndClose: () -> Unit,
    onMinimize: () -> Unit
) {
    Dialog(
        onDismissRequest = onMinimize,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler(onBack = onMinimize)

        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onMinimize,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("exit_fullscreen_draw_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FullscreenExit,
                            contentDescription = UiStrings.exitFullScreenDraw(language),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(UiStrings.exitFullScreenDraw(language), style = MaterialTheme.typography.labelLarge)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onUndo,
                            enabled = strokes.isNotEmpty(),
                            modifier = Modifier.testTag("fullscreen_undo_stroke_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Undo,
                                contentDescription = UiStrings.undoStroke(language)
                            )
                        }
                        IconButton(
                            onClick = onClear,
                            enabled = strokes.isNotEmpty(),
                            modifier = Modifier.testTag("fullscreen_clear_canvas_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LayersClear,
                                contentDescription = UiStrings.clearCanvas(language)
                            )
                        }
                        Button(
                            onClick = onSaveAndClose,
                            enabled = strokes.isNotEmpty() && !isBusy,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("fullscreen_save_drawing_button")
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isEditing) UiStrings.updateDrawing(language)
                                    else UiStrings.drawSaveAsNew(language),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                DrawingInteractiveCanvas(
                    strokes = strokes,
                    activePoints = activePoints,
                    selectedColorIndex = selectedColorIndex,
                    brushWidthDp = brushWidthDp,
                    testTagName = "fullscreen_drawing_canvas",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                itemsIndexed(DrawingPaletteColors) { idx, color ->
                                    val selected = idx == selectedColorIndex
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (selected) 2.5.dp else 1.dp,
                                                color = if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            )
                                            .clickable { onSelectColor(idx) }
                                            .testTag("fullscreen_palette_color_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = if (idx == 7) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(5f to 10.dp, 9f to 16.dp, 16f to 22.dp).forEach { (widthVal, dotSize) ->
                                    val selected = brushWidthDp == widthVal
                                    Surface(
                                        shape = CircleShape,
                                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .clickable { onSelectBrushWidth(widthVal) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Box(
                                                modifier = Modifier
                                                    .size(dotSize)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onSurface)
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
}

@Composable
private fun SavedDrawingCard(
    drawing: DailyDrawing,
    avatarId: String,
    isSelfDrawing: Boolean,
    canEditOrDelete: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(drawing.dateKey, language) {
        ReunionDateHelper.formatDateDisplay(drawing.dateKey, language)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_drawing_card_${drawing.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PartnerAvatarBadge(
                        avatarId = avatarId,
                        displayName = drawing.authorLabel,
                        size = 28.dp,
                        showBorder = true
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelfDrawing) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = drawing.authorLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelfDrawing) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (canEditOrDelete) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("edit_drawing_${drawing.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = UiStrings.editLabel(language),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_drawing_${drawing.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = UiStrings.deleteLabel(language),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            DrawingCanvasPreview(
                encodedStrokes = drawing.strokes,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.45f)
            )

            if (drawing.caption.isNotBlank()) {
                Text(
                    text = drawing.caption,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            EmojiReactionBar(
                currentReaction = drawing.reaction,
                language = language,
                targetTagPrefix = "drawing_${drawing.id}",
                onReact = onReact
            )
        }
    }
}

@Composable
fun DrawingCanvasPreview(
    encodedStrokes: List<String>,
    modifier: Modifier = Modifier
) {
    val decoded = remember(encodedStrokes) { DrawingStrokeCodec.decode(encodedStrokes) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CanvasPaperColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            decoded.forEach { stroke ->
                drawNormalizedStroke(stroke)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNormalizedStroke(stroke: DrawingStroke) {
    val pts = stroke.points
    if (pts.isEmpty()) return
    val color = DrawingPaletteColors.getOrElse(stroke.colorIndex) { DrawingPaletteColors.first() }
    val strokePx = stroke.widthDp.dp.toPx()

    if (pts.size == 1) {
        drawCircle(
            color = color,
            radius = strokePx / 2f,
            center = Offset(pts[0].x * size.width, pts[0].y * size.height)
        )
        return
    }

    val path = Path().apply {
        moveTo(pts[0].x * size.width, pts[0].y * size.height)
        for (i in 1 until pts.size) {
            lineTo(pts[i].x * size.width, pts[i].y * size.height)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
