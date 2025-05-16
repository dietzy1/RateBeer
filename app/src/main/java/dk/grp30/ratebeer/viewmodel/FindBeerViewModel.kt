package dk.grp30.ratebeer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.models.Beer
import dk.grp30.ratebeer.data.firestore.GroupRepository
import dk.grp30.ratebeer.data.firestore.DataResult
import dk.grp30.ratebeer.ui.navigation.RateBeerDestinations
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FindBeerScreenState(
    val isSearching: Boolean = false,
    val searchResults: List<Beer> = emptyList(), // <<<<---- Uses canonical Beer
    val initialMessage: String = "Enter a beer name, brewery, or style to discover your next favorite brew",
    val noResultsMessage: String? = null,
    val errorMessage: String? = null
)

sealed interface FindBeerNavEvent {
    object NavigateBack : FindBeerNavEvent
}

private object FindBeerMockDataSource {
    private val allMockBeers = listOf(
        Beer("1", "Heineken", "Heineken International", "Pale Lager", 5.0, 3.2, "https://example.com/heineken.jpg"),
        Beer("2", "Guinness Draught", "Guinness", "Irish Dry Stout", 4.2, 4.1, "https://example.com/guinness.jpg"),
        Beer("6", "Chimay Blue", "Bières de Chimay", "Belgian Dark Strong Ale", 9.0, 4.3, "url"),
        Beer("7", "Pliny the Elder", "Russian River Brewing Company", "American Double IPA", 8.0, 4.7, "url"),
        Beer("8", "Weihenstephaner Hefeweissbier", "Bayerische Staatsbrauerei Weihenstephan", "Hefeweizen", 5.4, 4.2, "url")

    )

    suspend fun searchBeers(query: String): List<Beer> {
        delay(1000)
        if (query.isBlank()) return emptyList()
        return allMockBeers.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.brewery.contains(query, ignoreCase = true) ||
                    it.style.contains(query, ignoreCase = true)
        }
    }
}

@HiltViewModel
class FindBeerViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = savedStateHandle[RateBeerDestinations.ARG_GROUP_ID] ?: ""

    private val _uiState = MutableStateFlow(FindBeerScreenState()) // Uses original state
    val uiState: StateFlow<FindBeerScreenState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<FindBeerNavEvent>()
    val navEvent: SharedFlow<FindBeerNavEvent> = _navEvent.asSharedFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false, noResultsMessage = null)
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true, noResultsMessage = null, errorMessage = null)
        searchJob = viewModelScope.launch {
            try {
                val results = FindBeerMockDataSource.searchBeers(query)
                _uiState.value = if (results.isEmpty()) {
                    _uiState.value.copy(isSearching = false, searchResults = emptyList(), noResultsMessage = "No beers found matching '$query'")
                } else {
                    _uiState.value.copy(isSearching = false, searchResults = results, noResultsMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, errorMessage = "Error searching: ${e.message}")
            }
        }
    }

    fun onBeerSelected(beer: Beer) {
        if (groupId.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Group ID is missing. Cannot select beer.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            val result = groupRepository.setTastingBeer(groupId, beer.id, beer.name)
            when (result) {
                is DataResult.Success -> {
                    _navEvent.emit(FindBeerNavEvent.NavigateBack)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSearching = false, errorMessage = "Failed to select beer: ${result.message}")
                }
                DataResult.Loading -> { }
            }
            if (_uiState.value.isSearching) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}