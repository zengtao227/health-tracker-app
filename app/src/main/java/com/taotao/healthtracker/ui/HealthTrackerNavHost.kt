package com.taotao.healthtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.taotao.healthtracker.ui.screens.*
import com.taotao.healthtracker.viewmodel.HealthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class Screen(val route: String) {
    Add("Add"),
    History("History"),
    Stats("Stats"),
    Knowledge("Knowledge")
}

@Composable
fun HealthTrackerNavHost(viewModel: HealthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, null) },
                    label = { Text(L10n.get("nav_record", lang)) },
                    selected = currentRoute == Screen.Add.route,
                    onClick = { navigate(navController, Screen.Add.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text(L10n.get("nav_history", lang)) },
                    selected = currentRoute == Screen.History.route,
                    onClick = { navigate(navController, Screen.History.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text(L10n.get("nav_trends", lang)) },
                    selected = currentRoute == Screen.Stats.route,
                    onClick = { navigate(navController, Screen.Stats.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Menu, null) },
                    label = { Text(L10n.get("nav_ref", lang)) },
                    selected = currentRoute == Screen.Knowledge.route,
                    onClick = { navigate(navController, Screen.Knowledge.route) }
                )
            }
        }
    ) {
 innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Add.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Add.route) { 
                AddScreen(
                    viewModel = viewModel,
                    onSaveSuccess = { 
                        navigate(navController, Screen.Stats.route)
                    }
                ) 
            }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable(Screen.Stats.route) { StatsScreen(viewModel) }
            composable(Screen.Knowledge.route) { KnowledgeScreen(viewModel) } // Passed ViewModel
        }
    }
}

private fun navigate(navController: androidx.navigation.NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
