package dk.grp30.ratebeer.ui.screens.beer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dk.grp30.ratebeer.data.api.Beer
import dk.grp30.ratebeer.data.api.UntappdApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindBeerScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onBeerSelected: (Beer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Beer>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // For demo, let's populate some sample beers if the API isn't available yet
    val sampleBeers = remember {
        listOf(
            Beer(
                id = "1",
                name = "Heineken",
                brewery = "Heineken International",
                style = "Pale Lager",
                abv = 5.0,
                rating = 3.2,
                imageUrl = "https://example.com/heineken.jpg"
            ),
            Beer(
                id = "2",
                name = "Guinness Draught",
                brewery = "Guinness",
                style = "Irish Dry Stout",
                abv = 4.2,
                rating = 4.1,
                imageUrl = "https://example.com/guinness.jpg"
            ),
            Beer(
                id = "3",
                name = "Corona Extra",
                brewery = "Grupo Modelo",
                style = "Pale Lager",
                abv = 4.5,
                rating = 3.0,
                imageUrl = "https://example.com/corona.jpg"
            ),
            Beer(
                id = "4",
                name = "Sierra Nevada Pale Ale",
                brewery = "Sierra Nevada Brewing Co.",
                style = "American Pale Ale",
                abv = 5.6,
                rating = 4.3,
                imageUrl = "https://example.com/sierra.jpg"
            ),
            Beer(
                id = "5",
                name = "Duvel",
                brewery = "Duvel Moortgat",
                style = "Belgian Strong Golden Ale",
                abv = 8.5,
                rating = 4.5,
                imageUrl = "https://example.com/duvel.jpg"
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Beer to Rate") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search for beers") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            isSearching = true
                            keyboardController?.hide()
                            
                            // Use Untappd API service to search for beers
                            coroutineScope.launch {
                                try {
                                    // In a real app, this would be a real API call
                                    // For demonstration, we're using sample data
                                    // val result = UntappdApiService.searchBeer(searchQuery)
                                    // if (result.isSuccess) {
                                    //     searchResults = result.getOrNull() ?: emptyList()
                                    // } else {
                                    //     throw Exception("Failed to search")
                                    // }
                                    
                                    // For demo, just filter sample beers
                                    kotlinx.coroutines.delay(1000) // Simulate network delay
                                    searchResults = sampleBeers.filter { 
                                        it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.brewery.contains(searchQuery, ignoreCase = true) ||
                                        it.style.contains(searchQuery, ignoreCase = true)
                                    }
                                    
                                    if (searchResults.isEmpty()) {
                                        snackbarHostState.showSnackbar("No beers found matching '$searchQuery'")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error searching: ${e.message}")
                                } finally {
                                    isSearching = false
                                }
                            }
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search results
            if (!isSearching) {
                if (searchResults.isEmpty() && searchQuery.isBlank()) {
                    // Initial state
                    Text(
                        text = "Enter a beer name, brewery, or style to search",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                    // No results
                    Text(
                        text = "No beers found matching '$searchQuery'",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    // Show results
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchResults) { beer ->
                            BeerListItem(
                                beer = beer,
                                onClick = { onBeerSelected(beer) }
                            )
                            Divider()
                        }
                    }
                }
            } else {
                // Show loading indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Searching for beers...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun BeerListItem(
    beer: Beer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = beer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = beer.brewery,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Rating: ${String.format("%.1f", beer.rating)}/5",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = beer.style,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow
                        .Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "ABV: ${String.format("%.1f", beer.abv)}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 