package com.example.pegasus.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.pegasus.R
import com.example.pegasus.domain.TripImage
import com.example.pegasus.safePopBackStack
import com.example.pegasus.ui.viewmodels.TripImageViewModel
import java.io.File

/**
 * Sprint 04 T3 — Real per-trip gallery (replaces the Sprint 03 mock).
 *
 * - Uses the Android 13+ Photo Picker so we don't need runtime READ_MEDIA_IMAGES.
 * - Photos are copied into app-internal storage and listed via
 *   [TripImageViewModel.images]. Tap a photo to delete it (with confirmation).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripGalleryScreen(
    navController: NavController,
    tripId: String,
    viewModel: TripImageViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(tripId) { viewModel.setTripId(tripId) }

    val images by viewModel.images.collectAsState()
    val error  by viewModel.errorMessage.collectAsState()

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Sprint 04 T3.1 — OpenDocument(arrayOf("image/*")) launches the SAF
    // (system file browser). It does NOT depend on MediaStore being indexed,
    // so it shows ANY file the user navigates to — Downloads, Pictures,
    // Screenshots, SD card, etc. This is the most robust picker for emulators
    // where the Photo Picker / MediaStore behave unpredictably.
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        Log.d("TripGalleryScreen", "Picker returned uri=$uri tripId=$tripId")
        if (uri != null) viewModel.addImage(tripId, uri)
    }

    var imageToDelete by remember { mutableStateOf<TripImage?>(null) }
    if (imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text(stringResource(R.string.trip_gallery_delete_dialog_title)) },
            text  = { Text(stringResource(R.string.trip_gallery_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteImage(imageToDelete!!)
                    imageToDelete = null
                }) {
                    Text(
                        text  = stringResource(R.string.trip_gallery_delete_dialog_confirm),
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) {
                    Text(stringResource(R.string.trip_gallery_delete_dialog_dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.trip_gallery_title),
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("TripGalleryScreen", "FAB clicked, launching picker")
                    picker.launch(arrayOf("image/*"))
                },
                containerColor = colors.primary,
                contentColor   = colors.onPrimary,
                shape          = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.trip_gallery_add_button))
            }
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
            if (images.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📷", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.trip_gallery_empty_title),
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.trip_gallery_empty_hint),
                        color = colors.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    items(images, key = { it.id }) { image ->
                        PhotoCell(
                            image = image,
                            onClick = { imageToDelete = image }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(image: TripImage, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = File(image.localPath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.trip_gallery_delete_dialog_confirm),
            tint = colors.onPrimary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(colors.primary.copy(alpha = 0.7f), CircleShape)
                .padding(4.dp)
        )
    }
}
