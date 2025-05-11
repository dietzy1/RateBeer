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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.grp30.ratebeer.R
import dk.grp30.ratebeer.data.api.Beer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteEndedScreen(
    groupId: String,
    beerId: String,
    onRateNextBeer: () -> Unit,
    onLeaveGroup: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var beer by remember { mutableStateOf<Beer?>(null) }
    var groupRating by remember { mutableStateOf(0.0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // For demonstration purposes, use sample data
    LaunchedEffect(beerId) {
        // In a real app, fetch beer details and group rating from Firebase
        try {
            // Simulate network delay
            kotlinx.coroutines.delay(1500)
            
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
            
            // Generate a random group rating for demo
            // In a real app, this would be fetched from Firestore
            groupRating = (Random.nextDouble() * 2 + 2).roundTo(1) // Random between 2 and 4
            
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading results: ${e.message}")
            }
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
                // Loading state
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
                // Results content
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
                            .height(180.dp)
                    ) {
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Results comparison
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
                            
                            // Group rating
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
                                    rating = groupRating,
                                    starSize = 24
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = String.format("%.1f", groupRating),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Untappd rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Untappd Rating:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                RatingDisplay(
                                    rating = beer?.rating ?: 0.0,
                                    starSize = 24
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = String.format("%.1f", beer?.rating),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Comparison text
                            val difference = (groupRating - (beer?.rating ?: 0.0)).roundTo(1)
                            val comparisonText = when {
                                difference > 0.5 -> "Your group rated this beer higher than the average Untappd user!"
                                difference < -0.5 -> "Your group rated this beer lower than the average Untappd user."
                                else -> "Your group's rating is similar to the Untappd community."
                            }
                            
                            Text(
                                text = comparisonText,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Action buttons
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
            if (starFill == 1.0) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFAB7B00),
                    modifier = Modifier.size(starSize.dp)
                )
            } else if (starFill == 0.0) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(starSize.dp)
                )
            } else {
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

fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToInt() / multiplier
} 