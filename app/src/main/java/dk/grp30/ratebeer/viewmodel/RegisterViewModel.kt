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

data class RegisterScreenState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface RegisterNavEvent {
    object ToMain : RegisterNavEvent
    object ToLogin : RegisterNavEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterScreenState())
        private set

    private val _navEvent = MutableSharedFlow<RegisterNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    fun registerUser(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please fill in all fields")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(errorMessage = "Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.register(email, password, username)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    _navEvent.emit(RegisterNavEvent.ToMain)
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

    fun navigateToLogin() {
        viewModelScope.launch {
            _navEvent.emit(RegisterNavEvent.ToLogin)
        }
    }
}