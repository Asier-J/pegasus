package com.example.pegasus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pegasus.R

@Composable
fun HomeScreen(navController: NavHostController) {
    val colors = MaterialTheme.colorScheme

    // ── Entry animation ───────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label         = "alpha"
    )
    val translateY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "translateY"
    )

    // ── Subtle emoji pulse ────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .alpha(alpha)
                .offset(y = translateY.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Hero ──────────────────────────────────────────────────────────
            Text(text = "🐴", fontSize = 72.sp, modifier = Modifier.scale(emojiScale))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text          = stringResource(id = R.string.home_app_title),
                color         = colors.onBackground,
                fontSize      = 30.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text          = stringResource(id = R.string.home_app_subtitle),
                color         = colors.primary,
                fontSize      = 14.sp,
                fontWeight    = FontWeight.Light,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Divider ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary)),
                        RoundedCornerShape(1.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = stringResource(id = R.string.home_app_description),
                color      = colors.onSurfaceVariant,
                fontSize   = 13.sp,
                textAlign  = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Buttons ───────────────────────────────────────────────────────
            PegasusButton(
                text    = stringResource(id = R.string.home_terms_button),
                onClick = { navController.navigate("terms") },
                primary = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            PegasusButton(
                text    = stringResource(id = R.string.home_about_button),
                onClick = { navController.navigate("about") },
                primary = false
            )
        }

        FloatingActionButton(
            onClick        = { navController.navigate("preferences") },
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape          = CircleShape,
            containerColor = colors.primary,
            contentColor   = colors.onPrimary
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }
}

// ─── Reusable Button ──────────────────────────────────────────────────────────
@Composable
private fun PegasusButton(text: String, onClick: () -> Unit, primary: Boolean) {
    val colors = MaterialTheme.colorScheme

    if (primary) {
        Button(
            onClick        = onClick,
            modifier       = Modifier.fillMaxWidth().height(52.dp),
            shape          = RoundedCornerShape(14.dp),
            colors         = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary)),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text          = text,
                    color         = colors.onPrimary,
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    } else {
        OutlinedButton(
            onClick  = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
            border   = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary))
            )
        ) {
            Text(
                text          = text,
                color         = colors.primary,
                fontWeight    = FontWeight.SemiBold,
                fontSize      = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}