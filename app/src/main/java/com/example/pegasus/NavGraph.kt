package com.example.pegasus

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pegasus.ui.screens.*
import com.example.pegasus.ui.viewmodels.ActivityViewModel
import com.example.pegasus.ui.viewmodels.AuthViewModel
import com.example.pegasus.ui.viewmodels.HotelViewModel
import com.example.pegasus.ui.viewmodels.TripViewModel

/**
 * Sprint 03: Adds auth routes (login / register / recover) and an auth guard
 * that redirects unauthenticated users to "login" while preserving the rest
 * of the navigation graph. ViewModels are now Hilt-injected via `hiltViewModel()`.
 */
@Composable
fun NavGraph(navController: NavHostController) {

    val bottomNavRoutes = listOf("home", "trips", "hotel_search", "map", "ai", "profile")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ── Auth state guard ───────────────────────────────────────────────────────
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val authRoutes = setOf("login", "register", "recover", "splash")

    // If session ends while the user is on a protected screen, bounce to login.
    // Note: ProfileScreen also navigates explicitly on logout — this guard is the
    // safety net for any other case (token expiry, deletion in Firebase Console…).
    LaunchedEffect(currentUser, currentRoute) {
        if (currentUser == null && currentRoute != null && currentRoute !in authRoutes) {
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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

            // ── Auth screens (Sprint 03) ──────────────────────────────────────
            composable("login")    { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("recover")  { RecoverPasswordScreen(navController) }

            // ── Bottom nav screens ─────────────────────────────────────────────
            composable("home")    { HomeScreen(navController) }
            composable("trips")   {
                val tripViewModel: TripViewModel = hiltViewModel()
                TripListScreen(navController, tripViewModel)
            }
            composable("map")     { MapScreen(navController) }
            composable("ai")      { AIRecommendationScreen(navController) }
            composable("profile") { ProfileScreen(navController) }

            // ── Trip CRUD screens ──────────────────────────────────────────────
            composable("add_trip") {
                AddEditTripScreen(
                    navController = navController,
                    tripViewModel = hiltViewModel(),
                    tripId        = null
                )
            }
            composable(
                route     = "edit_trip/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")
                AddEditTripScreen(
                    navController = navController,
                    tripViewModel = hiltViewModel(),
                    tripId        = tripId
                )
            }
            composable(
                route     = "trip_detail/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                TripDetailScreen(
                    navController     = navController,
                    tripViewModel     = hiltViewModel(),
                    activityViewModel = hiltViewModel<ActivityViewModel>(),
                    tripId            = tripId
                )
            }

            // ── Activity CRUD screens ──────────────────────────────────────────
            composable(
                route     = "add_activity/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                AddEditActivityScreen(
                    navController     = navController,
                    activityViewModel = hiltViewModel(),
                    tripId            = tripId,
                    activityId        = null
                )
            }
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
                    activityViewModel = hiltViewModel(),
                    tripId            = tripId,
                    activityId        = activityId
                )
            }

            // ── Detail screens (no bottom bar) ─────────────────────────────────
            composable("terms")           { TermsAndConditionsScreen(navController) }
            composable("about")           { AboutScreen(navController) }
            composable("preferences")     { PreferencesScreen(navController) }

            // Sprint 04 — Hotels (remote) + Reservations + Trip gallery (T2/T3/T4)
            //
            // HotelDetailScreen shares the SearchScreen's HotelViewModel so the
            // already-loaded hotel list is reused (otherwise `hotelById` returns
            // null on the detail screen and the user sees an empty page).
            composable("hotel_search") {
                val parent = remember(it) { navController.getBackStackEntry("hotel_search") }
                HotelSearchScreen(navController, hiltViewModel<HotelViewModel>(parent))
            }
            composable(
                route     = "hotel_detail/{hotelId}",
                arguments = listOf(navArgument("hotelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hotelId = backStackEntry.arguments?.getString("hotelId") ?: return@composable
                val parent  = remember(backStackEntry) { navController.getBackStackEntry("hotel_search") }
                HotelDetailScreen(navController, hotelId, hiltViewModel<HotelViewModel>(parent))
            }
            composable("reservations") { ReservationListScreen(navController) }
            composable(
                route     = "trip_gallery/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) {
                val tripId = it.arguments?.getString("tripId") ?: return@composable
                TripGalleryScreen(navController, tripId)
            }
        }
    }
}
