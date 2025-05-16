package dk.grp30.ratebeer.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.grp30.ratebeer.R
import dk.grp30.ratebeer.viewmodel.MainViewModel
import dk.grp30.ratebeer.viewmodel.MainNavEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToLobby: (groupId: String) -> Unit,
    onNavigateToWelcome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var groupCodeInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel.navEvent) {
        viewModel.navEvent.collectLatest { event ->
            when (event) {
                is MainNavEvent.ToLobby -> onNavigateToLobby(event.groupId)
                is MainNavEvent.ToWelcome -> onNavigateToWelcome()
            }
        }
    }

    LaunchedEffect(key1 = viewModel.uiMessage) {
        viewModel.uiMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.checkUserLoggedIn()
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalDrink,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RateBeer", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = colorScheme.primary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(colorScheme.background, colorScheme.surface)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(colorScheme.primary).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocalDrink, "RateBeer Logo", tint = colorScheme.onPrimary, modifier = Modifier.size(60.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Welcome to RateBeer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                Text("Social Beer Tasting Experience", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(32.dp))

                // Create group card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(colorScheme.primaryContainer).padding(8.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Add, null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Create a Group Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                Text("Host a new beer tasting event", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.createGroup() }, // Call ViewModel function
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !uiState.isLoadingCreate, // Use loading state from ViewModel
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
                        ) {
                            if (uiState.isLoadingCreate) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Rounded.Groups, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create New Group", fontSize = 16.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = colorScheme.outlineVariant)
                    Text(" OR ", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
                    Divider(modifier = Modifier.weight(1f), color = colorScheme.outlineVariant)
                }

                // Join group card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(colorScheme.secondaryContainer).padding(8.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Numbers, null, tint = colorScheme.secondary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Join a Group Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                Text("Enter 6-digit group code to join", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = groupCodeInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    groupCodeInput = it
                                }
                            },
                            label = { Text("Enter 6-digit code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (groupCodeInput.length == 6) {
                                    Icon(Icons.Default.Info, null, tint = colorScheme.primary)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.joinGroup(groupCodeInput) }, // Call ViewModel function
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = groupCodeInput.length == 6 && !uiState.isLoadingJoin, // Use loading state
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.secondary,
                                contentColor = colorScheme.onSecondary,
                                disabledContainerColor = colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                disabledContentColor = colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            if (uiState.isLoadingJoin) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                            } else {
                                Text("Join Group", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, "Join", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Added some bottom spacing
            }
        }
    }
}