package dk.grp30.ratebeer.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.grp30.ratebeer.data.firestore.GroupRepository
import dk.grp30.ratebeer.data.firestore.GroupResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI state for group creation/joining
data class GroupUiState(
    val isLoading: Boolean = false,
    val groupId: String? = null,
    val groupCode: String? = null,
    val error: String? = null
)

class MainViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState

    fun createGroup(groupCode: String) {
        _uiState.value = GroupUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = groupRepository.createGroup(groupCode)) {
                is GroupResult.Success -> _uiState.value = GroupUiState(
                    groupId = result.group.id,
                    groupCode = result.group.groupCode
                )
                is GroupResult.Error -> _uiState.value = GroupUiState(error = result.message)
            }
        }
    }

    fun joinGroup(groupCode: String) {
        _uiState.value = GroupUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = groupRepository.joinGroup(groupCode)) {
                is GroupResult.Success -> _uiState.value = GroupUiState(
                    groupId = result.group.id,
                    groupCode = result.group.groupCode
                )
                is GroupResult.Error -> _uiState.value = GroupUiState(error = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 