package com.example.pegasus

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pegasus.ui.screens.*
import com.example.pegasus.ui.viewmodels.ActivityViewModel
import com.example.pegasus.ui.viewmodels.TripViewModel

@Composable
fun NavGraph(navController: NavHostController) {

    val bottomNavRoutes = listOf("home", "trips", "trip_photo_list", "map", "ai", "profile")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ── Shared ViewModels (activity-scoped via NavGraph) ───────────────────────
    // Both ViewModels are created once and shared across all Trip/Activity screens.
    val tripViewModel: TripViewModel         = viewModel()
    val activityViewModel: ActivityViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                PegasusBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = "splash",
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ── Splash ────────────────────────────────────────────────────────
            composable("splash") { SplashScreen(navController) }

            // ── Bottom nav screens ─────────────────────────────────────────────
            composable("home")    { HomeScreen(navController) }
            composable("trips")   { TripListScreen(navController, tripViewModel) }
            composable("map")     { MapScreen(navController) }
            composable("ai")      { AIRecommendationScreen(navController) }
            composable("profile") { ProfileScreen(navController) }

            // ── Trip CRUD screens ──────────────────────────────────────────────

            // Add new trip
            composable("add_trip") {
                AddEditTripScreen(
                    navController = navController,
                    tripViewModel = tripViewModel,
                    tripId        = null
                )
            }

            // Edit existing trip
            composable(
                route     = "edit_trip/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")
                AddEditTripScreen(
                    navController = navController,
                    tripViewModel = tripViewModel,
                    tripId        = tripId
                )
            }

            // Trip detail (shows trip info + activity list)
            composable(
                route     = "trip_detail/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                TripDetailScreen(
                    navController     = navController,
                    tripViewModel     = tripViewModel,
                    activityViewModel = activityViewModel,
                    tripId            = tripId
                )
            }

            // ── Activity CRUD screens ──────────────────────────────────────────

            // Add new activity to a trip
            composable(
                route     = "add_activity/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                AddEditActivityScreen(
                    navController     = navController,
                    activityViewModel = activityViewModel,
                    tripId            = tripId,
                    activityId        = null
                )
            }

            // Edit existing activity
            composable(
                route     = "edit_activity/{tripId}/{activityId}",
                arguments = listOf(
                    navArgument("tripId")     { type = NavType.StringType },
                    navArgument("activityId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tripId     = backStackEntry.arguments?.getString("tripId")     ?: return@composable
                val activityId = backStackEntry.arguments?.getString("activityId") ?: return@composable
                AddEditActivityScreen(
                    navController     = navController,
                    activityViewModel = activityViewModel,
                    tripId            = tripId,
                    activityId        = activityId
                )
            }

            // ── Detail screens (no bottom bar) ─────────────────────────────────
            composable("terms")       { TermsAndConditionsScreen(navController) }
            composable("about")       { AboutScreen(navController) }
            composable("preferences") { PreferencesScreen(navController) }
            composable("trip_photo_list") { TripPhotoListScreen(navController) }
            composable(
                route     = "trip_gallery/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.IntType })
            ) {
                val tripId = it.arguments?.getInt("tripId") ?: 1
                TripGalleryScreen(navController, tripId)
            }
        }
    }
}
