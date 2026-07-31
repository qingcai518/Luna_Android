package jp.co.studio.kaka.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.repository.AuthRepository
import jp.co.studio.kaka.domain.repository.DownloadRepository
import jp.co.studio.kaka.player.MediaControllerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    downloadRepository: DownloadRepository,
    private val mediaControllerRepository: MediaControllerRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.currentUser,
        authRepository.isLoggedIn,
        downloadRepository.downloadedMusics,
    ) { user, isLoggedIn, downloaded ->
        ProfileUiState(
            username = user?.username,
            email = user?.email,
            avatarUrl = user?.avatarUrl,
            isLoggedIn = isLoggedIn,
            downloadedCount = downloaded.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    /** Logout must stop playback first - fixes an iOS gap where logging out leaves music playing. */
    fun logout() {
        viewModelScope.launch {
            mediaControllerRepository.stopAndClear()
            authRepository.logout()
        }
    }
}
