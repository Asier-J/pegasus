package com.example.pegasus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pegasus.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val colors = MaterialTheme.colorScheme

    // ── Animation states ──────────────────────────────────────────────────────
    var logoVisible   by remember { mutableStateOf(false) }
    var titleVisible  by remember { mutableStateOf(false) }
    var loadingVisible by remember { mutableStateOf(false) }
    var versionVisible by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue   = if (logoVisible) 1f else 0f,
        animationSpec = tween(600),
        label         = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue   = if (logoVisible) 1f else 0.7f,
        animationSpec = tween(600, easing = EaseOutBack),
        label         = "logoScale"
    )
    val titleAlpha by animateFloatAsState(
        targetValue   = if (titleVisible) 1f else 0f,
        animationSpec = tween(500),
        label         = "titleAlpha"
    )
    val loadingAlpha by animateFloatAsState(
        targetValue   = if (loadingVisible) 1f else 0f,
        animationSpec = tween(400),
        label         = "loadingAlpha"
    )
    val versionAlpha by animateFloatAsState(
        targetValue   = if (versionVisible) 1f else 0f,
        animationSpec = tween(400),
        label         = "versionAlpha"
    )

    // ── Loading bar progress ──────────────────────────────────────────────────
    val progress = remember { Animatable(0f) }

    // ── Sequence ──────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(100)
        logoVisible = true
        delay(400)
        titleVisible = true
        delay(300)
        loadingVisible = true
        versionVisible = true
        progress.animateTo(
            targetValue   = 1f,
            animationSpec = tween(1800, easing = EaseInOutCubic)
        )
        delay(200)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Logo ──────────────────────────────────────────────────────────
            Text(
                text     = "🐴",
                fontSize = 80.sp,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── App name ──────────────────────────────────────────────────────
            Text(
                text          = stringResource(R.string.home_app_title),
                color         = colors.onBackground,
                fontSize      = 34.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                modifier      = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Divider line ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(titleAlpha)
                    .width(48.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary)),
                        RoundedCornerShape(1.dp)
                    )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Loading bar ───────────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .alpha(loadingAlpha)
                    .width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.outline.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(colors.primaryContainer, colors.primary)
                                )
                            )
                    )
                }
            }
        }

        // ── Version ───────────────────────────────────────────────────────────
        Text(
            text     = stringResource(R.string.about_version),
            color    = colors.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(versionAlpha)
        )
    }
}