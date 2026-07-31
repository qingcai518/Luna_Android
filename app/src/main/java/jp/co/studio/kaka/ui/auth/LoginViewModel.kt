package jp.co.studio.kaka.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.repository.AuthRepository
import jp.co.studio.kaka.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(state.username, state.password)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, loginSucceeded = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "网络连接失败，请检查网络后重试")
                }
            }
        }
    }
}
