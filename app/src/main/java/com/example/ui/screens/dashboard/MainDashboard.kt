package com.example.ui.screens.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.discovery.DiscoveryScreen
import com.example.ui.screens.matches.MatchesChatsScreen

@Composable
fun MainDashboard(
    rootNavController: NavHostController,
    appViewModel: com.example.viewmodel.AppViewModel
) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "discovery",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("discovery") {
                DiscoveryScreen(appViewModel)
            }
            composable("instant_match") {
                com.example.ui.screens.instant.InstantMatchScreen()
            }
            composable("likes_chats") {
                MatchesChatsScreen(
                    appViewModel = appViewModel,
                    onNavigateToChat = { match ->
                        rootNavController.navigate("chat/${match.id}")
                    }
                )
            }
            composable("profile") {
                Text("Profile Settings - Coming Soon")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Discovery", "discovery", Icons.Rounded.LocalFireDepartment),
        BottomNavItem("Instant", "instant_match", Icons.Filled.ChatBubble),
        BottomNavItem("Matches", "likes_chats", Icons.Filled.Favorite),
        BottomNavItem("Profile", "profile", Icons.Filled.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

data class BottomNavItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
