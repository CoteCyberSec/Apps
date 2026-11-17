package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.CoupleAvatar
import com.example.model.UiStrings

@Composable
fun PartnerAvatarBadge(
    avatarId: String,
    displayName: String,
    size: Dp = 44.dp,
    showBorder: Boolean = true,
    modifier: Modifier = Modifier
) {
    val preset = AvatarCatalog.findById(avatarId)
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "♥"
    val borderColor = if (preset != null) Color(preset.ringHex) else MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = if (size >= 56.dp) 2.dp else 1.5.dp,
                        color = borderColor.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
                } else Modifier
            )
            .semantics { contentDescription = displayName.ifBlank { "Partner avatar" } },
        contentAlignment = Alignment.Center
    ) {
        if (preset != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(preset.bgTopHex), Color(preset.bgBottomHex))
                    )
                )
                drawPresetAvatarArt(preset.id)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.42f).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvatarPickerDialog(
    initialName: String,
    currentAvatarId: String,
    language: AppLanguage,
    isFirstJoin: Boolean = false,
    isBusy: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (displayName: String, avatarId: String) -> Unit
) {
    var nameInput by remember(initialName) { mutableStateOf(initialName) }
    var selectedAvatarId by remember(currentAvatarId) {
        mutableStateOf(currentAvatarId.ifBlank { "sunny_cat" })
    }

    AlertDialog(
        onDismissRequest = { if (!isFirstJoin) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isFirstJoin,
            dismissOnClickOutside = !isFirstJoin
        ),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isFirstJoin) UiStrings.welcomeSetupTitle(language)
                        else UiStrings.chooseAvatarFor(nameInput, language),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (isFirstJoin) UiStrings.welcomeSetupSubtitle(language)
                        else UiStrings.coupleAvatarsSubtitle(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isFirstJoin) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_avatar_picker")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = UiStrings.cancelLabel(language)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 80) nameInput = it },
                    label = { Text(UiStrings.yourNameFieldLabel(language)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarCatalog.presets.forEach { preset ->
                        val selected = preset.id == selectedAvatarId
                        AvatarPresetOptionCard(
                            preset = preset,
                            selected = selected,
                            language = language,
                            onClick = { selectedAvatarId = preset.id },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = nameInput.trim()
                    if (trimmed.isNotEmpty()) {
                        onSave(trimmed, selectedAvatarId)
                    }
                },
                enabled = nameInput.trim().isNotEmpty() && !isBusy,
                modifier = Modifier.testTag("save_profile_identity_button")
            ) {
                Text(
                    text = if (isFirstJoin) UiStrings.getStartedAction(language)
                    else UiStrings.saveProfile(language)
                )
            }
        },
        dismissButton = if (!isFirstJoin) {
            {
                TextButton(onClick = onDismiss) {
                    Text(UiStrings.cancelLabel(language))
                }
            }
        } else null
    )
}

