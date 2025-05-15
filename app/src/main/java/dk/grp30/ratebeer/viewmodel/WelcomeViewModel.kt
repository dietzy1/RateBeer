package dk.grp30.ratebeer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.grp30.ratebeer.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WelcomeNavEvent {
    object ToMain : WelcomeNavEvent
}

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navEvent = MutableSharedFlow<WelcomeNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        if (authRepository.isUserLoggedIn) {
            viewModelScope.launch {
                _navEvent.emit(WelcomeNavEvent.ToMain)
            }
        }
    }
}