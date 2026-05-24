package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.DateRange
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
import com.example.pegasus.ui.viewmodels.HotelViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Sprint 04 T2.1/T2.2 — Hotel search screen.
 *
 * Lets the user choose a city + check-in / check-out dates, calls the
 * `/availability` endpoint via [HotelViewModel] and renders the result. Tapping
 * a hotel card opens [HotelDetailScreen] to book a specific room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelSearchScreen(
    navController: NavController,
    viewModel: HotelViewModel = hiltViewModel()
) {
    val colors    = MaterialTheme.colorScheme

    val city      by viewModel.city.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate   by viewModel.endDate.collectAsState()
    val hotels    by viewModel.hotels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.errorMessage.collectAsState()

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }
    val startState      = rememberDatePickerState()
    val endState        = rememberDatePickerState()
    val isoFmt          = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.setStartDate(date.format(isoFmt))
                    }
                    showStartPicker = false
                }) { Text(stringResource(R.string.save_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        ) { DatePicker(state = startState) }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.setEndDate(date.format(isoFmt))
                    }
                    showEndPicker = false
                }) { Text(stringResource(R.string.save_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        ) { DatePicker(state = endState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.hotel_search_title),
                        color = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("reservations") }) {
                        Icon(
                            imageVector = Icons.Filled.BookOnline,
                            contentDescription = stringResource(R.string.nav_reservations),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))
                )
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CityDropdown(selected = city, onSelect = viewModel::selectCity)

            OutlinedTextField(
                value         = startDate,
                onValueChange = {},
                readOnly      = true,
                label         = { Text(stringResource(R.string.hotel_search_start_date)) },
                trailingIcon  = {
                    IconButton(onClick = { showStartPicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.select_start_date_description))
                    }
                },
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = endDate,
                onValueChange = {},
                readOnly      = true,
                label         = { Text(stringResource(R.string.hotel_search_end_date)) },
                trailingIcon  = {
                    IconButton(onClick = { showEndPicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.select_end_date_description))
                    }
                },
                modifier      = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::searchHotels,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                enabled  = !isLoading
            ) {
                Text(stringResource(R.string.hotel_search_button))
            }

            when {
                isLoading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.hotel_search_loading),
                            color = colors.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                hotels.isEmpty() && startDate.isBlank() -> EmptyHint(
                    title = stringResource(R.string.hotel_search_initial_hint),
                    colors = colors
                )

                hotels.isEmpty() -> EmptyHint(
                    title = stringResource(R.string.hotel_search_empty),
                    colors = colors
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(hotels, key = { it.id }) { hotel ->
                        HotelCard(
                            hotel = hotel,
                            onClick = { navController.navigate("hotel_detail/${hotel.id}") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    selected: HotelViewModel.City,
    onSelect: (HotelViewModel.City) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cityNames = mapOf(
        HotelViewModel.City.BCN to stringResource(R.string.hotel_search_city_bcn),
        HotelViewModel.City.PAR to stringResource(R.string.hotel_search_city_par),
        HotelViewModel.City.LON to stringResource(R.string.hotel_search_city_lon)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = cityNames[selected].orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.hotel_search_city_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HotelViewModel.City.values().forEach { c ->
                DropdownMenuItem(
                    text = { Text(cityNames[c].orEmpty()) },
                    onClick = {
                        onSelect(c)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyHint(title: String, colors: ColorScheme) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = title,
            color = colors.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun HotelCard(hotel: Hotel, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${hotel.name} (${hotel.id})",
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = hotel.address,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                val cheapest = hotel.rooms.minByOrNull { it.price }
                if (cheapest != null) {
                    Text(
                        text = stringResource(R.string.hotel_search_from_price, cheapest.price),
                        color = colors.primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = stringResource(R.string.hotel_search_rooms_count, hotel.rooms.size),
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
