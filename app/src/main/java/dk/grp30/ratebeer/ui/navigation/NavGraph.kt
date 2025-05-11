package dk.grp30.ratebeer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dk.grp30.ratebeer.ui.screens.login.LoginScreen
import dk.grp30.ratebeer.ui.screens.main.LobbyScreen
import dk.grp30.ratebeer.ui.screens.main.MainScreen
import dk.grp30.ratebeer.ui.screens.register.RegisterScreen
import dk.grp30.ratebeer.ui.screens.welcome.WelcomeScreen

object RateBeerDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    const val LOBBY_ROUTE = "lobby/{groupId}"
}

@Composable
fun RateBeerNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = RateBeerDestinations.WELCOME_ROUTE
    ) {
        composable(RateBeerDestinations.WELCOME_ROUTE) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(RateBeerDestinations.LOGIN_ROUTE) },
                onRegisterClick = { navController.navigate(RateBeerDestinations.REGISTER_ROUTE) }
            )
        }
        
        composable(RateBeerDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                    popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                }}
            )
        }
        
        composable(RateBeerDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = { navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                    popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                }}
            )
        }
        
        composable(RateBeerDestinations.MAIN_ROUTE) {
            MainScreen(
                onCreateGroup = { groupId -> 
                    navController.navigate(RateBeerDestinations.LOBBY_ROUTE.replace("{groupId}", groupId)) 
                },
                onJoinGroup = { groupId -> 
                    navController.navigate(RateBeerDestinations.LOBBY_ROUTE.replace("{groupId}", groupId)) 
                }
            )
        }
        
        composable(RateBeerDestinations.LOBBY_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            LobbyScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 