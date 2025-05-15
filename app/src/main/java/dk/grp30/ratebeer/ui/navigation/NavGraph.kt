package dk.grp30.ratebeer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

// RateBeerDestinations object remains the same

@Composable
fun RateBeerNavGraph(navController: NavHostController) {
    // val authRepository = AuthModule.provideAuthRepository() // REMOVE THIS

    // Initial auth check and navigation is now handled by WelcomeViewModel
    // The LaunchedEffect here is no longer needed.

    NavHost(
        navController = navController,
        startDestination = RateBeerDestinations.WELCOME_ROUTE
    ) {
        composable(RateBeerDestinations.WELCOME_ROUTE) {
            // WelcomeScreen now uses its own WelcomeViewModel (obtained via hiltViewModel())
            // to check auth status and trigger navigation.
            WelcomeScreen(
                onLoginClick = { navController.navigate(RateBeerDestinations.LOGIN_ROUTE) },
                onRegisterClick = { navController.navigate(RateBeerDestinations.REGISTER_ROUTE) },
                onNavigateToMain = {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                        popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RateBeerDestinations.LOGIN_ROUTE) {
            // LoginScreen will use its LoginViewModel (obtained via hiltViewModel())
            // It will no longer take authRepository directly.
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { // This lambda is called when LoginViewModel signals success
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                        popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = { // This lambda is called when LoginViewModel signals to go to register
                    navController.navigate(RateBeerDestinations.REGISTER_ROUTE) {
                        // Optional: popUpTo(RateBeerDestinations.LOGIN_ROUTE) { inclusive = true } if you want to clear login from backstack
                    }
                }
                // authRepository = authRepository // REMOVE THIS
            )
        }

        composable(RateBeerDestinations.REGISTER_ROUTE) {
            // RegisterScreen will use its RegisterViewModel (obtained via hiltViewModel())
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = { // Called when RegisterViewModel signals success
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                        popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { // Called when RegisterViewModel signals to go to login
                    navController.navigate(RateBeerDestinations.LOGIN_ROUTE) {
                        popUpTo(RateBeerDestinations.REGISTER_ROUTE) { inclusive = true } // Clear register from backstack
                    }
                }
                // authRepository = authRepository // REMOVE THIS
            )
        }

        composable(RateBeerDestinations.MAIN_ROUTE) {
            // MainScreen uses its MainViewModel (obtained via hiltViewModel())
            // The defensive auth check is now inside MainViewModel.
            MainScreen(
                onNavigateToLobby = { groupId ->
                    navController.navigate(RateBeerDestinations.LOBBY_ROUTE.replace("{groupId}", groupId))
                },
                onNavigateToWelcome = { // Called when MainViewModel signals logout or auth failure
                    navController.navigate(RateBeerDestinations.WELCOME_ROUTE) {
                        popUpTo(RateBeerDestinations.MAIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RateBeerDestinations.LOBBY_ROUTE,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }) // Define argument
        ) { backStackEntry ->
            // LobbyScreen will use its LobbyViewModel (obtained via hiltViewModel())
            // The groupId will be retrieved from SavedStateHandle in LobbyViewModel
            LobbyScreen(
                navController = navController // Pass navController for nav events from LobbyViewModel
                // No need to pass groupId directly if ViewModel handles it
                // onNavigateBack, onFindBeerClick will be handled by events from LobbyViewModel
            )
        }

        composable(
            route = RateBeerDestinations.FIND_BEER_ROUTE,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            // FindBeerScreen will use its FindBeerViewModel
            // groupId retrieved from SavedStateHandle in FindBeerViewModel
            FindBeerScreen(
                // groupId = backStackEntry.arguments?.getString("groupId") ?: "", // ViewModel gets this
                navController = navController, // For navigation from its ViewModel
                onNavigateBack = { navController.popBackStack() }
                // onBeerSelected will be handled by FindBeerViewModel, which then triggers Firestore update.
                // LobbyViewModel will react to Firestore update.
            )
        }

        composable(
            route = RateBeerDestinations.RATE_BEER_ROUTE,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("beerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // RateBeerScreen will use its RateBeerViewModel
            RateBeerScreen(
                // groupId and beerId retrieved from SavedStateHandle in RateBeerViewModel
                navController = navController // For navigation from its ViewModel
                // onVoteSubmitted will be handled by RateBeerViewModel
            )
        }

        composable(
            route = RateBeerDestinations.VOTE_ENDED_ROUTE,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("beerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // VoteEndedScreen will use its VoteEndedViewModel
            VoteEndedScreen(
                // groupId and beerId retrieved from SavedStateHandle in VoteEndedViewModel
                navController = navController // For navigation from its ViewModel
                // onRateNextBeer and onLeaveGroup will be handled by VoteEndedViewModel
            )
        }
    }
}