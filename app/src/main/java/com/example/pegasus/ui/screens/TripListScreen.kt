package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R
import com.example.pegasus.domain.Trip
import com.example.pegasus.ui.viewmodels.TripViewModel

// ─── TripListScreen ────────────────────────────────────────────────────────────
// Displays all trips from TripViewModel (backed by FakeTripDataSource).
// Navigation: tap a trip → TripDetailScreen; FAB → AddEditTripScreen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    navController: NavController,
    tripViewModel: TripViewModel
) {
    val colors = MaterialTheme.colorScheme

    // Reload trips every time this screen enters composition (e.g. after navigating back)
    LaunchedEffect(Unit) { tripViewModel.loadTrips() }

    val trips  by tripViewModel.trips.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val alpha  by animateFloatAsState(
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
            // ── Top bar ────────────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(id = R.string.trip_list_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            // ── Stats row ──────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label    = stringResource(id = R.string.trip_list_stat_total),
                    value    = "${trips.size}",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label    = stringResource(id = R.string.trip_list_stat_upcoming),
                    value    = "${trips.size}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (trips.isEmpty()) {
                // ── Empty state ────────────────────────────────────────────────
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✈️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text     = stringResource(id = R.string.trip_list_empty_title),
                            color    = colors.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = stringResource(id = R.string.trip_list_empty_hint),
                            color    = colors.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // ── Trip list ──────────────────────────────────────────────────
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trips, key = { it.id }) { trip ->
                        TripListCard(
                            trip       = trip,
                            onClick    = { navController.navigate("trip_detail/${trip.id}") }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // ── FAB ────────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = { navController.navigate("add_trip") },
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = colors.primary,
            contentColor   = colors.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.trip_list_new_trip_description))
        }
    }
}

// ─── StatChip ─────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = colors.primary,          fontWeight = FontWeight.Bold,   fontSize = 18.sp)
            Text(text = label, color = colors.onSurfaceVariant, fontWeight = FontWeight.Normal, fontSize = 11.sp)
        }
    }
}

// ─── TripListCard ──────────────────────────────────────────────────────────────
@Composable
private fun TripListCard(trip: Trip, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.weight(1f)
                ) {
                    Text(text = "✈️", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = trip.title,
                            color      = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                        if (trip.description.isNotBlank()) {
                            Text(
                                text     = trip.description,
                                color    = colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                // Date chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primaryContainer.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = "📅",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text     = "${trip.startDate}  →  ${trip.endDate}",
                    color    = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
