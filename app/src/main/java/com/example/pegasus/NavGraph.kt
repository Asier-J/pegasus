package com.example.pegasus

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pegasus.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {

    val bottomNavRoutes = listOf("home", "trips",  "trip_photo_list", "map", "ai", "profile")
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
            startDestination = "splash",          // ← empieza en splash
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ── Splash ────────────────────────────────────────────────────────
            composable("splash") { SplashScreen(navController) }

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
            composable("trip_photo_list") { TripPhotoListScreen(navController) }
            composable("trip_gallery/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.IntType })
            ) {
                val tripId = it.arguments?.getInt("tripId") ?: 1
                TripGalleryScreen(navController, tripId)
            }
        }
    }
}