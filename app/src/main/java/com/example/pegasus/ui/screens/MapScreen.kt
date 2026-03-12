package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

// ─── Mock Data ────────────────────────────────────────────────────────────────
private data class MockLocation(
    val nameResId: Int,
    val countryResId: Int,
    val emoji: String,
    val type: String,
    val visited: Boolean
)

private val mockLocations = listOf(
    MockLocation(R.string.loc_tokyo,     R.string.country_japan,     "🗼", "activity",  true),
    MockLocation(R.string.loc_paris,     R.string.country_france,    "🗼", "hotel",     false),
    MockLocation(R.string.loc_nyc,       R.string.country_usa,       "🗽", "activity",  true),
    MockLocation(R.string.loc_bali,      R.string.country_indonesia, "🌴", "activity",  true),
    MockLocation(R.string.loc_rome,      R.string.country_italy,     "🏛️", "activity",  false),
    MockLocation(R.string.loc_barcelona, R.string.country_spain,     "🏖️", "restaurant",false),
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme

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
                        text       = stringResource(id = R.string.map_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // ── Map placeholder ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🗺️", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text       = stringResource(id = R.string.map_placeholder_title),
                        color      = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text      = stringResource(id = R.string.todo_map_sdk),
                        color     = colors.onSurfaceVariant,
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 32.dp)
                    )
                }

                // ── Mock markers overlay ──────────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mockLocations.take(3).forEach { loc ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (loc.visited)
                                        colors.primary.copy(alpha = 0.15f)
                                    else
                                        colors.outline.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text     = "${loc.emoji} ${stringResource(id = loc.nameResId)}",
                                color    = if (loc.visited) colors.primary else colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Locations list ────────────────────────────────────────────────
            Text(
                text       = stringResource(id = R.string.map_locations_header),
                color      = colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding      = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mockLocations) { loc ->
                    LocationChip(loc)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ─── Location Chip ────────────────────────────────────────────────────────────
@Composable
private fun LocationChip(loc: MockLocation) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = loc.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(id = loc.nameResId),    color = colors.onSurface,        fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = stringResource(id = loc.countryResId), color = colors.onSurfaceVariant, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (loc.visited) colors.primary.copy(alpha = 0.15f)
                        else colors.outline.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text       = if (loc.visited) stringResource(id = R.string.map_status_visited) else stringResource(id = R.string.map_status_planned),
                    color      = if (loc.visited) colors.primary else colors.onSurfaceVariant,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}