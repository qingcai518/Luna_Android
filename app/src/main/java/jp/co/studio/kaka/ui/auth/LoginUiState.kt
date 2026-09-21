package jp.co.studio.kaka.ui.auth

import jp.co.studio.kaka.util.UiText

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val loginSucceeded: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}
