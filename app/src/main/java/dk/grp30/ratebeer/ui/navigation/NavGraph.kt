package dk.grp30.ratebeer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dk.grp30.ratebeer.data.api.Beer
import dk.grp30.ratebeer.data.auth.AuthRepository
import dk.grp30.ratebeer.di.AuthModule
import dk.grp30.ratebeer.ui.screens.beer.FindBeerScreen
import dk.grp30.ratebeer.ui.screens.beer.RateBeerScreen
import dk.grp30.ratebeer.ui.screens.beer.VoteEndedScreen
import dk.grp30.ratebeer.ui.screens.login.LoginScreen
import dk.grp30.ratebeer.ui.screens.main.LobbyScreen
import dk.grp30.ratebeer.ui.screens.main.MainScreen
import dk.grp30.ratebeer.ui.screens.register.RegisterScreen
import dk.grp30.ratebeer.ui.screens.welcome.WelcomeScreen
import dk.grp30.ratebeer.data.firestore.GroupRepository
import androidx.compose.runtime.remember
import dk.grp30.ratebeer.data.firestore.BeerRatingRepository

object RateBeerDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    const val LOBBY_ROUTE = "lobby/{groupId}/{groupCode}"
    const val FIND_BEER_ROUTE = "find_beer/{groupId}"
    const val RATE_BEER_ROUTE = "rate_beer/{groupId}/{beerId}"
    const val VOTE_ENDED_ROUTE = "vote_ended/{groupId}/{beerId}"
}

@Composable
fun RateBeerNavGraph(navController: NavHostController) {
    val authRepository = AuthModule.provideAuthRepository()
    val groupRepository = remember { dk.grp30.ratebeer.data.firestore.GroupRepository() }
    val beerRatingRepository = remember { BeerRatingRepository() }
    
    LaunchedEffect(key1 = true) {
        if (authRepository.isUserLoggedIn) {
            navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
            }
        }
    }
    
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
                }},
                onNavigateToSignup= {navController.navigate(RateBeerDestinations.REGISTER_ROUTE)},
                authRepository = authRepository
            )
        }
        
        composable(RateBeerDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = { navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                    popUpTo(RateBeerDestinations.WELCOME_ROUTE) { inclusive = true }
                }},
                onNavigateToLogin = {navController.navigate(RateBeerDestinations.LOGIN_ROUTE)},
                authRepository = authRepository
            )
        }
        
        composable(RateBeerDestinations.MAIN_ROUTE) {
            LaunchedEffect(key1 = true) {
                if (!authRepository.isUserLoggedIn) {
                    navController.navigate(RateBeerDestinations.WELCOME_ROUTE) {
                        popUpTo(RateBeerDestinations.MAIN_ROUTE) { inclusive = true }
                    }
                }
            }
            
            MainScreen(
                onNavigateToLobby = { groupId, groupCode ->
                    navController.navigate(RateBeerDestinations.LOBBY_ROUTE
                        .replace("{groupId}", groupId)
                        .replace("{groupCode}", groupCode)
                    )
                },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(RateBeerDestinations.WELCOME_ROUTE) {
                        popUpTo(RateBeerDestinations.MAIN_ROUTE) { inclusive = true }
                    }
                },
                groupRepository = groupRepository
            )
        }
        
        composable(RateBeerDestinations.LOBBY_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupCode = backStackEntry.arguments?.getString("groupCode") ?: ""
            LobbyScreen(
                groupId = groupId,
                groupCode = groupCode,
                onNavigateBack = { navController.popBackStack() },
                onFindBeerClick = {
                    navController.navigate(RateBeerDestinations.FIND_BEER_ROUTE.replace("{groupId}", groupId))
                },
                groupRepository = groupRepository,
                onNavigateToRoute = { route ->
                    navController.navigate(route)
                },
            )
        }
        
        composable(RateBeerDestinations.FIND_BEER_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            FindBeerScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                groupRepository = groupRepository,
                onNavigateToRateBeer = { gId, bId ->
                    navController.navigate(
                        RateBeerDestinations.RATE_BEER_ROUTE
                            .replace("{groupId}", gId)
                            .replace("{beerId}", bId)
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
                beerRatingRepository = beerRatingRepository,
                onNavToVoteEnded = {
                    navController.navigate(
                        RateBeerDestinations.VOTE_ENDED_ROUTE
                            .replace("{groupId}", groupId)
                            .replace("{beerId}", beerId)
                    ) {
                        popUpTo(RateBeerDestinations.FIND_BEER_ROUTE) { inclusive = true }
                    }
                },
                groupRepository = groupRepository,
                )
        }
        
        composable(RateBeerDestinations.VOTE_ENDED_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val beerId = backStackEntry.arguments?.getString("beerId") ?: ""
            VoteEndedScreen(
                groupId = groupId,
                beerId = beerId,
                beerRatingRepository = beerRatingRepository,
                onRateNextBeer = {
                    navController.navigate(
                        RateBeerDestinations.FIND_BEER_ROUTE.replace("{groupId}", groupId)
                    )
                },
                onLeaveGroup = {
                    navController.navigate(RateBeerDestinations.MAIN_ROUTE) {
                        popUpTo(RateBeerDestinations.MAIN_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToRoute = { route ->
                    navController.navigate(route)
                },
                groupRepository = groupRepository,
                )
        }
    }
}