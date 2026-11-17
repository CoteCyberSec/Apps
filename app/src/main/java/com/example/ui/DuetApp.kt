package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.AuthHelper
import com.example.data.CoupleRepository
import com.example.data.LanguagePreferences
import com.example.data.LanguageSettings
import com.example.model.AppLanguage
import com.example.model.CoupleAestheticCatalog
import com.example.model.UiStrings
import com.example.ui.theme.DuetTheme
import com.example.viewmodel.DuetUiState
import com.example.viewmodel.DuetViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

private const val ROUTE_TOPICS = "topics"
private const val ROUTE_DRAW = "draw"
private const val ROUTE_TRIPS = "trips"
private const val ROUTE_JOURNAL = "journal"
private const val ROUTE_COUPLE = "couple"
private const val ROUTE_TOPIC_DETAIL = "topic_detail/{topicId}?questionId={questionId}"
private const val ROUTE_TRIP_DETAIL = "trip_detail/{tripId}"

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun DuetApp(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val languagePreferences = remember(context) { LanguagePreferences(context) }
    val languageSettings by languagePreferences.settingsFlow.collectAsStateWithLifecycle(
        initialValue = LanguageSettings()
    )

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(
        initialValue = auth.currentUser
    )
    var localUserOverride by remember { mutableStateOf(auth.currentUser) }
    DisposableEffect(currentUser) {
        localUserOverride = currentUser
        onDispose {}
    }

    val activeUser = currentUser ?: localUserOverride
    if (activeUser == null) {
        DuetTheme(aestheticId = languageSettings.lastAestheticId) {
            AuthScreen(
                language = languageSettings.primaryLanguage,
                showSecondary = languageSettings.showSecondaryLanguage,
                onSelectLanguage = { lang ->
                    scope.launch { languagePreferences.setPrimaryLanguage(lang) }
                },
                onToggleSecondary = {
                    scope.launch {
                        languagePreferences.setShowSecondaryLanguage(!languageSettings.showSecondaryLanguage)
                    }
                },
                onAuthSuccess = {
                    localUserOverride = auth.currentUser
                }
            )
        }
    } else {
        key(activeUser.uid) {
            AuthenticatedDuetShell(
                currentUserId = activeUser.uid,
                fallbackDisplayName = activeUser.displayName ?: activeUser.email?.substringBefore("@") ?: "Partner 1",
                languagePreferences = languagePreferences
            )
        }
    }
}

