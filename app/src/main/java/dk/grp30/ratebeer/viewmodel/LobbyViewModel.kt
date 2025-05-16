package dk.grp30.ratebeer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.auth.AuthRepository
import dk.grp30.ratebeer.data.firestore.Group
import dk.grp30.ratebeer.data.firestore.GroupRepository
import dk.grp30.ratebeer.data.firestore.DataResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LobbyUiState {
    data class Success(val group: Group, val isCurrentUserHost: Boolean) : LobbyUiState
    data class Error(val message: String) : LobbyUiState
    object Loading : LobbyUiState
    object GroupLeft : LobbyUiState
}

sealed interface LobbyNavEvent {
    data class ToRateBeer(val groupId: String, val beerId: String) : LobbyNavEvent
    data class ToResults(val groupId: String, val beerId: String) : LobbyNavEvent
    object ToFindBeer : LobbyNavEvent
    object NavigateBack : LobbyNavEvent
}

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = savedStateHandle["groupId"] ?: ""

    private val _uiState = MutableStateFlow<LobbyUiState>(LobbyUiState.Loading)
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<LobbyNavEvent>()
    val navEvent: SharedFlow<LobbyNavEvent> = _navEvent.asSharedFlow()

    private var currentObservedGroup: Group? = null

    init {
        if (groupId.isBlank()) {
            _uiState.value = LobbyUiState.Error("Group ID is missing.")
        } else {
            observeGroupDetails()
        }
    }

    private fun observeGroupDetails() {
        viewModelScope.launch {
            groupRepository.observeGroup(groupId).collectLatest { group ->
                if (group == null) {
                    _uiState.value = LobbyUiState.Error("Group not found or has been deleted.")
                    _navEvent.tryEmit(LobbyNavEvent.NavigateBack)
                    return@collectLatest
                }

                val oldStatus = currentObservedGroup?.status
                val oldBeerId = currentObservedGroup?.currentBeerId
                currentObservedGroup = group

                // 👇 CORRECTED USAGE
                val currentUserId = authRepository.currentUser?.uid
                val isHost = group.hostId == currentUserId // This comparison is fine even if currentUserId is null (will be false)
                _uiState.value = LobbyUiState.Success(group, isHost)


                if (group.status == "tasting" && group.currentBeerId != null &&
                    (oldStatus != "tasting" || oldBeerId != group.currentBeerId)
                ) {
                    _navEvent.tryEmit(LobbyNavEvent.ToRateBeer(groupId, group.currentBeerId!!))
                } else if (group.status == "results" && group.currentBeerId != null &&
                    (oldStatus != "results" || oldBeerId != group.currentBeerId)
                ) {
                    _navEvent.tryEmit(LobbyNavEvent.ToResults(groupId, group.currentBeerId!!))
                } else if (group.status == "lobby" && (oldStatus == "tasting" || oldStatus == "results")) {
                    // ...
                }
            }
        }
    }

    fun onStartTastingSessionClicked() {
        val currentState = _uiState.value
        if (currentState is LobbyUiState.Success && currentState.isCurrentUserHost) {
            viewModelScope.launch {
                _navEvent.emit(LobbyNavEvent.ToFindBeer)
            }
        }
    }

    fun onLeaveGroupClicked() {
        viewModelScope.launch {
            _uiState.value = LobbyUiState.Loading
            // 👇 CORRECTED USAGE (and providing a default if null, though leaveGroup should handle blank userId)
            val userIdToLeave = authRepository.currentUser?.uid ?: ""
            val result = groupRepository.leaveGroup(groupId, userIdToLeave)
            when (result) {
                is DataResult.Success -> {
                    _uiState.value = LobbyUiState.GroupLeft
                    _navEvent.emit(LobbyNavEvent.NavigateBack)
                }
                is DataResult.Error -> {
                    currentObservedGroup?.let {
                        // 👇 CORRECTED USAGE
                        _uiState.value = LobbyUiState.Success(it, it.hostId == authRepository.currentUser?.uid)
                    } ?: run {
                        _uiState.value = LobbyUiState.Error(result.message)
                    }
                }
                DataResult.Loading -> {}
            }
        }
    }

    fun onEndVotingAndShowResultsClicked() {
        val currentState = _uiState.value
        if (currentState is LobbyUiState.Success && currentState.isCurrentUserHost && currentState.group.status == "tasting") {
            viewModelScope.launch {
                _uiState.value = LobbyUiState.Loading
                val result = groupRepository.moveToResults(groupId)
                if (result is DataResult.Error) {
                    _uiState.value = LobbyUiState.Success(currentState.group, currentState.isCurrentUserHost)
                }
            }
        }
    }
}