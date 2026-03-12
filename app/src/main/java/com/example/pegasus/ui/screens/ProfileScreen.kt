package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

// ─── Mock Data ────────────────────────────────────────────────────────────────
private data class MockUser(
    val name: String,
    val email: String,
    val tripsCount: Int,
    val countriesCount: Int,
    val kmTravelled: Int
)

private val mockUser = MockUser(
    name            = "Asier Juárez",
    email           = "asier@pegasus.app",
    tripsCount      = 5,
    countriesCount  = 8,
    kmTravelled     = 24300
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val colors      = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "fade"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(id = R.string.profile_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Avatar + Name ─────────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.primary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = mockUser.name.first().toString(),
                                color      = colors.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text       = mockUser.name,
                                color      = colors.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 16.sp
                            )
                            Text(
                                text     = mockUser.email,
                                color    = colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // ── Stats ─────────────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard("✈️", "${mockUser.tripsCount}",      stringResource(id = R.string.profile_stat_trips),     Modifier.weight(1f))
                    ProfileStatCard("🌍", "${mockUser.countriesCount}",  stringResource(id = R.string.profile_stat_countries), Modifier.weight(1f))
                    ProfileStatCard("📍", "${mockUser.kmTravelled / 1000}k", stringResource(id = R.string.profile_stat_km),    Modifier.weight(1f))
                }

                // ── Settings section ──────────────────────────────────────────
                ProfileSection(title = stringResource(id = R.string.profile_section_settings)) {
                    ProfileRow(
                        icon    = Icons.Filled.Tune,
                        label   = stringResource(id = R.string.profile_row_preferences),
                        sublabel = stringResource(id = R.string.profile_row_preferences_sub),
                        onClick = { navController.navigate("preferences") }
                    )
                }

                // ── App section ───────────────────────────────────────────────
                ProfileSection(title = stringResource(id = R.string.profile_section_app)) {
                    ProfileRow(
                        icon     = Icons.Filled.Info,
                        label    = stringResource(id = R.string.profile_row_about),
                        sublabel = stringResource(id = R.string.profile_row_about_sub),
                        onClick  = { navController.navigate("about") }
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 4.dp))
                    ProfileRow(
                        icon     = Icons.Filled.Description,
                        label    = stringResource(id = R.string.profile_row_terms),
                        sublabel = stringResource(id = R.string.profile_row_terms_sub),
                        onClick  = { navController.navigate("terms") }
                    )
                }

                // ── Account section ───────────────────────────────────────────
                ProfileSection(title = stringResource(id = R.string.profile_section_account)) {
                    ProfileRow(
                        icon     = Icons.Filled.Logout,
                        label    = stringResource(id = R.string.profile_row_logout),
                        sublabel = stringResource(id = R.string.profile_row_logout_sub),
                        onClick  = { /* @TODO implement logout */ },
                        tintRed  = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────
@Composable
private fun ProfileStatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji,  fontSize = 20.sp)
            Text(text = value,  color = colors.primary,          fontWeight = FontWeight.Bold,   fontSize = 16.sp)
            Text(text = label,  color = colors.onSurfaceVariant, fontWeight = FontWeight.Normal, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text       = title.uppercase(),
            color      = colors.onSurfaceVariant,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier   = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ProfileRow(
    icon:     ImageVector,
    label:    String,
    sublabel: String,
    onClick:  () -> Unit,
    tintRed:  Boolean = false
) {
    val colors    = MaterialTheme.colorScheme
    val iconColor = if (tintRed) Color(0xFFE53935) else colors.primary

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label,    color = if (tintRed) iconColor else colors.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(text = sublabel, color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint               = colors.onSurfaceVariant,
            modifier           = Modifier.size(16.dp)
        )
    }
}