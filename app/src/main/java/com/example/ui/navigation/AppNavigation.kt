package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.onboarding.OnboardingScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    appViewModel: com.example.viewmodel.AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("onboarding") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            com.example.ui.screens.dashboard.MainDashboard(navController, appViewModel)
        }
        composable(
            "chat/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: "1"
            com.example.ui.screens.matches.ChatScreen(
                matchId = matchId,
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
