package jp.co.studio.kaka.ui.auth

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSucceeded: Boolean = false,
) {
    val isSubmitEnabled: Boolean
        get() = username.isNotBlank() &&
            email.contains("@") &&
            password.isNotBlank() &&
            password == confirmPassword &&
            !isLoading
}
