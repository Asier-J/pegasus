package com.example.pegasus

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pegasus.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {

    val bottomNavRoutes = listOf("home", "trips", "map", "ai", "profile")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                PegasusBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ── Bottom nav screens ────────────────────────────────────────────
            composable("home")    { HomeScreen(navController) }
            composable("trips")   { TripListScreen(navController) }
            composable("map")     { MapScreen(navController) }
            composable("ai")      { AIRecommendationScreen(navController) }
            composable("profile") { ProfileScreen(navController) }

            // ── Detail screens (no bottom bar) ────────────────────────────────
            composable("terms")       { TermsAndConditionsScreen(navController) }
            composable("about")       { AboutScreen(navController) }
            composable("preferences") { PreferencesScreen(navController) }
        }
    }
}