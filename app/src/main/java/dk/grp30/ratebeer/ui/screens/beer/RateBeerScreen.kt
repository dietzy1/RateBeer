package dk.grp30.ratebeer.ui.screens.beer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import dk.grp30.ratebeer.viewmodel.RateBeerNavEvent
import dk.grp30.ratebeer.viewmodel.RateBeerViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateBeerScreen(
    navController: NavController,
    viewModel: RateBeerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe navigation events
    LaunchedEffect(key1 = viewModel.navEvent) {
        viewModel.navEvent.collectLatest { event ->
            when (event) {
                is RateBeerNavEvent.ToVoteEnded -> {
                    navController.navigate(RateBeerDestinations.voteEndedRoute(event.groupId, event.beerId)) {
                        popUpTo(RateBeerDestinations.rateBeerRoute(viewModel.groupId, viewModel.beerId)) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    // Observe snackbar messages
    LaunchedEffect(key1 = viewModel.snackbarMessage) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.beer != null) "Rate: ${uiState.beer?.name}" else "Rate Beer") }
                // TODO: Add NavigationIcon for back if desired, e.g., navController.popBackStack()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoadingBeer) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading beer details...")
                }
            } else if (uiState.beer != null) {
                val beer = uiState.beer!!

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.beer_placeholder),
                            contentDescription = beer.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(beer.name, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(beer.brewery, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Style: ${beer.style}",
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("ABV: ${String.format(Locale.US, "%.1f", beer.abv)}%",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Public Rating: ${String.format(Locale.US, "%.1f", beer.rating)}/5",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Your Rating", style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    StarRatingInput(
                        currentRating = uiState.selectedRating,
                        onRatingSelected = { newRating ->
                            viewModel.onRatingChanged(newRating)
                        },
                        enabled = !uiState.isSubmittingRating,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (uiState.selectedRating) {
                            0 -> "Tap a star to rate"
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Very Good"
                            5 -> "Excellent"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    if (uiState.isSubmittingRating) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                    uiState.submissionErrorMessage?.let { errorMsg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Text("Could not load beer details.", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun StarRatingInput(
    currentRating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Int = 48,
    maxRating: Int = 5,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            IconButton(
                onClick = { if (enabled) onRatingSelected(i) },
                enabled = enabled,
                modifier = Modifier.size(starSize.dp)
            ) {
                Icon(
                    imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Star $i",
                    tint = if (i <= currentRating) Color(0xFFFFAA00) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(starSize.dp)
                )
            }
        }
    }
}