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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pegasus.ui.viewmodels.AuthViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Sprint 03 — T3.1 / T3.2 / T4.1:
 * Registers a Firebase user, sends the verification email, and saves the
 * extended profile (login email, username, birthdate, address, country, phone,
 * acceptEmails) into the local Room database. The username must be unique.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var username     by remember { mutableStateOf("") }
    var birthdate    by remember { mutableStateOf("") }
    var address      by remember { mutableStateOf("") }
    var country      by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var acceptEmails by remember { mutableStateOf(false) }

    var showDobPicker by remember { mutableStateOf(false) }
    val dobPickerState = rememberDatePickerState()
    val dobFormatter   = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (showDobPicker) {
        DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dobPickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        birthdate = date.format(dobFormatter)
                    }
                    showDobPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDobPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dobPickerState) }
    }

    // On successful registration, go back to login. The verification email is
    // sent in the background by the AuthViewModel.
    LaunchedEffect(uiState.lastAction) {
        if (uiState.lastAction == AuthViewModel.UiState.Action.REGISTER_OK) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Crear cuenta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email *") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Contraseña *") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Nombre de usuario *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = birthdate, onValueChange = {},
                    label = { Text("Fecha de nacimiento") }, readOnly = true, singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDobPicker = true }) {
                            Icon(Icons.Filled.DateRange, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Dirección") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = country, onValueChange = { country = it },
                    label = { Text("País") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Teléfono") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = acceptEmails, onCheckedChange = { acceptEmails = it })
                    Spacer(Modifier.width(4.dp))
                    Text("Acepto recibir comunicaciones por email", fontSize = 13.sp)
                }

                uiState.errorMessage?.let {
                    Text(it, color = colors.error, fontSize = 13.sp)
                }
                uiState.infoMessage?.let {
                    Text(it, color = colors.primary, fontSize = 13.sp)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        authViewModel.register(
                            email = email,
                            password = password,
                            username = username,
                            birthdate = birthdate,
                            address = address,
                            country = country,
                            phone = phone,
                            acceptEmails = acceptEmails
                        )
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
