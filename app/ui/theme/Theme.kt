package com.example.pegasus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Dark palette (naval) ─────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF42A5F5), // SkyBlue
    onPrimary        = Color(0xFF0A1628), // NavyDeep
    primaryContainer = Color(0xFF1565C0), // AzureBlue
    secondary        = Color(0xFF42A5F5),
    background       = Color(0xFF0A1628), // NavyDeep
    onBackground     = Color(0xFFF0F6FF), // IceWhite
    surface          = Color(0xFF112240), // CardBg
    onSurface        = Color(0xFFF0F6FF), // IceWhite
    surfaceVariant   = Color(0xFF102040), // NavyMid
    onSurfaceVariant = Color(0xFFB0BEC5), // SubtleGray
    outline          = Color(0xFF1E3A5F),
)

// ─── Light palette (azul muy claro, identidad naval) ─────────────────────────
private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF1565C0), // AzureBlue
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF42A5F5), // SkyBlue
    secondary        = Color(0xFF1565C0),
    background       = Color(0xFFE8F1FB), // azul muy claro
    onBackground     = Color(0xFF0A1628), // NavyDeep — texto oscuro
    surface          = Color(0xFFD0E4F7), // cards azul claro
    onSurface        = Color(0xFF0D1F38), // texto oscuro sobre cards
    surfaceVariant   = Color(0xFFBDD5EF),
    onSurfaceVariant = Color(0xFF2E5070), // subtítulos
    outline          = Color(0xFF90B8D8),
)

@Composable
fun PegasusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}