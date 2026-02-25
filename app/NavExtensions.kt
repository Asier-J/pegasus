package com.example.pegasus

import androidx.navigation.NavController

fun NavController.safePopBackStack() {
    if (previousBackStackEntry != null) popBackStack()
}