@Composable
private fun AuthenticatedDuetShell(
    currentUserId: String,
    fallbackDisplayName: String,
    languagePreferences: LanguagePreferences
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: DuetViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val repository = CoupleRepository(app.applicationContext)
                DuetViewModel(
                    appContext = app.applicationContext,
                    repository = repository,
                    languagePreferences = languagePreferences,
                    currentUserId = currentUserId,
                    fallbackDisplayName = fallbackDisplayName
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val langSettings by viewModel.languageSettings.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    val language = langSettings.primaryLanguage
    val showSecondary = langSettings.showSecondaryLanguage

    val activeAestheticId = when (val s = uiState) {
        is DuetUiState.Ready -> s.coupleSpace.aestheticThemeId.ifBlank { CoupleAestheticCatalog.DEFAULT_ID }
        else -> langSettings.lastAestheticId
    }

    LaunchedEffect(activeAestheticId) {
        if (uiState is DuetUiState.Ready && activeAestheticId != langSettings.lastAestheticId) {
            languagePreferences.setLastAestheticId(activeAestheticId)
        }
    }

    DuetTheme(aestheticId = activeAestheticId) {
    when (val state = uiState) {
        DuetUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is DuetUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.bootstrap() }) {
                        Text(UiStrings.retry(language))
                    }
                }
            }
        }

        is DuetUiState.Ready -> {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showChrome = currentRoute in listOf(
                ROUTE_TOPICS,
                ROUTE_DRAW,
                ROUTE_TRIPS,
                ROUTE_JOURNAL,
                ROUTE_COUPLE
            )
            var showNotificationsSheet by remember { mutableStateOf(false) }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 600.dp

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (showChrome && !useRail) {
                            Column {
                                PartnerBottomNotificationBar(
                                    notifications = state.partnerNotifications,
                                    language = language,
                                    onClick = { unseen ->
                                        if (unseen.size == 1) {
                                            val onlyItem = unseen.first()
                                            viewModel.markAnswerSeen(onlyItem.answer.id)
                                            navController.navigate("topic_detail/${onlyItem.deck.id}?questionId=${onlyItem.question.id}")
                                        } else {
                                            showNotificationsSheet = true
                                        }
                                    }
                                )
                                NavigationBar(
                                    windowInsets = WindowInsets.navigationBars,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    NavigationBarItem(
                                        selected = currentRoute == ROUTE_TOPICS,
                                        onClick = {
                                            navController.navigate(ROUTE_TOPICS) {
                                                popUpTo(ROUTE_TOPICS) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == ROUTE_TOPICS) Icons.Rounded.Style
                                                else Icons.Outlined.Style,
                                                contentDescription = UiStrings.tabTopics(language)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = UiStrings.tabTopics(language),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_topics")
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == ROUTE_DRAW,
                                        onClick = {
                                            navController.navigate(ROUTE_DRAW) {
                                                popUpTo(ROUTE_TOPICS) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == ROUTE_DRAW) Icons.Rounded.Brush
                                                else Icons.Outlined.Brush,
                                                contentDescription = UiStrings.tabDraw(language)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = UiStrings.tabDraw(language),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_draw")
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == ROUTE_TRIPS,
                                        onClick = {
                                            navController.navigate(ROUTE_TRIPS) {
                                                popUpTo(ROUTE_TOPICS) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == ROUTE_TRIPS) Icons.Rounded.PhotoLibrary
                                                else Icons.Outlined.PhotoLibrary,
                                                contentDescription = UiStrings.tabTrips(language)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = UiStrings.tabTrips(language),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_trips")
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == ROUTE_JOURNAL,
                                        onClick = {
                                            navController.navigate(ROUTE_JOURNAL) {
                                                popUpTo(ROUTE_TOPICS) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == ROUTE_JOURNAL) Icons.Rounded.AutoStories
                                                else Icons.Outlined.AutoStories,
                                                contentDescription = UiStrings.tabJournal(language)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = UiStrings.tabJournal(language),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_journal")
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == ROUTE_COUPLE,
                                        onClick = {
                                            navController.navigate(ROUTE_COUPLE) {
                                                popUpTo(ROUTE_TOPICS) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == ROUTE_COUPLE) Icons.Rounded.Favorite
                                                else Icons.Outlined.FavoriteBorder,
                                                contentDescription = UiStrings.tabCouple(language)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = UiStrings.tabCouple(language),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_couple")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (showChrome && useRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                NavigationRailItem(
                                    selected = currentRoute == ROUTE_TOPICS,
                                    onClick = {
                                        navController.navigate(ROUTE_TOPICS) {
                                            popUpTo(ROUTE_TOPICS) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == ROUTE_TOPICS) Icons.Rounded.Style
                                            else Icons.Outlined.Style,
                                            contentDescription = UiStrings.tabTopics(language)
                                        )
                                    },
                                    label = { Text(UiStrings.tabTopics(language)) }
                                )
                                NavigationRailItem(
                                    selected = currentRoute == ROUTE_DRAW,
                                    onClick = {
                                        navController.navigate(ROUTE_DRAW) {
                                            popUpTo(ROUTE_TOPICS) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == ROUTE_DRAW) Icons.Rounded.Brush
                                            else Icons.Outlined.Brush,
                                            contentDescription = UiStrings.tabDraw(language)
                                        )
                                    },
                                    label = { Text(UiStrings.tabDraw(language)) }
                                )
                                NavigationRailItem(
                                    selected = currentRoute == ROUTE_TRIPS,
                                    onClick = {
                                        navController.navigate(ROUTE_TRIPS) {
                                            popUpTo(ROUTE_TOPICS) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == ROUTE_TRIPS) Icons.Rounded.PhotoLibrary
                                            else Icons.Outlined.PhotoLibrary,
                                            contentDescription = UiStrings.tabTrips(language)
                                        )
                                    },
                                    label = { Text(UiStrings.tabTrips(language)) },
                                    modifier = Modifier.testTag("nav_rail_trips")
                                )
                                NavigationRailItem(
                                    selected = currentRoute == ROUTE_JOURNAL,
                                    onClick = {
                                        navController.navigate(ROUTE_JOURNAL) {
                                            popUpTo(ROUTE_TOPICS) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == ROUTE_JOURNAL) Icons.Rounded.AutoStories
                                            else Icons.Outlined.AutoStories,
                                            contentDescription = UiStrings.tabJournal(language)
                                        )
                                    },
                                    label = { Text(UiStrings.tabJournal(language)) }
                                )
                                NavigationRailItem(
                                    selected = currentRoute == ROUTE_COUPLE,
                                    onClick = {
                                        navController.navigate(ROUTE_COUPLE) {
                                            popUpTo(ROUTE_TOPICS) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == ROUTE_COUPLE) Icons.Rounded.Favorite
                                            else Icons.Outlined.FavoriteBorder,
                                            contentDescription = UiStrings.tabCouple(language)
                                        )
                                    },
                                    label = { Text(UiStrings.tabCouple(language)) }
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_TOPICS,
                                modifier = Modifier.weight(1f)
                            ) {
                                composable(ROUTE_TOPICS) {
                                    TopicsScreen(
                                        state = state,
                                        currentUserId = currentUserId,
                                        language = language,
                                        showSecondary = showSecondary,
                                        isBusy = isBusy,
                                        onSelectLanguage = viewModel::setPrimaryLanguage,
                                        onToggleSecondary = viewModel::toggleSecondaryLanguage,
                                        onShufflePrompt = viewModel::shuffleDailyPrompt,
                                        onOpenTopic = { topicId, questionId ->
                                            val route = if (questionId != null) {
                                                "topic_detail/$topicId?questionId=$questionId"
                                            } else {
                                                "topic_detail/$topicId"
                                            }
                                            navController.navigate(route)
                                        },
                                        onAddCustomQuestion = viewModel::addCustomQuestion,
                                        onUpdateNextReunion = { dateKey, location ->
                                            viewModel.updateNextReunion(
                                                dateKey,
                                                location,
                                                UiStrings.savedSuccess(language)
                                            )
                                        },
                                        onUpdateRelationshipStartDate = { dateKey ->
                                            viewModel.updateRelationshipStartDate(
                                                dateKey = dateKey,
                                                savedText = UiStrings.relationshipDateSaved(language)
                                            )
                                        },
                                        onUpdateMyProfileIdentity = { name, avatarId ->
                                            viewModel.updateMyProfileIdentity(
                                                displayName = name,
                                                avatarId = avatarId,
                                                savedText = UiStrings.avatarUpdatedSuccess(language)
                                            )
                                        },
                                        onUpdateAesthetic = { aestheticId ->
                                            viewModel.updateCoupleAesthetic(
                                                aestheticThemeId = aestheticId,
                                                savedText = UiStrings.aestheticUpdatedSuccess(language)
                                            )
                                        }
                                    )
                                }

                                composable(ROUTE_DRAW) {
                                    DrawScreen(
                                        state = state,
                                        currentUserId = currentUserId,
                                        language = language,
                                        isBusy = isBusy,
                                        onSaveDrawing = { dateKey, speakerRole, authorLabel, caption, encodedStrokes, existing ->
                                            viewModel.saveDrawing(
                                                dateKey = dateKey,
                                                speakerRole = speakerRole,
                                                authorLabel = authorLabel,
                                                caption = caption,
                                                encodedStrokes = encodedStrokes,
                                                existingDrawing = existing,
                                                successMsg = UiStrings.drawingSaved(language)
                                            )
                                        },
                                        onReactToDrawing = viewModel::reactToDrawing,
                                        onDeleteDrawing = viewModel::deleteDrawing
                                    )
                                }

                                composable(ROUTE_TRIPS) {
                                    TripsScreen(
                                        state = state,
                                        language = language,
                                        isBusy = isBusy,
                                        onOpenTrip = { tripId ->
                                            navController.navigate("trip_detail/$tripId")
                                        },
                                        onSaveTrip = { title, location, startDateKey, endDateKey, notes, existing, onCreatedTripId ->
                                            viewModel.saveTrip(
                                                title = title,
                                                location = location,
                                                startDateKey = startDateKey,
                                                endDateKey = endDateKey,
                                                notes = notes,
                                                existingTrip = existing,
                                                savedText = UiStrings.tripSavedSuccess(language),
                                                onSavedId = onCreatedTripId
                                            )
                                        },
                                        onUpdatePhoto = { photoId, caption, takenDateKey, takenTimeLabel, takenMillis, timestampSource ->
                                            viewModel.updateTripPhoto(
                                                photoId = photoId,
                                                caption = caption,
                                                takenDateKey = takenDateKey,
                                                takenTimeLabel = takenTimeLabel,
                                                takenMillis = takenMillis,
                                                timestampSource = timestampSource,
                                                savedMsg = UiStrings.savedSuccess(language)
                                            )
                                        },
                                        onReactToPhoto = viewModel::reactToTripPhoto,
                                        onDeletePhoto = viewModel::deleteTripPhoto,
                                        onDeletePhotos = { photoIds ->
                                            viewModel.deleteTripPhotos(
                                                photoIds = photoIds,
                                                successMessageBuilder = { count ->
                                                    UiStrings.photosDeletedSuccess(count, language)
                                                }
                                            )
                                        }
                                    )
                                }

                                composable(ROUTE_JOURNAL) {
                                    JournalScreen(
                                        state = state,
                                        currentUserId = currentUserId,
                                        language = language,
                                        showSecondary = showSecondary,
                                        onOpenQuestion = { topicId, questionId ->
                                            navController.navigate("topic_detail/$topicId?questionId=$questionId")
                                        },
                                        onReactToAnswer = viewModel::reactToAnswer
                                    )
                                }

                                composable(ROUTE_COUPLE) {
                                    CoupleSpaceScreen(
                                        state = state,
                                        currentUserId = currentUserId,
                                        language = language,
                                        showSecondary = showSecondary,
                                        isBusy = isBusy,
                                        onSelectLanguage = viewModel::setPrimaryLanguage,
                                        onToggleSecondary = viewModel::toggleSecondaryLanguage,
                                        onUpdateMyProfileIdentity = { name, avatarId ->
                                            viewModel.updateMyProfileIdentity(
                                                displayName = name,
                                                avatarId = avatarId,
                                                savedText = UiStrings.avatarUpdatedSuccess(language)
                                            )
                                        },
                                        onUpdateNextReunion = { dateKey, location ->
                                            viewModel.updateNextReunion(
                                                dateKey,
                                                location,
                                                UiStrings.savedSuccess(language)
                                            )
                                        },
                                        onUpdateRelationshipStartDate = { dateKey ->
                                            viewModel.updateRelationshipStartDate(
                                                dateKey = dateKey,
                                                savedText = UiStrings.relationshipDateSaved(language)
                                            )
                                        },
                                        onUpdateAesthetic = { aestheticId ->
                                            viewModel.updateCoupleAesthetic(
                                                aestheticThemeId = aestheticId,
                                                savedText = UiStrings.aestheticUpdatedSuccess(language)
                                            )
                                        },
                                        onJoinByInviteCode = viewModel::joinPartnerSpace,
                                        onShowMessage = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        },
                                        onSignOut = {
                                            AuthHelper.signOut(credentialManager, scope)
                                        }
                                    )
                                }

                                composable(
                                    route = ROUTE_TOPIC_DETAIL,
                                    arguments = listOf(
                                        navArgument("topicId") { type = NavType.StringType },
                                        navArgument("questionId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val topicId = backStackEntry.arguments?.getString("topicId") ?: "sparks"
                                    val questionId = backStackEntry.arguments?.getString("questionId")
                                    TopicDetailScreen(
                                        state = state,
                                        topicId = topicId,
                                        initialQuestionId = questionId,
                                        currentUserId = currentUserId,
                                        language = language,
                                        showSecondary = showSecondary,
                                        isBusy = isBusy,
                                        onSelectLanguage = viewModel::setPrimaryLanguage,
                                        onToggleSecondary = viewModel::toggleSecondaryLanguage,
                                        onBack = { navController.popBackStack() },
                                        onSaveAnswer = { q, role, label, text, existing ->
                                            viewModel.saveAnswer(
                                                question = q,
                                                speakerRole = role,
                                                authorLabel = label,
                                                text = text,
                                                existingAnswer = existing,
                                                successMsg = UiStrings.savedSuccess(language)
                                            )
                                        },
                                        onReactToAnswer = viewModel::reactToAnswer,
                                        onAddCustomQuestion = viewModel::addCustomQuestion,
                                        onMarkAnswerSeen = viewModel::markAnswerSeen
                                    )
                                }

                                composable(
                                    route = ROUTE_TRIP_DETAIL,
                                    arguments = listOf(
                                        navArgument("tripId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                                    TripDetailScreen(
                                        state = state,
                                        tripId = tripId,
                                        currentUserId = currentUserId,
                                        language = language,
                                        isBusy = isBusy,
                                        onBack = { navController.popBackStack() },
                                        onUpdateTrip = { title, location, startDateKey, endDateKey, notes, existing ->
                                            viewModel.saveTrip(
                                                title = title,
                                                location = location,
                                                startDateKey = startDateKey,
                                                endDateKey = endDateKey,
                                                notes = notes,
                                                existingTrip = existing,
                                                savedText = UiStrings.tripSavedSuccess(language)
                                            )
                                        },
                                        onDeleteTrip = viewModel::deleteTrip,
                                        onAddPhotos = { id, uris, authorLabel ->
                                            viewModel.addTripPhotos(
                                                tripId = id,
                                                uris = uris,
                                                authorLabel = authorLabel,
                                                successMessageBuilder = { count ->
                                                    UiStrings.photosAddedSuccess(count, language)
                                                }
                                            )
                                        },
                                        onUpdatePhoto = { photoId, caption, takenDateKey, takenTimeLabel, takenMillis, timestampSource ->
                                            viewModel.updateTripPhoto(
                                                photoId = photoId,
                                                caption = caption,
                                                takenDateKey = takenDateKey,
                                                takenTimeLabel = takenTimeLabel,
                                                takenMillis = takenMillis,
                                                timestampSource = timestampSource,
                                                savedMsg = UiStrings.savedSuccess(language)
                                            )
                                        },
                                        onReactToPhoto = viewModel::reactToTripPhoto,
                                        onDeletePhoto = viewModel::deleteTripPhoto,
                                        onDeletePhotos = { photoIds ->
                                            viewModel.deleteTripPhotos(
                                                photoIds = photoIds,
                                                successMessageBuilder = { count ->
                                                    UiStrings.photosDeletedSuccess(count, language)
                                                }
                                            )
                                        }
                                    )
                                }
                            }

                            if (showChrome && useRail) {
                                PartnerBottomNotificationBar(
                                    notifications = state.partnerNotifications,
                                    language = language,
                                    onClick = { unseen ->
                                        if (unseen.size == 1) {
                                            val onlyItem = unseen.first()
                                            viewModel.markAnswerSeen(onlyItem.answer.id)
                                            navController.navigate("topic_detail/${onlyItem.deck.id}?questionId=${onlyItem.question.id}")
                                        } else {
                                            showNotificationsSheet = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (showNotificationsSheet) {
                    PartnerNotificationsSheet(
                        notifications = state.partnerNotifications,
                        coupleSpace = state.coupleSpace,
                        language = language,
                        showSecondary = showSecondary,
                        onDismiss = { showNotificationsSheet = false },
                        onOpenQuestion = { item ->
                            viewModel.markAnswerSeen(item.answer.id)
                            showNotificationsSheet = false
                            navController.navigate("topic_detail/${item.deck.id}?questionId=${item.question.id}")
                        },
                        onMarkAllRead = viewModel::markAllAnswersSeen
                    )
                }

                val unconfirmedProfile = state.userProfile?.takeIf { !it.nameConfirmed }
                if (unconfirmedProfile != null) {
                    AvatarPickerDialog(
                        initialName = unconfirmedProfile.displayName,
                        currentAvatarId = unconfirmedProfile.avatarId,
                        language = language,
                        isFirstJoin = true,
                        isBusy = isBusy,
                        onDismiss = {},
                        onSave = { name, avatarId ->
                            viewModel.updateMyProfileIdentity(
                                displayName = name,
                                avatarId = avatarId,
                                savedText = UiStrings.avatarUpdatedSuccess(language)
                            )
                        }
                    )
                }
            }
        }
    }
    }
}
