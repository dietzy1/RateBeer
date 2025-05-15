package dk.grp30.ratebeer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.auth.AuthRepository
import dk.grp30.ratebeer.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginScreenState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginNavEvent {
    object ToMain : LoginNavEvent
    object ToRegister : LoginNavEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginScreenState())
        private set

    private val _navEvent = MutableSharedFlow<LoginNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    _navEvent.emit(LoginNavEvent.ToMain)
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun clearErrorMessage() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun navigateToRegister() {
        viewModelScope.launch {
            _navEvent.emit(LoginNavEvent.ToRegister)
        }
    }
}