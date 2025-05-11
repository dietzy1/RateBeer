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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.grp30.ratebeer.R
import dk.grp30.ratebeer.data.api.Beer
import dk.grp30.ratebeer.data.api.UntappdApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateBeerScreen(
    groupId: String,
    beerId: String,
    onVoteSubmitted: (Int) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var beer by remember { mutableStateOf<Beer?>(null) }
    var rating by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // For demonstration purposes, use sample data
    LaunchedEffect(beerId) {
        // In a real app, fetch beer details from the API
        try {
            // Simulate network delay
            kotlinx.coroutines.delay(1000)
            
            // Sample data for demo purposes
            beer = when (beerId) {
                "1" -> Beer(
                    id = "1",
                    name = "Heineken",
                    brewery = "Heineken International",
                    style = "Pale Lager",
                    abv = 5.0,
                    rating = 3.2,
                    imageUrl = "https://example.com/heineken.jpg"
                )
                "2" -> Beer(
                    id = "2",
                    name = "Guinness Draught",
                    brewery = "Guinness",
                    style = "Irish Dry Stout",
                    abv = 4.2,
                    rating = 4.1,
                    imageUrl = "https://example.com/guinness.jpg"
                )
                "3" -> Beer(
                    id = "3",
                    name = "Corona Extra",
                    brewery = "Grupo Modelo",
                    style = "Pale Lager",
                    abv = 4.5,
                    rating = 3.0,
                    imageUrl = "https://example.com/corona.jpg"
                )
                "4" -> Beer(
                    id = "4",
                    name = "Sierra Nevada Pale Ale",
                    brewery = "Sierra Nevada Brewing Co.",
                    style = "American Pale Ale",
                    abv = 5.6,
                    rating = 4.3,
                    imageUrl = "https://example.com/sierra.jpg"
                )
                "5" -> Beer(
                    id = "5",
                    name = "Duvel",
                    brewery = "Duvel Moortgat",
                    style = "Belgian Strong Golden Ale",
                    abv = 8.5,
                    rating = 4.5,
                    imageUrl = "https://example.com/duvel.jpg"
                )
                else -> null
            }
            
            if (beer == null) {
                throw Exception("Beer not found")
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading beer: ${e.message}")
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
                        // In a real app, load image from URL
                        // For demo, use placeholder
                        Image(
                            painter = painterResource(id = R.drawable.beer_placeholder),
                            contentDescription = beer?.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
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
                    
                    // Brewery
                    Text(
                        text = beer?.brewery ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Beer details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Style: ${beer?.style}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "ABV: ${String.format("%.1f", beer?.abv)}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Untappd rating
                    Text(
                        text = "Untappd Rating: ${String.format("%.1f", beer?.rating)}/5",
                        style = MaterialTheme.typography.bodyMedium
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
                            // Submit the rating when a star is clicked
                            onVoteSubmitted(newRating)
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
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