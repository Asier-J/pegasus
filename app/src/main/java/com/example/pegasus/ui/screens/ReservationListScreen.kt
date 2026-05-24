package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.pegasus.domain.Reservation
import com.example.pegasus.safePopBackStack
import com.example.pegasus.ui.viewmodels.ReservationViewModel

/**
 * Sprint 04 T4.1/T4.3 — lists every local reservation with hotel + room images,
 * plus a cancel button (T4.2) which removes it locally and on the server.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(
    navController: NavController,
    viewModel: ReservationViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.colorScheme
    val reservations  by viewModel.reservations.collectAsState()
    val tripTitlesById by viewModel.tripTitlesById.collectAsState()
    val error          by viewModel.errorMessage.collectAsState()

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
    if (reservationToCancel != null) {
        AlertDialog(
            onDismissRequest = { reservationToCancel = null },
            title = { Text(stringResource(R.string.reservations_cancel_dialog_title)) },
            text  = { Text(stringResource(R.string.reservations_cancel_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancel(reservationToCancel!!)
                    reservationToCancel = null
                }) {
                    Text(
                        text  = stringResource(R.string.reservations_cancel_dialog_confirm),
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToCancel = null }) {
                    Text(stringResource(R.string.reservations_cancel_dialog_dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reservations_title),
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
            if (reservations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏨", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reservations_empty_title),
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(R.string.reservations_empty_hint),
                            color = colors.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservations, key = { it.id }) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            tripTitle   = tripTitlesById[reservation.tripId],
                            onClick     = { navController.navigate("trip_detail/${reservation.tripId}") },
                            onCancel    = { reservationToCancel = reservation }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    tripTitle: String?,
    onClick: () -> Unit,
    onCancel: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${reservation.hotelName} (${reservation.hotelId})",
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${reservation.roomType} (${reservation.roomId})",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    // T4.1 — every card "indicates the trip related" by name.
                    Text(
                        text  = stringResource(
                            R.string.reservations_card_trip_format,
                            tripTitle ?: "—"
                        ),
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(
                            R.string.reservations_card_dates_format,
                            reservation.startDate, reservation.endDate
                        ),
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text  = stringResource(R.string.reservations_card_price_format, reservation.totalPrice),
                        color = colors.primary,
                        fontSize = 12.sp
                    )
                    Text(
                        text  = stringResource(R.string.reservations_card_guest_format, reservation.guestName),
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.reservations_cancel_dialog_confirm),
                        tint = colors.error
                    )
                }
            }

            // T4.3 — hotel + room images alongside the data, like the in-class example.
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOfNotNull(reservation.hotelImageUrl, reservation.roomImageUrl)
                    .filter { it.isNotBlank() }
                ) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = reservation.hotelName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 140.dp, height = 90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onClick) {
                Text(stringResource(R.string.trip_detail_reservation_open))
            }
        }
    }
}
