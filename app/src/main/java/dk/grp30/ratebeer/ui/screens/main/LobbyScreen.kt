package dk.grp30.ratebeer.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dk.grp30.ratebeer.ui.navigation.RateBeerDestinations
import dk.grp30.ratebeer.viewmodel.LobbyViewModel
import dk.grp30.ratebeer.viewmodel.LobbyUiState
import dk.grp30.ratebeer.viewmodel.LobbyNavEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    navController: NavController,
    viewModel: LobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel.navEvent) {
        viewModel.navEvent.collectLatest { event ->
            when (event) {
                is LobbyNavEvent.ToRateBeer -> {
                    navController.navigate(RateBeerDestinations.rateBeerRoute(event.groupId, event.beerId))
                }
                is LobbyNavEvent.ToResults -> {
                    navController.navigate(RateBeerDestinations.voteEndedRoute(event.groupId, event.beerId))
                }
                is LobbyNavEvent.ToFindBeer -> {
                    navController.navigate(RateBeerDestinations.findBeerRoute(viewModel.groupId))
                }
                is LobbyNavEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            delay(2000)
            showCopiedMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beer Tasting Lobby")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onLeaveGroupClicked() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Leave Group / Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        when (val currentUiState = uiState) {
            is LobbyUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LobbyUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error: ${currentUiState.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onLeaveGroupClicked() }) {
                        Text("Go Back")
                    }
                }
            }
            is LobbyUiState.GroupLeft -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("You have left the group.")
                }
            }
            is LobbyUiState.Success -> {
                val group = currentUiState.group
                val isHost = currentUiState.isCurrentUserHost

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Group PIN display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Group PIN", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                        .padding(horizontal = 24.dp, vertical = 12.dp) // Padding applied
                                        .border( // Border applied AFTER padding, correctly chained
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Text(
                                        text = group.pinCode, // Assuming 'group' is from your uiState.success
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(group.pinCode))
                                        showCopiedMessage = true
                                    }) {
                                        Icon(
                                            Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy PIN",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }


                            AnimatedVisibility(visible = showCopiedMessage) {
                                Text("PIN Copied!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Share this PIN with friends to join your tasting session", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { /* TODO: Implement share intent */ }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Icon(Icons.Outlined.Share, "Share")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share Invite")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Participants section
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Participants", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${group.participants.size}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = when(group.status) {
                                "lobby" -> if (isHost) "Ready to start?" else "Waiting for host..."
                                "tasting" -> "Tasting: ${group.currentBeerName ?: "..."}"
                                "results" -> "Results for: ${group.currentBeerName ?: "..."}"
                                else -> group.status.replaceFirstChar { it.titlecase() }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Participants list
                    if (group.participants.isEmpty()) {
                        Text(
                            "No participants yet. Share the PIN!",
                            modifier = Modifier.padding(16.dp).weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            itemsIndexed(group.participants, key = { _, p -> p.userId }) { index, participant ->
                                // Ensure ParticipantRow uses your actual Participant model from data.firestore
                                ParticipantRow(
                                    participant = participant,
                                    isHost = (participant.userId == group.hostId),
                                    index = index
                                )
                            }
                        }
                    }


                    // Action buttons
                    Spacer(modifier = Modifier.height(16.dp)) // Add some space before buttons
                    if (isHost && group.status == "lobby") {
                        Button(
                            onClick = { viewModel.onStartTastingSessionClicked() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Select Beer to Taste", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Search, "Search")
                        }
                    } else if (isHost && group.status == "tasting") {
                        Button(
                            onClick = { viewModel.onEndVotingAndShowResultsClicked() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("End Voting & Show Results", style = MaterialTheme.typography.titleMedium)
                        }
                    } else if (group.status == "lobby") {
                        Text(
                            "Waiting for the host to select a beer...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    // The "Leave Group" button is now the back arrow in TopAppBar
                    // You could add an explicit OutlinedButton for "Leave Group" here if desired.
                    // Example:
                    // OutlinedButton(onClick = { viewModel.onLeaveGroupClicked() }, modifier = Modifier.fillMaxWidth()) {
                    //     Text("Leave Group")
                    // }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// Make sure ParticipantRow Composable is defined and uses the correct Participant model
@Composable
fun ParticipantRow(participant: dk.grp30.ratebeer.data.firestore.Participant, isHost: Boolean, index: Int) {
    // Your existing ParticipantRow UI, using participant.userName, etc.
    // The animation logic can be kept.
    var visible by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        delay(index * 100L) // Staggered animation
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isHost)
                    colorScheme.secondaryContainer.copy(alpha = 0.7f)
                else
                    colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = participant.userName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isHost) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isHost) {
                        Text("Host", style = MaterialTheme.typography.bodySmall, color = colorScheme.primary)
                    }
                }
            }
        }
    }
}