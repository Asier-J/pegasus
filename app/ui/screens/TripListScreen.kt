package com.example.pegasus.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

// ─── Mock Data ────────────────────────────────────────────────────────────────
private data class MockTrip(
    val id: String,
    val titleResId: Int,
    val destinationResId: Int,
    val datesResId: Int,
    val status: String,
    val budget: Double,
    val spent: Double,
    val emoji: String
)

private val mockTrips = listOf(
    MockTrip("1", R.string.trip_1_title, R.string.trip_1_dest, R.string.trip_1_dates, "planned",   2500.0, 800.0,  "🗼"),
    MockTrip("2", R.string.trip_2_title, R.string.trip_2_dest, R.string.trip_2_dates, "planned",   1800.0, 200.0,  "🗼"),
    MockTrip("3", R.string.trip_3_title, R.string.trip_3_dest, R.string.trip_3_dates, "completed", 1200.0, 1150.0, "🗽"),
    MockTrip("4", R.string.trip_4_title, R.string.trip_4_dest, R.string.trip_4_dates, "completed", 900.0,  870.0,  "🌴"),
    MockTrip("5", R.string.trip_5_title, R.string.trip_5_dest, R.string.trip_5_dates, "planned",   2200.0, 0.0,    "🏛️"),
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val todoCreate = stringResource(id = R.string.todo_trip_create)

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
            // ── Top Bar ──────────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(id = R.string.trip_list_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, todoCreate, Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            imageVector        = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.trip_list_new_trip_description),
                            tint               = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            // ── Stats row ─────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(label = stringResource(id = R.string.trip_list_stat_total), value = "${mockTrips.size}", modifier = Modifier.weight(1f))
                StatChip(label = stringResource(id = R.string.trip_list_stat_planned), value = "${mockTrips.count { it.status == "planned" }}", modifier = Modifier.weight(1f))
                StatChip(label = stringResource(id = R.string.trip_list_stat_completed), value = "${mockTrips.count { it.status == "completed" }}", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Trip list ─────────────────────────────────────────────────────
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockTrips) { trip ->
                    TripCard(trip = trip)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = { Toast.makeText(context, todoCreate, Toast.LENGTH_SHORT).show() },
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

// ─── Trip Card ────────────────────────────────────────────────────────────────
@Composable
private fun TripCard(trip: MockTrip) {
    val colors   = MaterialTheme.colorScheme
    val context  = LocalContext.current
    val progress = if (trip.budget > 0) (trip.spent / trip.budget).toFloat() else 0f
    val statusColor = if (trip.status == "completed") colors.primary else colors.primaryContainer
    val todoDetail = stringResource(id = R.string.todo_trip_detail)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { Toast.makeText(context, todoDetail, Toast.LENGTH_SHORT).show() },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = trip.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = stringResource(id = trip.titleResId),
                            color      = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                        Text(
                            text     = stringResource(id = trip.destinationResId),
                            color    = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val statusText = if (trip.status == "planned") {
                        stringResource(id = R.string.trip_list_status_planned)
                    } else {
                        stringResource(id = R.string.trip_list_status_completed)
                    }
                    Text(
                        text       = statusText,
                        color      = statusColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = trip.datesResId), color = colors.onSurfaceVariant, fontSize = 12.sp)
                Text(
                    text       = stringResource(id = R.string.trip_list_budget_format, trip.spent.toInt(), trip.budget.toInt()),
                    color      = colors.onSurfaceVariant,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress      = { progress },
                modifier      = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color         = colors.primary,
                trackColor    = colors.outline.copy(alpha = 0.3f)
            )
        }
    }
}

// ─── Stat Chip ────────────────────────────────────────────────────────────────
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
            modifier            = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = colors.primary,          fontWeight = FontWeight.Bold,    fontSize = 18.sp)
            Text(text = label, color = colors.onSurfaceVariant, fontWeight = FontWeight.Normal,  fontSize = 11.sp)
        }
    }
}