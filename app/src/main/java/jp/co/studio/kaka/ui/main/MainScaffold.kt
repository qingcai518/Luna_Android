package jp.co.studio.kaka.ui.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import jp.co.studio.kaka.ui.navigation.MainNavHost
import jp.co.studio.kaka.ui.navigation.MainRoutes
import jp.co.studio.kaka.ui.player.FullPlayerScreen
import jp.co.studio.kaka.ui.player.MiniPlayerBar
import jp.co.studio.kaka.ui.player.PlayerViewModel

enum class MainTab(val label: String, val icon: ImageVector, val route: String) {
    HOME("主页", Icons.Filled.Home, MainRoutes.HOME),
    SEARCH("检索", Icons.Filled.Search, MainRoutes.SEARCH),
    DOWNLOADED("已下载", Icons.Filled.CloudDownload, MainRoutes.DOWNLOADED),
    RECOMMEND("推荐", Icons.Filled.Star, MainRoutes.RECOMMEND),
    PROFILE("我的", Icons.Filled.Person, MainRoutes.PROFILE),
}

/**
 * 5-tab shell + persistent MiniPlayer, matching the iOS TabBar layout. A single NavController
 * shared with MainNavHost implements the standard bottom-nav pattern (popUpTo start destination
 * with saveState, launchSingleTop, restoreState) so each tab keeps its own back stack (e.g. a
 * MusicList pushed from Home doesn't leak into Search's stack). Tapping the MiniPlayer opens
 * FullPlayerScreen as a near-fullscreen modal sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(playerViewModel: PlayerViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    var showFullPlayer by rememberSaveable { mutableStateOf(false) }
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    RequestNotificationPermissionIfNeeded()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MainTab.entries.forEach { tab ->
                item(
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    selected = tab.route == currentRoute,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                MainNavHost(navController = navController)
            }
            playerState.currentMusic?.let { music ->
                MiniPlayerBar(
                    music = music,
                    isPlaying = playerState.isPlaying,
                    onPlayPauseClick = playerViewModel::playPause,
                    onBarClick = { showFullPlayer = true },
                )
            }
        }
    }

    if (showFullPlayer) {
        ModalBottomSheet(onDismissRequest = { showFullPlayer = false }) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)) {
                FullPlayerScreen()
            }
        }
    }
}

@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
