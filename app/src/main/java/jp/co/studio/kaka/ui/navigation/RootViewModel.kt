package jp.co.studio.kaka.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.studio.kaka.domain.SessionManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionManager: SessionManager,
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn

    /** Refresh-token failure kicked the user out - collectors should also stop playback. */
    val forcedLogoutEvents: SharedFlow<Unit> = sessionManager.forcedLogoutEvents
}
