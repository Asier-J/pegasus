package com.example.pegasus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pegasus.R

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────
sealed class BottomNavItem(val route: String, val icon: ImageVector, val labelResId: Int) {
    object Home    : BottomNavItem("home",    Icons.Filled.Home,       R.string.nav_home)
    object Trips   : BottomNavItem("trips",   Icons.Filled.TravelExplore, R.string.nav_trips)
    object Map     : BottomNavItem("map",     Icons.Filled.Map,      R.string.nav_map)
    object AI      : BottomNavItem("ai",      Icons.Filled.Star,       R.string.nav_ai)
    object Profile : BottomNavItem("profile", Icons.Filled.Person,     R.string.nav_profile)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Trips,
    BottomNavItem.Map,
    BottomNavItem.AI,
    BottomNavItem.Profile
)

// ─── HomeScreen ───────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(navController: NavHostController) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label         = "alpha"
    )
    val translateY by animateFloatAsState(
        targetValue   = if (visible) 0f else 30f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "translateY"
    )

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

    val greeting = remember {
        val array = context.resources.getStringArray(R.array.home_greetings)
        array.random()
    }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .alpha(alpha)
                .offset(y = translateY.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(text = "🐴", fontSize = 72.sp, modifier = Modifier.scale(emojiScale))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text          = stringResource(id = R.string.home_app_title),
                color         = colors.onBackground,
                fontSize      = 30.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                text       = greeting,
                color      = colors.onSurfaceVariant,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────
@Composable
fun PegasusBottomBar(navController: NavHostController) {
    val colors = MaterialTheme.colorScheme
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val label = stringResource(id = item.labelResId)
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text     = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = colors.primary,
                    selectedTextColor   = colors.primary,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                    indicatorColor      = colors.primaryContainer.copy(alpha = 0.2f)
                )
            )
        }
    }
}