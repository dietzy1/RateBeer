package dk.grp30.ratebeer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dk.grp30.ratebeer.data.api.Beer
import dk.grp30.ratebeer.ui.screens.beer.FindBeerScreen
import dk.grp30.ratebeer.ui.screens.beer.RateBeerScreen
import dk.grp30.ratebeer.ui.screens.beer.VoteEndedScreen
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
    const val FIND_BEER_ROUTE = "find_beer/{groupId}"
    const val RATE_BEER_ROUTE = "rate_beer/{groupId}/{beerId}"
    const val VOTE_ENDED_ROUTE = "vote_ended/{groupId}/{beerId}"
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
                onNavigateBack = { navController.popBackStack() },
                onFindBeerClick = {
                    navController.navigate(RateBeerDestinations.FIND_BEER_ROUTE.replace("{groupId}", groupId))
                }
            )
        }
        
        composable(RateBeerDestinations.FIND_BEER_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            FindBeerScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                onBeerSelected = { beer ->
                    navController.navigate(
                        RateBeerDestinations.RATE_BEER_ROUTE
                            .replace("{groupId}", groupId)
                            .replace("{beerId}", beer.id)
                    )
                }
            )
        }
        
        composable(RateBeerDestinations.RATE_BEER_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val beerId = backStackEntry.arguments?.getString("beerId") ?: ""
            RateBeerScreen(
                groupId = groupId,
                beerId = beerId,
                onVoteSubmitted = { rating ->
                    navController.navigate(
                        RateBeerDestinations.VOTE_ENDED_ROUTE
                            .replace("{groupId}", groupId)
                            .replace("{beerId}", beerId)
                    ) {
                        popUpTo(RateBeerDestinations.FIND_BEER_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        composable(RateBeerDestinations.VOTE_ENDED_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val beerId = backStackEntry.arguments?.getString("beerId") ?: ""
            VoteEndedScreen(
                groupId = groupId,
                beerId = beerId,
                onRateNextBeer = {
                    navController.navigate(
                        RateBeerDestinations.FIND_BEER_ROUTE.replace("{groupId}", groupId)
                    )
                },
                onLeaveGroup = {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                        popUpTo(RateBeerDestinations.MAIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
} 