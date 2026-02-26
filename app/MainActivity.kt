package com.example.pegasus

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.compose.rememberNavController
import com.example.pegasus.ui.screens.applyLocaleToContext
import com.example.pegasus.ui.screens.getSavedLanguage
import com.example.pegasus.ui.screens.getSavedTheme
import com.example.pegasus.ui.theme.PegasusTheme

class MainActivity : ComponentActivity() {

    // Aplica el idioma guardado antes de que arranque la Activity
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(applyLocaleToContext(base, getSavedLanguage(base)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica el tema ANTES de inflar cualquier vista
        val isDark = getSavedTheme(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PegasusTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}