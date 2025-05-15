package dk.grp30.ratebeer.ui.navigation

import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect // No longer needed at the NavGraph level
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dk.grp30.ratebeer.ui.screens.beer.FindBeerScreen
import dk.grp30.ratebeer.ui.screens.beer.RateBeerScreen
import dk.grp30.ratebeer.ui.screens.beer.VoteEndedScreen
import dk.grp30.ratebeer.ui.screens.login.LoginScreen
import dk.grp30.ratebeer.ui.screens.main.LobbyScreen
import dk.grp30.ratebeer.ui.screens.main.MainScreen
import dk.grp30.ratebeer.ui.screens.register.RegisterScreen
import dk.grp30.ratebeer.ui.screens.welcome.WelcomeScreen

object RateBeerDestinations {
    // Routes without arguments
    const val WELCOME_ROUTE = "welcome"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"

    // Argument names (good practice to define them as constants)
    const val ARG_GROUP_ID = "groupId"
    const val ARG_BEER_ID = "beerId"

    // Routes with arguments - defined with placeholders
    const val LOBBY_ROUTE_PATTERN = "lobby/{$ARG_GROUP_ID}"
    const val FIND_BEER_ROUTE_PATTERN = "find_beer/{$ARG_GROUP_ID}"
    const val RATE_BEER_ROUTE_PATTERN = "rate_beer/{$ARG_GROUP_ID}/{$ARG_BEER_ID}"
    const val VOTE_ENDED_ROUTE_PATTERN = "vote_ended/{$ARG_GROUP_ID}/{$ARG_BEER_ID}"

    // --- Helper functions to build routes with actual arguments ---
    // These are very useful for type safety and avoiding string errors when navigating.

    fun lobbyRoute(groupId: String): String {
        return LOBBY_ROUTE_PATTERN.replace("{$ARG_GROUP_ID}", groupId)
    }

    fun findBeerRoute(groupId: String): String {
        return FIND_BEER_ROUTE_PATTERN.replace("{$ARG_GROUP_ID}", groupId)
    }

    fun rateBeerRoute(groupId: String, beerId: String): String {
        return RATE_BEER_ROUTE_PATTERN
            .replace("{$ARG_GROUP_ID}", groupId)
            .replace("{$ARG_BEER_ID}", beerId)
    }

    fun voteEndedRoute(groupId: String, beerId: String): String {
        return VOTE_ENDED_ROUTE_PATTERN
            .replace("{$ARG_GROUP_ID}", groupId)
            .replace("{$ARG_BEER_ID}", beerId)
    }
}
@Composable
fun RateBeerNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = RateBeerDestinations.WELCOME_ROUTE // Use constant
    ) {
        composable(RateBeerDestinations.WELCOME_ROUTE) { // Use constant
            WelcomeScreen(
                onLoginClick = { navController.navigate(RateBeerDestinations.LOGIN_ROUTE) }, // Use constant
                onRegisterClick = { navController.navigate(RateBeerDestinations.REGISTER_ROUTE) }, // Use constant
                onNavigateToMain = {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) { // Use constant
                        popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RateBeerDestinations.LOGIN_ROUTE) { // Use constant
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) { // Use constant
                        popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(RateBeerDestinations.REGISTER_ROUTE) // Use constant
                }
            )
        }

        composable(RateBeerDestinations.REGISTER_ROUTE) { // Use constant
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) { // Use constant
                        popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(RateBeerDestinations.LOGIN_ROUTE) { // Use constant
                        popUpTo(RateBeerDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(RateBeerDestinations.MAIN_ROUTE) { // Use constant
            MainScreen(
                onNavigateToLobby = { groupId ->
                    // Use helper function for clarity and safety
                    navController.navigate(RateBeerDestinations.lobbyRoute(groupId))
                },
                onNavigateToWelcome = {
                    navController.navigate(RateBeerDestinations.WELCOME_ROUTE) { // Use constant
                        popUpTo(RateBeerDestinations.MAIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RateBeerDestinations.LOBBY_ROUTE_PATTERN, // Use pattern for route definition
            arguments = listOf(navArgument(RateBeerDestinations.ARG_GROUP_ID) { type = NavType.StringType }) // Use arg constant
        ) {
            LobbyScreen(navController = navController)
        }

        composable(
            route = RateBeerDestinations.FIND_BEER_ROUTE_PATTERN, // Use pattern
            arguments = listOf(navArgument(RateBeerDestinations.ARG_GROUP_ID) { type = NavType.StringType }) // Use arg constant
        ) {
            FindBeerScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
                // onBeerSelected will be handled by its ViewModel, which will trigger navigation (likely popBackStack)
                // or an event that LobbyViewModel observes if FindBeer is a sub-flow of Lobby
            )
        }

        composable(
            route = RateBeerDestinations.RATE_BEER_ROUTE_PATTERN, // Use pattern
            arguments = listOf(
                navArgument(RateBeerDestinations.ARG_GROUP_ID) { type = NavType.StringType }, // Use arg constant
                navArgument(RateBeerDestinations.ARG_BEER_ID) { type = NavType.StringType }  // Use arg constant
            )
        ) {
            RateBeerScreen(navController = navController)
        }

        composable(
            route = RateBeerDestinations.VOTE_ENDED_ROUTE_PATTERN, // Use pattern
            arguments = listOf(
                navArgument(RateBeerDestinations.ARG_GROUP_ID) { type = NavType.StringType }, // Use arg constant
                navArgument(RateBeerDestinations.ARG_BEER_ID) { type = NavType.StringType }  // Use arg constant
            )
        ) {
            VoteEndedScreen(navController = navController)
        }
    }
}