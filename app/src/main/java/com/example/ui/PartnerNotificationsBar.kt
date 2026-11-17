package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.PartnerNotificationHelper
import com.example.model.AppLanguage
import com.example.model.AvatarCatalog
import com.example.model.CoupleSpace
import com.example.model.UiStrings
import com.example.viewmodel.PartnerAnswerNotification

@Composable
fun PartnerBottomNotificationBar(
    notifications: List<PartnerAnswerNotification>,
    language: AppLanguage,
    onClick: (unseenItems: List<PartnerAnswerNotification>) -> Unit,
    modifier: Modifier = Modifier
) {
    val unseenItems = remember(notifications) { notifications.filter { it.unseen } }
    val hasUnseen = unseenItems.isNotEmpty()
    val latestPrompt = unseenItems.firstOrNull()?.question?.text?.primary(language) ?: ""

    AnimatedVisibility(visible = hasUnseen) {
        Surface(
            onClick = { onClick(unseenItems) },
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            tonalElevation = 4.dp,
            modifier = modifier
                .fillMaxWidth()
                .testTag("partner_bottom_notification_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = UiStrings.partnerUpdatesTitle(language),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = UiStrings.bottomNewAnswers(unseenItems.size, latestPrompt, language),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${unseenItems.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerNotificationsSheet(
    notifications: List<PartnerAnswerNotification>,
    coupleSpace: CoupleSpace,
    language: AppLanguage,
    showSecondary: Boolean,
    onDismiss: () -> Unit,
    onOpenQuestion: (PartnerAnswerNotification) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(PartnerNotificationHelper.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }
    val unseenNotifications = remember(notifications) { notifications.filter { it.unseen } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = UiStrings.partnerUpdatesTitle(language),
                    style = MaterialTheme.typography.headlineSmall
                )

                if (unseenNotifications.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onMarkAllRead()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("mark_all_notifications_read")
                    ) {
                        Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(UiStrings.markAllRead(language))
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = UiStrings.enableNotifications(language),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.testTag("request_notifications_permission_button")
                        ) {
                            Text(UiStrings.openQuestion(language).let {
                                if (language == AppLanguage.RU) "Разрешить" else "Allow"
                            })
                        }
                    }
                }
            }

            if (unseenNotifications.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = UiStrings.noPartnerAnswersYet(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.height(340.dp)
                ) {
                    items(unseenNotifications, key = { it.answer.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (item.unseen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (item.unseen) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onOpenQuestion(item) }
                                .testTag("notification_item_${item.answer.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val avatarId = AvatarCatalog.resolveForPartner(
                                        couple = coupleSpace,
                                        authorUid = item.answer.authorUid,
                                        speakerRole = item.answer.speakerRole
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        PartnerAvatarBadge(
                                            avatarId = avatarId,
                                            displayName = item.answer.authorLabel,
                                            size = 24.dp,
                                            showBorder = true
                                        )
                                        Text(
                                            text = "${item.deck.emojiBadge} ${item.answer.authorLabel} • ${item.deck.title.primary(language)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (item.unseen) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = UiStrings.newBadge(language),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                BilingualPromptText(
                                    text = item.question.text,
                                    language = language,
                                    showSecondary = showSecondary,
                                    primaryStyle = MaterialTheme.typography.titleMedium,
                                    secondaryStyle = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = "\"${item.answer.text}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
