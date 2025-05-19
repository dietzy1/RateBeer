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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import dk.grp30.ratebeer.R
import dk.grp30.ratebeer.data.api.Beer
import dk.grp30.ratebeer.data.api.PunkApiService
import dk.grp30.ratebeer.data.firestore.BeerRatingRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import dk.grp30.ratebeer.data.firestore.GroupRepository
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteEndedScreen(
    groupId: String,
    beerId: String,
    groupRepository: GroupRepository,
    beerRatingRepository: BeerRatingRepository,
    onRateNextBeer: () -> Unit,
    onLeaveGroup: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
) {
    val groupFlow = remember { groupRepository.observeGroup(groupId) }
    val group by groupFlow.collectAsState(initial = null)

    var isLoading by remember { mutableStateOf(true) }
    var beer by remember { mutableStateOf<Beer?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val groupRatingFlow = remember { beerRatingRepository.observeGroupRating(groupId, beerId) }
    val groupRatingState by groupRatingFlow.collectAsState(initial = null)
    val groupAverage = groupRatingState?.averageRating ?: 0.0

    var globalAverage by remember { mutableStateOf<Double?>(null) }

    // Redirect if displayed screen is not this one.
    LaunchedEffect(group?.displayedRoute) {
        if (group?.displayedRoute != null && group!!.displayedRoute != "") {
            val route = group!!.displayedRoute
            if (!route.startsWith("vote_ended")) {
                onNavigateToRoute(route)
            }
        }
    }


    // Fetch global average
    LaunchedEffect(beerId) {
        isLoading = true
        errorMessage = null
        try {
            val beerIdInt = beerId.toIntOrNull() ?: throw IllegalArgumentException("Invalid beer ID format")
            val result = PunkApiService.getBeerById(beerIdInt)
            result.fold(
                onSuccess = { fetchedBeer -> beer = fetchedBeer },
                onFailure = { exception ->
                    errorMessage = "Failed to load beer: ${exception.message}"
                    coroutineScope.launch { snackbarHostState.showSnackbar(errorMessage ?: "Unknown error occurred") }
                }
            )
            globalAverage = beerRatingRepository.getGlobalAverageForBeer(beerId)
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            coroutineScope.launch { snackbarHostState.showSnackbar(errorMessage ?: "Unknown error occurred") }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vote Results") }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading results...")
                }
            } else if (beer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (beer?.image != null) {
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
                        Image(
                            painter = painterResource(id = R.drawable.beer_placeholder),
                            contentDescription = beer?.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = beer?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Rating Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Group Rating:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                RatingDisplay(
                                    rating = groupAverage,
                                    starSize = 24
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = if (groupRatingState != null) String.format("%.2f", groupAverage) else "-",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Global Average:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                RatingDisplay(
                                    rating = globalAverage ?: 0.0,
                                    starSize = 24
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = globalAverage?.let { String.format("%.2f", it) } ?: "-",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val comparisonText = if (globalAverage != null && groupRatingState != null) {
                                val diff = groupAverage - globalAverage!!
                                when {
                                    diff > 0.5 -> "Your group rated this beer higher than the global average!"
                                    diff < -0.5 -> "Your group rated this beer lower than the global average."
                                    else -> "Your group's rating is similar to the global average."
                                }
                            } else ""

                            Text(
                                text = comparisonText,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onLeaveGroup,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Leave Group")
                        }

                        Button(
                            onClick = onRateNextBeer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rate Next Beer")
                        }
                    }
                }
            }
        }
    }
}

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
            val starFill = minOf(maxOf(rating - (i - 1), 0.0), 1.0)
            when (starFill) {
                1.0 -> {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFAB7B00),
                        modifier = Modifier.size(starSize.dp)
                    )
                }
                0.0 -> {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(starSize.dp)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.size(starSize.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize(starFill.toFloat())
                                .align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFAB7B00),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}