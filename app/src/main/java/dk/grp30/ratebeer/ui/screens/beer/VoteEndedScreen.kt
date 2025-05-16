package dk.grp30.ratebeer.ui.screens.beer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dk.grp30.ratebeer.R
import dk.grp30.ratebeer.ui.navigation.RateBeerDestinations
import dk.grp30.ratebeer.viewmodel.VoteEndedViewModel
import dk.grp30.ratebeer.viewmodel.VoteEndedNavEvent
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteEndedScreen(
    navController: NavController, // For handling navigation events
    viewModel: VoteEndedViewModel = hiltViewModel()
    // groupId: String, // ViewModel gets these from SavedStateHandle
    // beerId: String,
    // onRateNextBeer: () -> Unit, // Handled by ViewModel nav event
    // onLeaveGroup: () -> Unit    // Handled by ViewModel nav event
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Or your preferred working collector
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe navigation events from ViewModel
    LaunchedEffect(key1 = viewModel.navEvent) {
        viewModel.navEvent.collectLatest { event ->
            when (event) {
                is VoteEndedNavEvent.ToFindBeer -> {
                    navController.navigate(RateBeerDestinations.findBeerRoute(event.groupId)) {
                        // Pop this VoteEndedScreen instance from the back stack
                        popUpTo(RateBeerDestinations.voteEndedRoute(viewModel.groupId, viewModel.beerId)) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is VoteEndedNavEvent.ToMain -> {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    // Observe error messages for Snackbar (from ViewModel's uiState.errorMessage)
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message: String ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage() // Clear error in ViewModel after showing
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Vote Results") })
            // TODO: Add NavigationIcon for back if desired, e.g., to Lobby or Main
            // This could call viewModel.onLeaveGroupClicked() or just navController.popBackStack()
            // For now, "Leave Group" button is at the bottom.
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading results...")
                }
            } else if (uiState.errorMessage != null && uiState.beer == null) {
                // Error when beer details couldn't be loaded (e.g. "Beer not found")
                Text(
                    text = "Could not load beer details: ${uiState.errorMessage}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.beer != null) {
                val beer = uiState.beer!! // Safe due to the null check
                val groupRatingDisplay = uiState.groupRating // This is Double?
                val publicRatingDisplay = beer.rating // This is Double from Beer model

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        // TODO: Replace with Coil or Glide for beer.imageUrl if it's a real URL
                        Image(
                            painter = painterResource(id = R.drawable.beer_placeholder),
                            contentDescription = beer.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(beer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rating Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))

                            // Group rating
                            if (groupRatingDisplay != null) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Group Rating:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                    RatingDisplay(rating = groupRatingDisplay, starSize = 24)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(String.format(Locale.US, "%.1f", groupRatingDisplay), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            } else if (uiState.errorMessage != null && uiState.groupRating == null) {
                                // Specific error for group rating if beer details loaded but group rating didn't
                                Text(
                                    "Could not load group rating: ${uiState.errorMessage}", // Show the specific error
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                // Generic "not available" if no specific error and no rating
                                Text(
                                    "Group rating not available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                )
                            }

                            // Public ("Untappd") rating
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Public Rating:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                RatingDisplay(rating = publicRatingDisplay, starSize = 24)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(String.format(Locale.US, "%.1f", publicRatingDisplay), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Comparison text (only if group rating is available)
                            if (groupRatingDisplay != null) {
                                val difference = (groupRatingDisplay - publicRatingDisplay).roundTo(1)
                                val comparisonText = when {
                                    difference > 0.5 -> "Your group rated this beer higher than the public average!"
                                    difference < -0.5 -> "Your group rated this beer lower than the public average."
                                    else -> "Your group's rating is similar to the public average."
                                }
                                Text(comparisonText, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to bottom
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(onClick = { viewModel.onLeaveGroupClicked() }, modifier = Modifier.weight(1f)) {
                            Text("Leave Group")
                        }
                        Button(onClick = { viewModel.onRateNextBeerClicked() }, modifier = Modifier.weight(1f)) {
                            Text("Rate Next Beer")
                        }
                    }
                }
            } else {
                // This case implies beer is null, not loading, and no specific errorMessage about beer loading failed.
                // ViewModel's init logic should set an error if IDs are blank.
                Text("No results available or an error occurred.", textAlign = TextAlign.Center)
            }
        }
    }
}

// Your RatingDisplay and roundTo extension functions (ensure they are defined or imported)
@Composable
fun RatingDisplay(
    rating: Double,
    starSize: Int = 24,
    maxRating: Int = 5
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            val starFill = minOf(max(rating - (i - 1), 0.0), 1.0)
            if (starFill == 1.0) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFAA00),
                    modifier = Modifier.size(starSize.dp)
                )
            } else if (starFill == 0.0) {
                Icon(
                    imageVector = Icons.Outlined.Star, // Using outlined star from Material Icons
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(starSize.dp)
                )
            } else { // Partial star
                Box(modifier = Modifier.size(starSize.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(starFill.toFloat())
                            .clip(RectangleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFAA00),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

fun Double.roundTo(decimals: Int): Double {
    if (decimals < 0) throw IllegalArgumentException("Decimal places must be non-negative")
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToInt() / multiplier
}