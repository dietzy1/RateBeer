package dk.grp30.ratebeer.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.auth.AuthRepository
import dk.grp30.ratebeer.data.firestore.BeerRatingRepository
import dk.grp30.ratebeer.data.firestore.RatingResult
import dk.grp30.ratebeer.data.firestore.GroupRepository
import dk.grp30.ratebeer.data.firestore.DataResult as GenericDataResult
import dk.grp30.ratebeer.data.models.Beer
import dk.grp30.ratebeer.ui.navigation.RateBeerDestinations
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoteEndedScreenState(
    val isLoading: Boolean = true,
    val beer: Beer? = null,
    val groupRating: Double? = null,
    val errorMessage: String? = null
)

sealed interface VoteEndedNavEvent {
    data class ToFindBeer(val groupId: String) : VoteEndedNavEvent
    object ToMain : VoteEndedNavEvent
}


private object VoteEndedMockBeerDataSource {
    private val mockBeers = mapOf(
        "1" to Beer("1", "Heineken", "Heineken International", "Pale Lager", 5.0, 3.2, "https://example.com/heineken.jpg"),
        "2" to Beer("2", "Guinness Draught", "Guinness", "Irish Dry Stout", 4.2, 4.1, "https://example.com/guinness.jpg"),
        "3" to Beer("3", "Corona Extra", "Grupo Modelo", "Pale Lager", 4.5, 3.0, "https://example.com/corona.jpg"),
        "4" to Beer("4", "Sierra Nevada Pale Ale", "Sierra Nevada Brewing Co.", "American Pale Ale", 5.6, 4.3, "https://example.com/sierra.jpg"),
        "5" to Beer("5", "Duvel", "Duvel Moortgat", "Belgian Strong Golden Ale", 8.5, 4.5, "https://example.com/duvel.jpg")
    )

    suspend fun getBeerById(beerId: String): Beer? {
        delay(1000)
        return mockBeers[beerId]
    }
}


@HiltViewModel
class VoteEndedViewModel @Inject constructor(
    private val beerRatingRepository: BeerRatingRepository,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = savedStateHandle[RateBeerDestinations.ARG_GROUP_ID] ?: ""
    val beerId: String = savedStateHandle[RateBeerDestinations.ARG_BEER_ID] ?: ""

    private val _uiState = MutableStateFlow(VoteEndedScreenState())
    val uiState: StateFlow<VoteEndedScreenState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<VoteEndedNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    init {
        if (groupId.isNotBlank() && beerId.isNotBlank()) {
            loadVoteResults()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Group or Beer ID missing. Cannot load results."
            )
        }
    }

    private fun loadVoteResults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val fetchedBeer = VoteEndedMockBeerDataSource.getBeerById(beerId)
            if (fetchedBeer == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beer details not found for ID: $beerId"
                )
                return@launch
            }


            when (val groupRatingResult = beerRatingRepository.getGroupRating(groupId, beerId)) {
                is RatingResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        beer = fetchedBeer,
                        groupRating = groupRatingResult.rating.averageRating,
                        errorMessage = null
                    )
                }
                is RatingResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        beer = fetchedBeer,
                        groupRating = null,
                        errorMessage = "Failed to load group rating: ${groupRatingResult.message}"
                    )
                }
            }
        }
    }

    fun onRateNextBeerClicked() {
        viewModelScope.launch {
            _navEvent.emit(VoteEndedNavEvent.ToFindBeer(groupId))
        }
    }

    fun onLeaveGroupClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = authRepository.currentUser?.uid ?: ""
            when (val leaveResult = groupRepository.leaveGroup(groupId, userId)) {
                is GenericDataResult.Success -> {
                    _navEvent.emit(VoteEndedNavEvent.ToMain)
                }
                is GenericDataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to leave group: ${leaveResult.message}"
                    )
                }
                GenericDataResult.Loading -> { }
            }
            if (_uiState.value.isLoading && _navEvent.replayCache.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}