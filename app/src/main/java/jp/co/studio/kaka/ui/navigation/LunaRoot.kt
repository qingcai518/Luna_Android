package jp.co.studio.kaka.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.studio.kaka.ui.main.MainScaffold
import jp.co.studio.kaka.ui.player.PlayerViewModel

/** Top-level branch on login state. */
@Composable
fun LunaRoot(
    viewModel: RootViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.forcedLogoutEvents.collect {
            playerViewModel.stopAndClear()
        }
    }

    if (isLoggedIn) {
        MainScaffold(playerViewModel = playerViewModel)
    } else {
        AuthNavHost()
    }
}
