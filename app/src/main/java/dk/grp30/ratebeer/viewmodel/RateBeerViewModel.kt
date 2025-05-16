package dk.grp30.ratebeer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.firestore.BeerRatingRepository
import dk.grp30.ratebeer.data.firestore.RatingResult
import dk.grp30.ratebeer.data.models.Beer
import dk.grp30.ratebeer.ui.navigation.RateBeerDestinations
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for RateBeerScreen
data class RateBeerScreenState(
    val isLoadingBeer: Boolean = true,
    val beer: Beer? = null,
    val selectedRating: Int = 0,
    val isSubmittingRating: Boolean = false,
    val submissionErrorMessage: String? = null
)


sealed interface RateBeerNavEvent {
    data class ToVoteEnded(val groupId: String, val beerId: String) : RateBeerNavEvent
}


private object RateBeerMockDataSource {
    private val mockBeers = mapOf(
        "1" to Beer("1", "Heineken", "Heineken International", "Pale Lager", 5.0, 3.2, "https://example.com/heineken.jpg"),
        "2" to Beer("2", "Guinness Draught", "Guinness", "Irish Dry Stout", 4.2, 4.1, "https://example.com/guinness.jpg"),
        "3" to Beer("3", "Corona Extra", "Grupo Modelo", "Pale Lager", 4.5, 3.0, "https://example.com/corona.jpg"),
        "4" to Beer("4", "Sierra Nevada Pale Ale", "Sierra Nevada Brewing Co.", "American Pale Ale", 5.6, 4.3, "https://example.com/sierra.jpg"),
        "5" to Beer("5", "Duvel", "Duvel Moortgat", "Belgian Strong Golden Ale", 8.5, 4.5, "https://example.com/duvel.jpg")    )
    suspend fun getBeerById(beerId: String): Beer? {
        delay(1000)
        return mockBeers[beerId]
    }
}


@HiltViewModel
class RateBeerViewModel @Inject constructor(
    private val beerRatingRepository: BeerRatingRepository,
    savedStateHandle: SavedStateHandle
    // Removed authRepository and groupRepository if not used directly in this VM's current logic
) : ViewModel() {

    val groupId: String = savedStateHandle[RateBeerDestinations.ARG_GROUP_ID] ?: ""
    val beerId: String = savedStateHandle[RateBeerDestinations.ARG_BEER_ID] ?: ""

    private val _uiState = MutableStateFlow(RateBeerScreenState()) // Initial state
    val uiState: StateFlow<RateBeerScreenState> = _uiState.asStateFlow() // Exposed as StateFlow

    private val _navEvent = MutableSharedFlow<RateBeerNavEvent>()
    val navEvent: SharedFlow<RateBeerNavEvent> = _navEvent.asSharedFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()


    init {
        if (groupId.isNotBlank() && beerId.isNotBlank()) {
            loadBeerDetails()
        } else {
            viewModelScope.launch { _snackbarMessage.emit("Group or Beer ID missing.") }
            _uiState.value = _uiState.value.copy(isLoadingBeer = false)
        }
    }

    private fun loadBeerDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingBeer = true, submissionErrorMessage = null)
            val fetchedBeer = RateBeerMockDataSource.getBeerById(beerId) // Returns dk.grp30.ratebeer.data.models.Beer?
            if (fetchedBeer == null) {
                viewModelScope.launch { _snackbarMessage.emit("Beer details not found.") }
                _uiState.value = _uiState.value.copy(isLoadingBeer = false)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingBeer = false, beer = fetchedBeer)
            }
        }
    }

    fun onRatingChanged(newRating: Int) {
        _uiState.value = _uiState.value.copy(selectedRating = newRating)
        submitRating(newRating)
    }

    private fun submitRating(ratingValue: Int) {
        val currentBeer = _uiState.value.beer // Get the beer from the current state
        if (currentBeer == null || ratingValue == 0) {
            viewModelScope.launch { _snackbarMessage.emit("Please select a beer and rating.") }
            return
        }
        // beerId is already a class property from SavedStateHandle, use that for consistency
        // if (groupId.isBlank() || currentBeer.id.isBlank()) { // Use currentBeer.id
        if (groupId.isBlank() || beerId.isBlank()) { // Or continue using class property beerId
            viewModelScope.launch { _snackbarMessage.emit("Cannot submit rating: Group/Beer ID missing.") }
            return
        }


        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingRating = true, submissionErrorMessage = null)
            // Use the class property beerId for submitting, as it's the definitive ID for this screen instance
            when (val result = beerRatingRepository.submitRating(groupId, beerId, ratingValue)) {
                is RatingResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmittingRating = false)
                    _navEvent.emit(RateBeerNavEvent.ToVoteEnded(groupId, beerId))
                }
                is RatingResult.Error -> {
                    val errorMsg = "Error submitting rating: ${result.message}"
                    _uiState.value = _uiState.value.copy(isSubmittingRating = false, submissionErrorMessage = errorMsg)
                    _snackbarMessage.emit(errorMsg)
                }
            }
        }
    }

    // This function was in VoteEndedViewModel, might not be needed if submissionErrorMessage is only for inline display
    // fun clearSubmissionError() {
    //     _uiState.value = _uiState.value.copy(submissionErrorMessage = null)
    // }
}