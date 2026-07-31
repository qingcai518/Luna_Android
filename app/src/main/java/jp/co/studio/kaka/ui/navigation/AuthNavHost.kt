package jp.co.studio.kaka.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.co.studio.kaka.ui.auth.LoginScreen
import jp.co.studio.kaka.ui.auth.RegisterScreen

@Composable
fun AuthNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AuthRoutes.REGISTER) },
            )
        }
        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSucceeded = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
