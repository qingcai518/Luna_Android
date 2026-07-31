package jp.co.studio.kaka.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import jp.co.studio.kaka.ui.downloaded.DownloadedScreen
import jp.co.studio.kaka.ui.home.HomeScreen
import jp.co.studio.kaka.ui.musiclist.MusicListScreen
import jp.co.studio.kaka.ui.profile.ProfileScreen
import jp.co.studio.kaka.ui.recommend.RecommendScreen
import jp.co.studio.kaka.ui.search.SearchScreen

/**
 * Single shared nav graph for all 5 bottom-nav tabs (standard Navigation Compose bottom-nav
 * pattern) - MUSIC_LIST is reachable from both HOME and SEARCH so it only needs to exist once.
 */
@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = MainRoutes.HOME) {
        composable(MainRoutes.HOME) {
            HomeScreen(
                onArtistClick = { artist ->
                    navController.navigate(MainRoutes.musicList("artist", artist.id, artist.name))
                },
                onCategoryClick = { category ->
                    navController.navigate(MainRoutes.musicList("category", category.id, category.name))
                },
            )
        }
        composable(MainRoutes.SEARCH) {
            SearchScreen(
                onArtistClick = { artist ->
                    navController.navigate(MainRoutes.musicList("artist", artist.id, artist.name))
                },
                onCategoryClick = { category ->
                    navController.navigate(MainRoutes.musicList("category", category.id, category.name))
                },
            )
        }
        composable(MainRoutes.DOWNLOADED) { DownloadedScreen() }
        composable(MainRoutes.RECOMMEND) { RecommendScreen() }
        composable(MainRoutes.PROFILE) {
            ProfileScreen(
                onNavigateToDownloaded = {
                    navController.navigate(MainRoutes.DOWNLOADED) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(
            route = MainRoutes.MUSIC_LIST,
            arguments = listOf(
                navArgument("idType") { type = NavType.StringType },
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType },
            ),
        ) {
            MusicListScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
