package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.pegasus.domain.Hotel
import com.example.pegasus.domain.Room
import com.example.pegasus.safePopBackStack
import com.example.pegasus.ui.viewmodels.HotelViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Sprint 04 T2.3/T2.4 — Hotel detail screen.
 *
 * Loads the hotel from the search-screen cache, displays its image carousel and
 * every room (with all room images + price). Tapping "Reserve" books the room
 * via [HotelViewModel.bookRoom], which also persists the local Trip + Reservation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    navController: NavController,
    hotelId: String,
    viewModel: HotelViewModel = hiltViewModel()
) {
    val colors    = MaterialTheme.colorScheme

    val hotel     = viewModel.hotelById(hotelId)
    val startDate by viewModel.startDate.collectAsState()
    val endDate   by viewModel.endDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.errorMessage.collectAsState()
    val booking   by viewModel.lastBooking.collectAsState()

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Confirmation dialog after a successful booking.
    if (booking != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearLastBooking() },
            title = { Text(stringResource(R.string.hotel_detail_booking_ok)) },
            text = {
                Text(
                    stringResource(
                        R.string.hotel_detail_booking_ok_message,
                        booking!!.id,
                        booking!!.nights
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLastBooking()
                    navController.navigate("reservations") {
                        launchSingleTop = true
                    }
                }) { Text(stringResource(R.string.hotel_detail_view_bookings)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearLastBooking() }) {
                    Text(stringResource(R.string.hotel_detail_dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = hotel?.let { "${it.name} (${it.id})" }
                            ?: stringResource(R.string.hotel_search_title),
                        color = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.hotel_detail_back),
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))
                )
                .padding(padding)
        ) {
            if (hotel == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.trip_detail_not_found),
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        AsyncImage(
                            model = hotel.imageUrl,
                            contentDescription = hotel.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceVariant)
                        )
                    }

                    item {
                        Column {
                            Text(
                                text = hotel.address,
                                color = colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Text(
                                text = stringResource(R.string.hotel_search_rating, hotel.rating),
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (startDate.isNotBlank() && endDate.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.hotel_detail_stay_format,
                                        startDate, endDate
                                    ),
                                    color = colors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (hotel.rooms.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.hotel_detail_no_rooms),
                                color = colors.onSurfaceVariant
                            )
                        }
                    }

                    val nights = nightsBetween(startDate, endDate)
                    items(hotel.rooms, key = { it.id }) { room ->
                        RoomCard(
                            hotel     = hotel,
                            room      = room,
                            nights    = nights,
                            loading   = isLoading,
                            onReserve = { viewModel.bookRoom(hotel, room) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Parses ISO dates and returns the number of nights between them, or 0 when
 * either field is missing/invalid. Used to display the booking total per room.
 */
private fun nightsBetween(startIso: String, endIso: String): Int {
    if (startIso.isBlank() || endIso.isBlank()) return 0
    return try {
        val s = LocalDate.parse(startIso, DateTimeFormatter.ISO_LOCAL_DATE)
        val e = LocalDate.parse(endIso,   DateTimeFormatter.ISO_LOCAL_DATE)
        ChronoUnit.DAYS.between(s, e).toInt().coerceAtLeast(0)
    } catch (_: Exception) {
        0
    }
}

@Composable
private fun RoomCard(
    hotel: Hotel,
    room: Room,
    nights: Int,
    loading: Boolean,
    onReserve: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${room.roomType} (${room.id})",
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.hotel_detail_room_price, room.price),
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (room.images.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(room.images) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "${hotel.name} ${room.id}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 160.dp, height = 100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant)
                        )
                    }
                }
            }

            // PDF example p.55 — booking total per room (= price × nights) when
            // the user has picked both dates.
            if (nights > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.hotel_detail_room_total, room.price * nights),
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onReserve,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.hotel_detail_reserve_button))
            }
        }
    }
}
