package jp.co.studio.kaka.ui.auth

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSucceeded: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}
