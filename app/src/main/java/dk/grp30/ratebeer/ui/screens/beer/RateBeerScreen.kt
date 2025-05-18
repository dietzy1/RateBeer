package dk.grp30.ratebeer.ui.screens.beer

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.grp30.ratebeer.R
import dk.grp30.ratebeer.data.api.Beer
import dk.grp30.ratebeer.data.api.PunkApiService
import dk.grp30.ratebeer.data.firestore.BeerRatingRepository
import kotlinx.coroutines.launch
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateBeerScreen(
    groupId: String,
    beerId: String,
    beerRatingRepository: BeerRatingRepository,
    onVoteSubmitted: (Int) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var beer by remember { mutableStateOf<Beer?>(null) }
    var rating by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }

    // Convert beerId from String to Int for API call
    LaunchedEffect(beerId) {
        isLoading = true
        errorMessage = null

        try {
            // Parse beerId to Int
            val beerIdInt = beerId.toIntOrNull()

            if (beerIdInt == null) {
                throw IllegalArgumentException("Invalid beer ID format")
            }

            // Fetch beer details from API
            val result = PunkApiService.getBeerById(beerIdInt)

            result.fold(
                onSuccess = { fetchedBeer ->
                    beer = fetchedBeer
                },
                onFailure = { exception ->
                    errorMessage = "Failed to load beer: ${exception.message}"
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(errorMessage ?: "Unknown error occurred")
                    }
                }
            )
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMessage ?: "Unknown error occurred")
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Beer") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                // Loading state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading beer details...")
                }
            } else if (errorMessage != null) {
                // Error state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (beer != null) {
                // Beer details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Beer image
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (beer?.image != null) {
                            // Load image from URL using Coil
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(beer?.getFormattedImageUrl())
                                        .build()
                                ),
                                contentDescription = beer?.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // Fallback placeholder image
                            Image(
                                painter = painterResource(id = R.drawable.beer_placeholder),
                                contentDescription = beer?.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Beer name
                    Text(
                        text = beer?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tagline
                    Text(
                        text = beer?.tagline ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Beer details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "First Brewed: ${beer?.first_brewed}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "ABV: ${String.format("%.1f", beer?.abv)}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (beer?.ibu != null || beer?.ebc != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (beer?.ibu != null) {
                                Text(
                                    text = "IBU: ${String.format("%.1f", beer?.ibu)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            if (beer?.ebc != null) {
                                Text(
                                    text = "EBC: ${String.format("%.1f", beer?.ebc)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = beer?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Rating section
                    Text(
                        text = "Your Rating",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Star rating
                    StarRating(
                        rating = rating,
                        onRatingChanged = { newRating ->
                            rating = newRating
                            isSubmitting = true
                            coroutineScope.launch {
                                val result = beerRatingRepository.submitRating(groupId, beerId, newRating)
                                isSubmitting = false
                                if (result is dk.grp30.ratebeer.data.firestore.RatingResult.Success) {
                                    onVoteSubmitted(newRating)
                                } else if (result is dk.grp30.ratebeer.data.firestore.RatingResult.Error) {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (isSubmitting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating text
                    Text(
                        text = when (rating) {
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
                }
            }
        }
    }
}

@Composable
fun StarRating(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Int = 48,
    maxRating: Int = 5
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            IconButton(
                onClick = { onRatingChanged(i) },
                modifier = Modifier.size(starSize.dp)
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) Color(0xFFAB7B00) else Color.Gray,
                    modifier = Modifier.size(starSize.dp)
                )
            }
        }
    }
}