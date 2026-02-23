package com.example.pegasus

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pegasus.ui.screens.AboutScreen
import com.example.pegasus.ui.screens.HomeScreen
import com.example.pegasus.ui.screens.PreferencesScreen
import com.example.pegasus.ui.screens.TermsAndConditionsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("terms") { TermsAndConditionsScreen(navController) }
        composable("about") { AboutScreen(navController) }
        composable("preferences") { PreferencesScreen(navController) }
    }
}