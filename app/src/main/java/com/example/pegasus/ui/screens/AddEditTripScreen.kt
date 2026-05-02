package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R
import com.example.pegasus.safePopBackStack
import com.example.pegasus.ui.viewmodels.TripViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// ─── AddEditTripScreen ─────────────────────────────────────────────────────────
// Handles both creating a new trip (tripId == null) and editing an existing one.
// All date fields use DatePicker — free text entry is disabled.
// Validation is delegated to TripViewModel before data reaches the repository.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTripScreen(
    navController: NavController,
    tripViewModel: TripViewModel,
    tripId: String? = null          // null → create mode; non-null → edit mode
) {
    val colors      = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    // Sprint 03: load existing trip via suspend repository call when editing.
    var existingTrip by remember { mutableStateOf<com.example.pegasus.domain.Trip?>(null) }
    LaunchedEffect(tripId) {
        if (tripId != null) existingTrip = tripViewModel.getTripById(tripId)
    }
    val isEditMode = tripId != null

    // ── Form state ─────────────────────────────────────────────────────────────
    var title       by remember(existingTrip) { mutableStateOf(existingTrip?.title       ?: "") }
    var startDate   by remember(existingTrip) { mutableStateOf(existingTrip?.startDate   ?: "") }
    var endDate     by remember(existingTrip) { mutableStateOf(existingTrip?.endDate     ?: "") }
    var description by remember(existingTrip) { mutableStateOf(existingTrip?.description ?: "") }

    // ── DatePicker dialog state ────────────────────────────────────────────────
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker   by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState   = rememberDatePickerState()

    // ── Error state from ViewModel ─────────────────────────────────────────────
    val errorMessage by tripViewModel.errorMessage.collectAsState()

    // ── Date formatter ─────────────────────────────────────────────────────────
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ── Start DatePicker dialog ────────────────────────────────────────────────
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        startDate = date.format(formatter)
                    }
                    showStartDatePicker = false
                }) { Text(stringResource(R.string.save_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(stringResource(R.string.cancel_button)) }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    // ── End DatePicker dialog ──────────────────────────────────────────────────
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        endDate = date.format(formatter)
                    }
                    showEndDatePicker = false
                }) { Text(stringResource(R.string.save_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text(stringResource(R.string.cancel_button)) }
            }
        ) { DatePicker(state = endDatePickerState) }
    }

    // ── Error snackbar ─────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            tripViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = if (isEditMode) stringResource(R.string.edit_trip_screen_title) else stringResource(R.string.add_trip_screen_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
                            tint               = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
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
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Title field ────────────────────────────────────────────────
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text(stringResource(R.string.trip_field_title)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    isError       = errorMessage != null && title.isBlank(),
                    supportingText = {
                        if (errorMessage != null && title.isBlank())
                            Text(stringResource(R.string.error_title_empty), color = colors.error)
                    }
                )

                // ── Start date field (read-only, opens DatePicker) ─────────────
                OutlinedTextField(
                    value         = startDate,
                    onValueChange = {},
                    label         = { Text(stringResource(R.string.trip_field_start_date)) },
                    readOnly      = true,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    trailingIcon  = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(
                                imageVector        = Icons.Filled.DateRange,
                                contentDescription = stringResource(R.string.select_start_date_description),
                                tint               = colors.primary
                            )
                        }
                    },
                    isError = errorMessage != null && startDate.isBlank(),
                    supportingText = {
                        if (errorMessage != null && startDate.isBlank())
                            Text(stringResource(R.string.error_start_date_empty), color = colors.error)
                    }
                )

                // ── End date field (read-only, opens DatePicker) ───────────────
                OutlinedTextField(
                    value         = endDate,
                    onValueChange = {},
                    label         = { Text(stringResource(R.string.trip_field_end_date)) },
                    readOnly      = true,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    trailingIcon  = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(
                                imageVector        = Icons.Filled.DateRange,
                                contentDescription = stringResource(R.string.select_end_date_description),
                                tint               = colors.primary
                            )
                        }
                    },
                    isError = errorMessage != null && endDate.isBlank(),
                    supportingText = {
                        if (errorMessage != null && endDate.isBlank())
                            Text(stringResource(R.string.error_end_date_empty), color = colors.error)
                    }
                )

                // ── Description field ──────────────────────────────────────────
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text(stringResource(R.string.trip_field_description)) },
                    minLines      = 3,
                    maxLines      = 5,
                    modifier      = Modifier.fillMaxWidth(),
                    isError       = errorMessage != null && description.isBlank(),
                    supportingText = {
                        if (errorMessage != null && description.isBlank())
                            Text(stringResource(R.string.error_description_empty), color = colors.error)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Save button ────────────────────────────────────────────────
                Button(
                    onClick = {
                        // Sprint 03: VM now uses suspend operations and reports success via callback.
                        if (isEditMode && existingTrip != null) {
                            tripViewModel.editTrip(existingTrip!!.id, title, startDate, endDate, description) { ok ->
                                if (ok) navController.safePopBackStack()
                            }
                        } else {
                            tripViewModel.addTrip(title, startDate, endDate, description) { ok ->
                                if (ok) navController.safePopBackStack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(
                        text       = stringResource(R.string.save_button),
                        color      = colors.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    )
                }

                // ── Cancel button ──────────────────────────────────────────────
                OutlinedButton(
                    onClick  = { navController.safePopBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text     = stringResource(R.string.cancel_button),
                        color    = colors.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
