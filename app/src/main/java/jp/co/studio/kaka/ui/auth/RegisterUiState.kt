package jp.co.studio.kaka.ui.auth

import jp.co.studio.kaka.util.UiText

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val registerSucceeded: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = username.isNotBlank() &&
            email.contains("@") &&
            password.isNotBlank() &&
            password == confirmPassword &&
            !isLoading
}
