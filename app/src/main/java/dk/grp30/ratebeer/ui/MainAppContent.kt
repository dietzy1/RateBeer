package dk.grp30.ratebeer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import dk.grp30.ratebeer.ui.navigation.RateBeerNavGraph

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    RateBeerNavGraph(navController = navController)
}