@Composable
private fun AvatarPresetOptionCard(
    preset: CoupleAvatar,
    selected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("avatar_option_${preset.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                PartnerAvatarBadge(
                    avatarId = preset.id,
                    displayName = preset.name.primary(language),
                    size = 64.dp,
                    showBorder = true
                )
                if (selected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = preset.name.primary(language),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun DrawScope.drawPresetAvatarArt(id: String) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = w.coerceAtMost(h) * 0.5f
    val ink = Color(0xFF2B1D1A)
    val blush = Color(0xFFF28482).copy(alpha = 0.65f)
    val cream = Color(0xFFFFF8F0)

    when (id) {
        "sunny_cat" -> {
            val catGold = Color(0xFFF4A261)
            // Left & right pointed ears
            drawPath(
                Path().apply {
                    moveTo(cx - r * 0.52f, cy - r * 0.12f)
                    lineTo(cx - r * 0.58f, cy - r * 0.66f)
                    lineTo(cx - r * 0.14f, cy - r * 0.42f)
                    close()
                },
                color = catGold
            )
            drawPath(
                Path().apply {
                    moveTo(cx + r * 0.52f, cy - r * 0.12f)
                    lineTo(cx + r * 0.58f, cy - r * 0.66f)
                    lineTo(cx + r * 0.14f, cy - r * 0.42f)
                    close()
                },
                color = catGold
            )
            // Face
            drawCircle(color = cream, radius = r * 0.54f, center = Offset(cx, cy + r * 0.08f))
            // Forehead stripes
            drawLine(catGold, Offset(cx, cy - r * 0.42f), Offset(cx, cy - r * 0.28f), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
            // Blush
            drawCircle(blush, radius = r * 0.1f, center = Offset(cx - r * 0.28f, cy + r * 0.18f))
            drawCircle(blush, radius = r * 0.1f, center = Offset(cx + r * 0.28f, cy + r * 0.18f))
            // Happy eyes
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx - r * 0.2f, cy + r * 0.02f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx + r * 0.2f, cy + r * 0.02f))
            // Nose & whiskers
            drawCircle(Color(0xFFE76F51), radius = r * 0.055f, center = Offset(cx, cy + r * 0.14f))
            drawSmile(cx, cy + r * 0.2f, r * 0.14f, ink, r * 0.045f)
            drawLine(ink.copy(alpha = 0.5f), Offset(cx - r * 0.36f, cy + r * 0.12f), Offset(cx - r * 0.58f, cy + r * 0.08f), strokeWidth = r * 0.03f, cap = StrokeCap.Round)
            drawLine(ink.copy(alpha = 0.5f), Offset(cx + r * 0.36f, cy + r * 0.12f), Offset(cx + r * 0.58f, cy + r * 0.08f), strokeWidth = r * 0.03f, cap = StrokeCap.Round)
        }

        "moon_bunny" -> {
            // Tall bunny ears
            drawRoundRect(
                color = cream,
                topLeft = Offset(cx - r * 0.36f, cy - r * 0.82f),
                size = Size(r * 0.24f, r * 0.62f),
                cornerRadius = CornerRadius(r * 0.12f, r * 0.12f)
            )
            drawRoundRect(
                color = cream,
                topLeft = Offset(cx + r * 0.12f, cy - r * 0.82f),
                size = Size(r * 0.24f, r * 0.62f),
                cornerRadius = CornerRadius(r * 0.12f, r * 0.12f)
            )
            // Inner ears
            drawRoundRect(
                color = Color(0xFFF4ACB7),
                topLeft = Offset(cx - r * 0.30f, cy - r * 0.70f),
                size = Size(r * 0.12f, r * 0.40f),
                cornerRadius = CornerRadius(r * 0.06f, r * 0.06f)
            )
            drawRoundRect(
                color = Color(0xFFF4ACB7),
                topLeft = Offset(cx + r * 0.18f, cy - r * 0.70f),
                size = Size(r * 0.12f, r * 0.40f),
                cornerRadius = CornerRadius(r * 0.06f, r * 0.06f)
            )
            // Face
            drawCircle(cream, radius = r * 0.52f, center = Offset(cx, cy + r * 0.14f))
            drawCircle(blush, radius = r * 0.095f, center = Offset(cx - r * 0.27f, cy + r * 0.22f))
            drawCircle(blush, radius = r * 0.095f, center = Offset(cx + r * 0.27f, cy + r * 0.22f))
            drawCircle(ink, radius = r * 0.06f, center = Offset(cx - r * 0.19f, cy + r * 0.09f))
            drawCircle(ink, radius = r * 0.06f, center = Offset(cx + r * 0.19f, cy + r * 0.09f))
            drawCircle(Color(0xFFE56B6F), radius = r * 0.05f, center = Offset(cx, cy + r * 0.2f))
            drawSmile(cx, cy + r * 0.25f, r * 0.12f, ink, r * 0.042f)
        }

        "cozy_bear" -> {
            val fur = Color(0xFFA47148)
            val innerEar = Color(0xFFE6CCB2)
            drawCircle(fur, radius = r * 0.22f, center = Offset(cx - r * 0.42f, cy - r * 0.34f))
            drawCircle(fur, radius = r * 0.22f, center = Offset(cx + r * 0.42f, cy - r * 0.34f))
            drawCircle(innerEar, radius = r * 0.11f, center = Offset(cx - r * 0.42f, cy - r * 0.34f))
            drawCircle(innerEar, radius = r * 0.11f, center = Offset(cx + r * 0.42f, cy - r * 0.34f))
            drawCircle(fur, radius = r * 0.56f, center = Offset(cx, cy + r * 0.08f))
            drawCircle(innerEar, radius = r * 0.24f, center = Offset(cx, cy + r * 0.22f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx - r * 0.2f, cy - r * 0.02f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx + r * 0.2f, cy - r * 0.02f))
            drawCircle(ink, radius = r * 0.075f, center = Offset(cx, cy + r * 0.15f))
            drawSmile(cx, cy + r * 0.24f, r * 0.14f, ink, r * 0.045f)
        }

        "dreamy_fox" -> {
            val rust = Color(0xFFD95D39)
            drawPath(
                Path().apply {
                    moveTo(cx - r * 0.5f, cy - r * 0.08f)
                    lineTo(cx - r * 0.56f, cy - r * 0.65f)
                    lineTo(cx - r * 0.12f, cy - r * 0.38f)
                    close()
                },
                color = rust
            )
            drawPath(
                Path().apply {
                    moveTo(cx + r * 0.5f, cy - r * 0.08f)
                    lineTo(cx + r * 0.56f, cy - r * 0.65f)
                    lineTo(cx + r * 0.12f, cy - r * 0.38f)
                    close()
                },
                color = rust
            )
            drawCircle(rust, radius = r * 0.54f, center = Offset(cx, cy + r * 0.08f))
            // Cream cheeks
            drawCircle(cream, radius = r * 0.26f, center = Offset(cx - r * 0.24f, cy + r * 0.24f))
            drawCircle(cream, radius = r * 0.26f, center = Offset(cx + r * 0.24f, cy + r * 0.24f))
            drawCircle(cream, radius = r * 0.22f, center = Offset(cx, cy + r * 0.3f))
            drawCircle(ink, radius = r * 0.06f, center = Offset(cx - r * 0.2f, cy + r * 0.04f))
            drawCircle(ink, radius = r * 0.06f, center = Offset(cx + r * 0.2f, cy + r * 0.04f))
            drawCircle(ink, radius = r * 0.07f, center = Offset(cx, cy + r * 0.24f))
        }

        "starry_owl" -> {
            val navy = Color(0xFF456990)
            // Twinkle stars
            drawCircle(Color(0xFFFFD166), radius = r * 0.055f, center = Offset(cx - r * 0.62f, cy - r * 0.45f))
            drawCircle(Color(0xFFFFD166), radius = r * 0.045f, center = Offset(cx + r * 0.62f, cy - r * 0.38f))
            // Ear tufts
            drawCircle(navy, radius = r * 0.56f, center = Offset(cx, cy + r * 0.08f))
            // Big white eye discs
            drawCircle(cream, radius = r * 0.21f, center = Offset(cx - r * 0.21f, cy + r * 0.02f))
            drawCircle(cream, radius = r * 0.21f, center = Offset(cx + r * 0.21f, cy + r * 0.02f))
            drawCircle(ink, radius = r * 0.095f, center = Offset(cx - r * 0.21f, cy + r * 0.02f))
            drawCircle(ink, radius = r * 0.095f, center = Offset(cx + r * 0.21f, cy + r * 0.02f))
            // Sparkle in eyes
            drawCircle(Color.White, radius = r * 0.03f, center = Offset(cx - r * 0.24f, cy - r * 0.01f))
            drawCircle(Color.White, radius = r * 0.03f, center = Offset(cx + r * 0.18f, cy - r * 0.01f))
            // Golden beak
            drawPath(
                Path().apply {
                    moveTo(cx - r * 0.09f, cy + r * 0.14f)
                    lineTo(cx + r * 0.09f, cy + r * 0.14f)
                    lineTo(cx, cy + r * 0.29f)
                    close()
                },
                color = Color(0xFFFFB703)
            )
        }

        "matcha_frog" -> {
            val green = Color(0xFF6B9080)
            val paleGreen = Color(0xFFEAF4F4)
            drawCircle(green, radius = r * 0.22f, center = Offset(cx - r * 0.28f, cy - r * 0.3f))
            drawCircle(green, radius = r * 0.22f, center = Offset(cx + r * 0.28f, cy - r * 0.3f))
            drawOval(green, topLeft = Offset(cx - r * 0.58f, cy - r * 0.24f), size = Size(r * 1.16f, r * 0.86f))
            drawCircle(paleGreen, radius = r * 0.13f, center = Offset(cx - r * 0.28f, cy - r * 0.3f))
            drawCircle(paleGreen, radius = r * 0.13f, center = Offset(cx + r * 0.28f, cy - r * 0.3f))
            drawCircle(ink, radius = r * 0.07f, center = Offset(cx - r * 0.28f, cy - r * 0.3f))
            drawCircle(ink, radius = r * 0.07f, center = Offset(cx + r * 0.28f, cy - r * 0.3f))
            drawCircle(blush, radius = r * 0.11f, center = Offset(cx - r * 0.32f, cy + r * 0.16f))
            drawCircle(blush, radius = r * 0.11f, center = Offset(cx + r * 0.32f, cy + r * 0.16f))
            drawSmile(cx, cy + r * 0.14f, r * 0.2f, ink, r * 0.05f)
        }

        "espresso_shiba" -> {
            val caramel = Color(0xFFDDA15E)
            drawPath(
                Path().apply {
                    moveTo(cx - r * 0.48f, cy - r * 0.1f)
                    lineTo(cx - r * 0.5f, cy - r * 0.62f)
                    lineTo(cx - r * 0.12f, cy - r * 0.4f)
                    close()
                },
                color = caramel
            )
            drawPath(
                Path().apply {
                    moveTo(cx + r * 0.48f, cy - r * 0.1f)
                    lineTo(cx + r * 0.5f, cy - r * 0.62f)
                    lineTo(cx + r * 0.12f, cy - r * 0.4f)
                    close()
                },
                color = caramel
            )
            drawCircle(caramel, radius = r * 0.54f, center = Offset(cx, cy + r * 0.08f))
            drawOval(cream, topLeft = Offset(cx - r * 0.42f, cy + r * 0.02f), size = Size(r * 0.84f, r * 0.58f))
            // Cream shiba eyebrows
            drawCircle(cream, radius = r * 0.07f, center = Offset(cx - r * 0.2f, cy - r * 0.1f))
            drawCircle(cream, radius = r * 0.07f, center = Offset(cx + r * 0.2f, cy - r * 0.1f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx - r * 0.2f, cy + r * 0.06f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx + r * 0.2f, cy + r * 0.06f))
            drawCircle(ink, radius = r * 0.07f, center = Offset(cx, cy + r * 0.19f))
            drawSmile(cx, cy + r * 0.26f, r * 0.13f, ink, r * 0.045f)
        }

        "berry_panda" -> {
            drawCircle(ink, radius = r * 0.2f, center = Offset(cx - r * 0.4f, cy - r * 0.34f))
            drawCircle(ink, radius = r * 0.2f, center = Offset(cx + r * 0.4f, cy - r * 0.34f))
            drawCircle(cream, radius = r * 0.54f, center = Offset(cx, cy + r * 0.08f))
            drawCircle(ink, radius = r * 0.14f, center = Offset(cx - r * 0.21f, cy + r * 0.04f))
            drawCircle(ink, radius = r * 0.14f, center = Offset(cx + r * 0.21f, cy + r * 0.04f))
            drawCircle(Color.White, radius = r * 0.045f, center = Offset(cx - r * 0.19f, cy + r * 0.02f))
            drawCircle(Color.White, radius = r * 0.045f, center = Offset(cx + r * 0.23f, cy + r * 0.02f))
            drawCircle(blush, radius = r * 0.09f, center = Offset(cx - r * 0.31f, cy + r * 0.24f))
            drawCircle(blush, radius = r * 0.09f, center = Offset(cx + r * 0.31f, cy + r * 0.24f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx, cy + r * 0.18f))
            drawSmile(cx, cy + r * 0.25f, r * 0.12f, ink, r * 0.04f)
        }

        "honey_bee" -> {
            // Antennae
            drawLine(ink, Offset(cx - r * 0.15f, cy - r * 0.35f), Offset(cx - r * 0.28f, cy - r * 0.62f), strokeWidth = r * 0.05f, cap = StrokeCap.Round)
            drawLine(ink, Offset(cx + r * 0.15f, cy - r * 0.35f), Offset(cx + r * 0.28f, cy - r * 0.62f), strokeWidth = r * 0.05f, cap = StrokeCap.Round)
            drawCircle(ink, radius = r * 0.07f, center = Offset(cx - r * 0.28f, cy - r * 0.62f))
            drawCircle(ink, radius = r * 0.07f, center = Offset(cx + r * 0.28f, cy - r * 0.62f))
            // Wings
            drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.22f, center = Offset(cx - r * 0.5f, cy - r * 0.08f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.22f, center = Offset(cx + r * 0.5f, cy - r * 0.08f))
            // Golden head
            drawCircle(Color(0xFFFFCA3A), radius = r * 0.5f, center = Offset(cx, cy + r * 0.1f))
            drawCircle(blush, radius = r * 0.09f, center = Offset(cx - r * 0.25f, cy + r * 0.2f))
            drawCircle(blush, radius = r * 0.09f, center = Offset(cx + r * 0.25f, cy + r * 0.2f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx - r * 0.18f, cy + r * 0.06f))
            drawCircle(ink, radius = r * 0.065f, center = Offset(cx + r * 0.18f, cy + r * 0.06f))
            drawSmile(cx, cy + r * 0.18f, r * 0.15f, ink, r * 0.048f)
        }

        else -> {
            // cloud_koala
            val slate = Color(0xFF7D93A6)
            drawCircle(slate, radius = r * 0.26f, center = Offset(cx - r * 0.48f, cy - r * 0.14f))
            drawCircle(slate, radius = r * 0.26f, center = Offset(cx + r * 0.48f, cy - r * 0.14f))
            drawCircle(cream, radius = r * 0.14f, center = Offset(cx - r * 0.48f, cy - r * 0.14f))
            drawCircle(cream, radius = r * 0.14f, center = Offset(cx + r * 0.48f, cy - r * 0.14f))
            drawCircle(slate, radius = r * 0.52f, center = Offset(cx, cy + r * 0.08f))
            drawCircle(ink, radius = r * 0.06f, center = Offset(cx - r * 0.22f, cy + r * 0.03f))
            drawCircle(ink, radius = r * 0.06f, center = Offset(cx + r * 0.22f, cy + r * 0.03f))
            drawOval(Color(0xFF2B2D42), topLeft = Offset(cx - r * 0.09f, cy + r * 0.04f), size = Size(r * 0.18f, r * 0.24f))
            drawSmile(cx, cy + r * 0.29f, r * 0.12f, ink, r * 0.04f)
        }
    }
}

private fun DrawScope.drawSmile(
    cx: Float,
    cy: Float,
    halfWidth: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = Path().apply {
        moveTo(cx - halfWidth, cy)
        quadraticTo(cx, cy + halfWidth * 0.85f, cx + halfWidth, cy)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
