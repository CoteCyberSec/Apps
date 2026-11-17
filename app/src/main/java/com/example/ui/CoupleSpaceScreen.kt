package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PartnerNotificationHelper
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.UiStrings
import com.example.viewmodel.DuetUiState
import com.example.widget.PinWidgetOutcome
import com.example.widget.WidgetUpdateHelper

@Composable
fun CoupleSpaceScreen(
    state: DuetUiState.Ready,
    currentUserId: String,
    language: AppLanguage,
    showSecondary: Boolean,
    isBusy: Boolean,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleSecondary: () -> Unit,
    onUpdateMyProfileIdentity: (displayName: String, avatarId: String) -> Unit,
    onUpdateNextReunion: (dateKey: String, location: String) -> Unit,
    onUpdateRelationshipStartDate: (dateKey: String) -> Unit,
    onUpdateAesthetic: (aestheticId: String) -> Unit,
    onJoinByInviteCode: (code: String, myName: String) -> Unit,
    onShowMessage: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val couple = state.coupleSpace
    val isConnected = couple.memberUids.size >= 2
    val mySlot = remember(couple.creatorUid, currentUserId) {
        AvatarCatalog.slotForUser(couple, currentUserId)
    }

    var joinCodeInput by remember { mutableStateOf("") }
    var showReunionDialog by remember { mutableStateOf(false) }
    var showRelationshipDialog by remember { mutableStateOf(false) }
    var showMyAvatarPicker by remember { mutableStateOf(false) }
    var showAestheticPicker by remember { mutableStateOf(false) }
    var showManualWidgetGuide by remember { mutableStateOf(false) }
    var notificationsGranted by remember {
        mutableStateOf(PartnerNotificationHelper.hasNotificationPermission(context))
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = UiStrings.coupleSpaceTitle(language),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Status + Invite Code Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isConnected) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
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
                        imageVector = if (isConnected) Icons.Rounded.CheckCircle else Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isConnected) UiStrings.connectedStatus(language)
                        else UiStrings.soloOrLocalStatus(language),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isConnected && couple.inviteCode.isNotBlank()) {
                    Text(
                        text = UiStrings.inviteCodeLabel(language),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = couple.inviteCode,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    letterSpacing = 4.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(couple.inviteCode))
                                onShowMessage(UiStrings.codeCopied(language))
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("copy_invite_code_button")
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(UiStrings.copyCode(language))
                        }
                    }
                }
            }
        }

        // Join Partner's Space via Invite Code
        if (!isConnected) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
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
                            imageVector = Icons.Rounded.GroupAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = UiStrings.joinPartnerTitle(language),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { raw ->
                                val cleaned = raw.uppercase().filter { it.isLetterOrDigit() }.take(6)
                                joinCodeInput = cleaned
                            },
                            placeholder = { Text(UiStrings.joinCodeHint(language)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("join_invite_code_input")
                        )

                        Button(
                            onClick = {
                                val myName = state.userProfile?.displayName?.takeIf { it.isNotBlank() }
                                    ?: couple.partnerOneName
                                onJoinByInviteCode(joinCodeInput, myName)
                            },
                            enabled = joinCodeInput.length == 6 && !isBusy,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("join_partner_button")
                        ) {
                            Text(UiStrings.joinButton(language))
                        }
                    }
                }
            }
        }

        // Couple Avatars Showcase Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = UiStrings.coupleAvatarsTitle(language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = UiStrings.coupleAvatarsSubtitle(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val p1Label = couple.partnerOneName.ifBlank { UiStrings.partnerOneField(language) }
                    val p2Label = couple.partnerTwoName.ifBlank { UiStrings.partnerTwoField(language) }
                    val p1IsMe = mySlot == 1
                    val p2IsMe = mySlot == 2

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (p1IsMe) Modifier.clickable { showMyAvatarPicker = true } else Modifier
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("pick_partner_one_avatar")
                    ) {
                        PartnerAvatarBadge(
                            avatarId = couple.partnerOneAvatarId,
                            displayName = p1Label,
                            size = 72.dp,
                            showBorder = true
                        )
                        Text(
                            text = if (p1IsMe) UiStrings.yourAnswerSection(p1Label, language) else p1Label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (p1IsMe) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = UiStrings.changeAvatarAction(language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = UiStrings.partnerReadOnlyBadge(language),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = UiStrings.partnerReadOnlyBadge(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (p2IsMe) Modifier.clickable { showMyAvatarPicker = true } else Modifier
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("pick_partner_two_avatar")
                    ) {
                        PartnerAvatarBadge(
                            avatarId = couple.partnerTwoAvatarId,
                            displayName = p2Label,
                            size = 72.dp,
                            showBorder = true
                        )
                        Text(
                            text = if (p2IsMe) UiStrings.yourAnswerSection(p2Label, language) else p2Label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (p2IsMe) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = UiStrings.changeAvatarAction(language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = UiStrings.partnerReadOnlyBadge(language),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = UiStrings.partnerReadOnlyBadge(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Next Time We See Each Other (Reunion Countdown)
        ReunionCountdownCard(
            couple = couple,
            language = language,
            onEditClick = { showReunionDialog = true }
        )

        // Relationship Duration & Next Anniversary Tracker
        RelationshipTrackerCard(
            couple = couple,
            language = language,
            onEditClick = { showRelationshipDialog = true }
        )

        // Home Screen Widget Preview & Pinning
        CoupleWidgetPreviewCard(
            couple = couple,
            language = language,
            onAddWidgetClick = {
                when (WidgetUpdateHelper.requestPinCountdownWidget(context)) {
                    PinWidgetOutcome.REQUESTED_SYSTEM_PROMPT -> {
                        onShowMessage(UiStrings.widgetPinPromptSent(language))
                    }
                    PinWidgetOutcome.UNSUPPORTED_BY_LAUNCHER -> {
                        showManualWidgetGuide = true
                    }
                }
            }
        )

        // Couple Aesthetic & Theme Card (synced across both devices)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = UiStrings.aestheticSectionTitle(language),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = UiStrings.aestheticSectionTitle(language),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = describeCurrentAesthetic(couple.aestheticThemeId, language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showAestheticPicker = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("open_aesthetic_picker_button")
                    ) {
                        Text(UiStrings.aestheticCustomizeAction(language))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = UiStrings.aestheticSectionSubtitle(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AestheticSwatchDots(aestheticId = couple.aestheticThemeId)
                }
            }
        }

        // Language & Bilingual Display Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = UiStrings.languageSettingsTitle(language),
                    style = MaterialTheme.typography.titleMedium
                )

                LanguageSwitcherPill(
                    language = language,
                    showSecondary = showSecondary,
                    onSelectLanguage = onSelectLanguage,
                    onToggleSecondary = onToggleSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = UiStrings.dualLanguageLabel(language),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = showSecondary,
                        onCheckedChange = { onToggleSecondary() },
                        modifier = Modifier.testTag("dual_language_switch")
                    )
                }
            }
        }

        // Notifications Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (notificationsGranted) Icons.Rounded.NotificationsActive
                        else Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        tint = if (notificationsGranted) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = UiStrings.partnerUpdatesTitle(language),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (notificationsGranted) UiStrings.notificationsEnabled(language)
                            else UiStrings.enableNotifications(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsGranted) {
                    FilledTonalButton(
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.testTag("couple_enable_notifications_button")
                    ) {
                        Text(if (language == AppLanguage.RU) "Включить" else "Enable")
                    }
                }
            }
        }

        // Sign Out
        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sign_out_button")
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(UiStrings.signOut(language))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showReunionDialog) {
        ReunionEditorDialog(
            initialDateKey = couple.nextMeetDateKey,
            initialLocation = couple.nextMeetLocation,
            language = language,
            isBusy = isBusy,
            onDismiss = { showReunionDialog = false },
            onSave = { dateKey, location ->
                onUpdateNextReunion(dateKey, location)
                showReunionDialog = false
            }
        )
    }

    if (showRelationshipDialog) {
        RelationshipDateEditorDialog(
            initialDateKey = couple.relationshipStartDateKey,
            language = language,
            isBusy = isBusy,
            onDismiss = { showRelationshipDialog = false },
            onSave = { dateKey ->
                onUpdateRelationshipStartDate(dateKey)
                showRelationshipDialog = false
            }
        )
    }

    if (showMyAvatarPicker) {
        val currentName = if (mySlot == 1) {
            couple.partnerOneName.ifBlank { state.userProfile?.displayName ?: "" }
        } else {
            couple.partnerTwoName.ifBlank { state.userProfile?.displayName ?: "" }
        }
        val currentAvatar = if (mySlot == 1) couple.partnerOneAvatarId else couple.partnerTwoAvatarId
        AvatarPickerDialog(
            initialName = currentName,
            currentAvatarId = currentAvatar,
            language = language,
            isFirstJoin = false,
            isBusy = isBusy,
            onDismiss = { showMyAvatarPicker = false },
            onSave = { updatedName, selectedId ->
                onUpdateMyProfileIdentity(updatedName, selectedId)
                showMyAvatarPicker = false
            }
        )
    }

    if (showAestheticPicker) {
        AestheticPickerDialog(
            currentAestheticId = couple.aestheticThemeId,
            language = language,
            onDismiss = { showAestheticPicker = false },
            onSelectAesthetic = { themeId ->
                onUpdateAesthetic(themeId)
                showAestheticPicker = false
            }
        )
    }

    if (showManualWidgetGuide) {
        ManualWidgetPinDialog(
            language = language,
            onDismiss = { showManualWidgetGuide = false }
        )
    }
}
