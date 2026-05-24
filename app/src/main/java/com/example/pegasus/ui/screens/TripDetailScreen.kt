package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.pegasus.R
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.TripImage
import com.example.pegasus.safePopBackStack
import com.example.pegasus.ui.viewmodels.ActivityViewModel
import com.example.pegasus.ui.viewmodels.ReservationViewModel
import com.example.pegasus.ui.viewmodels.TripImageViewModel
import com.example.pegasus.ui.viewmodels.TripViewModel
import java.io.File
import java.time.format.DateTimeFormatter

// ─── TripDetailScreen ──────────────────────────────────────────────────────────
// Shows trip details and the list of activities (itinerary) for that trip.
// Allows navigating to add/edit activities and editing/deleting the trip itself.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    navController: NavController,
    tripViewModel: TripViewModel,
    activityViewModel: ActivityViewModel,
    tripId: String
) {
    val colors = MaterialTheme.colorScheme

    // Sprint 03: trips Flow is hot, only activities still need an explicit load.
    var trip by remember { mutableStateOf<com.example.pegasus.domain.Trip?>(null) }
    val trips by tripViewModel.trips.collectAsState()
    LaunchedEffect(tripId, trips) {
        trip = trips.firstOrNull { it.id == tripId } ?: tripViewModel.getTripById(tripId)
        activityViewModel.loadActivities(tripId)
    }
    val activities by activityViewModel.activities.collectAsState()

    // Sprint 04 — pull the gallery + the reservation (if any) for this trip.
    val tripImageViewModel: TripImageViewModel = hiltViewModel()
    LaunchedEffect(tripId) { tripImageViewModel.setTripId(tripId) }
    val tripImages by tripImageViewModel.images.collectAsState()

    val reservationViewModel: ReservationViewModel = hiltViewModel()
    val allReservations by reservationViewModel.reservations.collectAsState()
    val tripReservation = allReservations.firstOrNull { it.tripId == tripId }

    // ── Delete trip confirmation dialog ────────────────────────────────────────
    var showDeleteTripDialog by remember { mutableStateOf(false) }
    if (showDeleteTripDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTripDialog = false },
            title            = { Text(stringResource(R.string.trip_delete_confirm_title)) },
            text             = { Text(stringResource(R.string.trip_delete_confirm_message)) },
            confirmButton    = {
                TextButton(onClick = {
                    tripViewModel.deleteTrip(tripId)
                    showDeleteTripDialog = false
                    navController.safePopBackStack()
                }) { Text(stringResource(R.string.confirm_delete), color = colors.error) }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteTripDialog = false }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }

    // ── Delete activity confirmation dialog ────────────────────────────────────
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }
    if (activityToDelete != null) {
        AlertDialog(
            onDismissRequest = { activityToDelete = null },
            title            = { Text(stringResource(R.string.activity_delete_confirm_title)) },
            text             = { Text(stringResource(R.string.activity_delete_confirm_message_format, activityToDelete!!.title)) },
            confirmButton    = {
                TextButton(onClick = {
                    activityViewModel.deleteActivity(activityToDelete!!.id, tripId)
                    activityToDelete = null
                }) { Text(stringResource(R.string.confirm_delete), color = colors.error) }
            },
            dismissButton    = {
                TextButton(onClick = { activityToDelete = null }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = trip?.title ?: stringResource(R.string.trip_detail_title_fallback),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.trip_detail_back_description),
                            tint               = colors.primary
                        )
                    }
                },
                actions = {
                    // Edit trip button
                    IconButton(onClick = { navController.navigate("edit_trip/$tripId") }) {
                        Icon(
                            imageVector        = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_trip_icon_description),
                            tint               = colors.primary
                        )
                    }
                    // Delete trip button
                    IconButton(onClick = { showDeleteTripDialog = true }) {
                        Icon(
                            imageVector        = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_trip_icon_description),
                            tint               = colors.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate("add_activity/$tripId") },
                containerColor = colors.primary,
                contentColor   = colors.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_activity_fab_description))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))
                )
                .padding(innerPadding)
        ) {
            // Sprint 03: snapshot the delegated state to a local val so smart-cast works.
            val currentTrip = trip
            if (currentTrip == null) {
                // Trip not found — should not normally happen
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.trip_detail_not_found), color = colors.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Trip info card ─────────────────────────────────────────
                    item {
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = CardDefaults.cardColors(containerColor = colors.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text       = currentTrip.title,
                                    color      = colors.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 20.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(text = "📅", fontSize = 16.sp)
                                    Text(
                                        text     = "${currentTrip.startDate}  →  ${currentTrip.endDate}",
                                        color    = colors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (currentTrip.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text     = currentTrip.description,
                                        color    = colors.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // ── Sprint 04 — Reservation summary (if a hotel was booked) ─
                    if (tripReservation != null) {
                        item {
                            ReservationSummary(
                                reservation = tripReservation,
                                onOpen      = { navController.navigate("reservations") }
                            )
                        }
                    }

                    // ── Sprint 04 — Trip gallery ───────────────────────────────
                    item {
                        TripGallerySection(
                            images     = tripImages,
                            onSeeAll   = { navController.navigate("trip_gallery/$tripId") },
                            onAddOpen  = { navController.navigate("trip_gallery/$tripId") }
                        )
                    }

                    // ── Activities header ──────────────────────────────────────
                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = stringResource(R.string.trip_detail_activities_count, activities.size),
                                color      = colors.onBackground,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 16.sp
                            )
                        }
                    }

                    // ── Empty state ────────────────────────────────────────────
                    if (activities.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text     = stringResource(R.string.trip_detail_no_activities),
                                    color    = colors.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // ── Activity cards ─────────────────────────────────────────
                    items(activities.sortedWith(compareBy({ it.date }, { it.time }))) { activity ->
                        ActivityCard(
                            activity   = activity,
                            onEdit     = { navController.navigate("edit_activity/$tripId/${activity.id}") },
                            onDelete   = { activityToDelete = activity }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ─── Sprint 04 — Reservation summary card ─────────────────────────────────────
@Composable
private fun ReservationSummary(reservation: Reservation, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text       = stringResource(R.string.trip_detail_reservation_section),
                color      = colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (reservation.hotelImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = reservation.hotelImageUrl,
                        contentDescription = reservation.hotelName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "${reservation.hotelName} (${reservation.hotelId})",
                        color = colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text  = stringResource(
                            R.string.trip_detail_reservation_room_format,
                            reservation.roomType, reservation.roomId
                        ),
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text  = stringResource(
                            R.string.trip_detail_reservation_dates_format,
                            reservation.startDate, reservation.endDate, reservation.nights
                        ),
                        color = colors.primary,
                        fontSize = 12.sp
                    )
                    Text(
                        text  = stringResource(
                            R.string.trip_detail_reservation_total_format,
                            reservation.totalPrice
                        ),
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─── Sprint 04 — Trip gallery preview ─────────────────────────────────────────
@Composable
private fun TripGallerySection(
    images: List<TripImage>,
    onSeeAll: () -> Unit,
    onAddOpen: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.trip_gallery_section_title),
                    color = colors.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                TextButton(onClick = onSeeAll) {
                    Text(stringResource(R.string.trip_gallery_view_all))
                }
            }

            if (images.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onAddOpen),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = colors.primary
                    )
                    Text(
                        text = stringResource(R.string.trip_gallery_empty_hint),
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            } else {
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(images.take(8), key = { it.id }) { image ->
                        AsyncImage(
                            model = File(image.localPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 76.dp, height = 76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

// ─── ActivityCard ──────────────────────────────────────────────────────────────
@Composable
private fun ActivityCard(
    activity: Activity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors    = MaterialTheme.colorScheme
    val dateFmt   = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFmt   = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = activity.title,
                    color      = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text     = "📅 ${activity.date.format(dateFmt)}",
                        color    = colors.primary,
                        fontSize = 12.sp
                    )
                    Text(
                        text     = "🕐 ${activity.time.format(timeFmt)}",
                        color    = colors.primary,
                        fontSize = 12.sp
                    )
                }
                if (activity.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = activity.description,
                        color    = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector        = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_activity_icon_description),
                        tint               = colors.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector        = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_activity_icon_description),
                        tint               = colors.error,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
