package dk.grp30.ratebeer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.auth.AuthRepository
import dk.grp30.ratebeer.data.firestore.DataResult
import dk.grp30.ratebeer.data.firestore.GroupRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenUiState(
    val isLoadingCreate: Boolean = false,
    val isLoadingJoin: Boolean = false
)

sealed interface MainNavEvent {
    data class ToLobby(val groupId: String) : MainNavEvent
    object ToWelcome : MainNavEvent
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<MainNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    fun createGroup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCreate = true)
            val hostName = authRepository.currentUser?.displayName
            when (val result = groupRepository.createGroup(hostName)) {
                is DataResult.Success -> {
                    _navEvent.emit(MainNavEvent.ToLobby(result.data.id))
                }
                is DataResult.Error -> {
                    _uiMessage.emit("Error creating group: ${result.message}")
                }
                DataResult.Loading -> { }
            }
            _uiState.value = _uiState.value.copy(isLoadingCreate = false)
        }
    }

    fun joinGroup(pinCode: String) {
        if (pinCode.length != 6 || !pinCode.all { it.isDigit() }) {
            viewModelScope.launch { _uiMessage.emit("Please enter a valid 6-digit PIN.") }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingJoin = true)
            val userName = authRepository.currentUser?.displayName
            when (val result = groupRepository.joinGroup(pinCode, userName)) {
                is DataResult.Success -> {
                    _navEvent.emit(MainNavEvent.ToLobby(result.data.id))
                }
                is DataResult.Error -> {
                    _uiMessage.emit("Error joining group: ${result.message}")
                }
                DataResult.Loading -> { }
            }
            _uiState.value = _uiState.value.copy(isLoadingJoin = false)
        }
    }

    fun logout() {
        authRepository.logout()
        viewModelScope.launch {
            _navEvent.emit(MainNavEvent.ToWelcome)
        }
    }

    fun checkUserLoggedIn() {
        if (!authRepository.isUserLoggedIn) {
            viewModelScope.launch {
                _navEvent.emit(MainNavEvent.ToWelcome)
            }
        }
    }
}