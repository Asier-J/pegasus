package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.pegasus.R
import com.example.pegasus.safePopBackStack
import com.example.pegasus.ui.viewmodels.ActivityViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// ─── AddEditActivityScreen ─────────────────────────────────────────────────────
// Handles both creating a new activity and editing an existing one.
// Date field uses DatePicker; time field uses TimePicker — free text is disabled.
// Validates that the activity date is within the trip's date range.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActivityScreen(
    navController: NavController,
    activityViewModel: ActivityViewModel,
    tripId: String,
    activityId: String? = null   // null → create mode; non-null → edit mode
) {
    val colors      = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    // Pre-fill if editing
    val existingActivity = remember(activityId) {
        activityId?.let { activityViewModel.getActivityById(it) }
    }
    val isEditMode = existingActivity != null

    // ── Form state ─────────────────────────────────────────────────────────────
    var title       by remember { mutableStateOf(existingActivity?.title       ?: "") }
    var description by remember { mutableStateOf(existingActivity?.description ?: "") }
    var date        by remember { mutableStateOf<LocalDate?>(existingActivity?.date) }
    var time        by remember { mutableStateOf<LocalTime?>(existingActivity?.time) }

    val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    // ── Dialog state ───────────────────────────────────────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour   = existingActivity?.time?.hour   ?: 9,
        initialMinute = existingActivity?.time?.minute ?: 0
    )

    // ── Error state ────────────────────────────────────────────────────────────
    val errorMessage by activityViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            activityViewModel.clearError()
        }
    }

    // ── Date picker dialog ─────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.save_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel_button)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // ── Time picker dialog (custom AlertDialog wrapping TimePicker) ────────────
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.time_picker_title),
                        color      = colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.cancel_button))
                        }
                        TextButton(onClick = {
                            time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) {
                            Text(stringResource(R.string.save_button))
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = if (isEditMode) stringResource(R.string.edit_activity_screen_title) else stringResource(R.string.add_activity_screen_title),
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
                // ── Title ──────────────────────────────────────────────────────
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text(stringResource(R.string.activity_field_title)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    isError       = errorMessage != null && title.isBlank(),
                    supportingText = {
                        if (errorMessage != null && title.isBlank())
                            Text(stringResource(R.string.error_activity_title_empty), color = colors.error)
                    }
                )

                // ── Description ────────────────────────────────────────────────
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text(stringResource(R.string.activity_field_description)) },
                    minLines      = 2,
                    maxLines      = 4,
                    modifier      = Modifier.fillMaxWidth()
                )

                // ── Date (read-only, opens DatePicker) ─────────────────────────
                OutlinedTextField(
                    value         = date?.format(dateFmt) ?: "",
                    onValueChange = {},
                    label         = { Text(stringResource(R.string.activity_field_date)) },
                    readOnly      = true,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    trailingIcon  = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector        = Icons.Filled.DateRange,
                                contentDescription = stringResource(R.string.select_date_description),
                                tint               = colors.primary
                            )
                        }
                    },
                    isError = errorMessage != null && date == null,
                    supportingText = {
                        if (errorMessage != null && date == null)
                            Text(stringResource(R.string.error_activity_date_empty), color = colors.error)
                    }
                )

                // ── Time (read-only, opens TimePicker) ─────────────────────────
                OutlinedTextField(
                    value         = time?.format(timeFmt) ?: "",
                    onValueChange = {},
                    label         = { Text(stringResource(R.string.activity_field_time)) },
                    readOnly      = true,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    trailingIcon  = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector        = Icons.Filled.Schedule,
                                contentDescription = stringResource(R.string.select_time_description),
                                tint               = colors.primary
                            )
                        }
                    },
                    isError = errorMessage != null && time == null,
                    supportingText = {
                        if (errorMessage != null && time == null)
                            Text(stringResource(R.string.error_activity_time_empty), color = colors.error)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Save button ────────────────────────────────────────────────
                Button(
                    onClick = {
                        val success = if (isEditMode) {
                            activityViewModel.updateActivity(
                                existingActivity!!.id, tripId, title, description, date, time
                            )
                        } else {
                            activityViewModel.addActivity(tripId, title, description, date, time)
                        }
                        if (success) navController.safePopBackStack()
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
