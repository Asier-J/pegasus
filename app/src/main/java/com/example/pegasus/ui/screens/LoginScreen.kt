package com.example.pegasus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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

/**
 * Sprint 03 — T2.2 / T2.3: Login screen backed by AuthViewModel + Firebase Auth.
 * Navigates to "home" on success and offers links to register / recover password.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // On successful login, jump to home and clear back stack.
    LaunchedEffect(uiState.lastAction) {
        if (uiState.lastAction == AuthViewModel.UiState.Action.LOGIN_OK) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pegasus",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Inicia sesión para continuar",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(text = msg, color = colors.error, fontSize = 13.sp)
            }
            uiState.infoMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(text = msg, color = colors.primary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { authViewModel.login(email, password) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("recover") }) {
                Text("¿Has olvidado tu contraseña?")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta?", color = colors.onSurfaceVariant, fontSize = 13.sp)
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Regístrate")
                }
            }
        }
    }
}
