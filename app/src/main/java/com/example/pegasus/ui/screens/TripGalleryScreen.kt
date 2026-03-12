package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripGalleryScreen(
    navController: NavController,
    tripId: Int
) {
    val trip = remember(tripId) { mockPhotoTrips.find { it.id == tripId } }
    var backEnabled by remember { mutableStateOf(true) }

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "fade"
    )

    var photos by remember { mutableStateOf(trip?.photos ?: emptyList()) }

    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (trip != null) stringResource(id = trip.nameResId) else stringResource(id = R.string.gallery_default_title),
                        color = Color(0xFFF0F6FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (backEnabled) {
                                backEnabled = false
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.gallery_back_button), tint = Color(0xFF42A5F5))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1628))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: pick photo from gallery */ },
                containerColor = Color(0xFF1565C0),
                contentColor = Color(0xFFF0F6FF),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.gallery_add_photo))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF0A1628), Color(0xFF102040))))
                .padding(padding)
                .alpha(alpha)
        ) {
            if (photos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📷", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(id = R.string.gallery_no_photos), color = Color(0xFFF0F6FF), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.gallery_add_first_photo_hint), color = Color(0xFFB0BEC5), fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    val countText = if (photos.size == 1) {
                        stringResource(id = R.string.gallery_photo_count_singular)
                    } else {
                        stringResource(id = R.string.gallery_photo_count_plural, photos.size)
                    }
                    Text(
                        text = countText,
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            PhotoCell(
                                photo = photo,
                                onDelete = { photos = photos.filter { p -> p.id != photo.id } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(photo: TripPhoto, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(photo.color)
    ) {
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color(0xCC0A1628), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = R.string.gallery_delete_photo),
                tint = Color(0xFFF0F6FF),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}