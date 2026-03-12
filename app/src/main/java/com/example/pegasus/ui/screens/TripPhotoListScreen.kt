package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

private val NavyDeep   = Color(0xFF0A1628)
private val NavyMid    = Color(0xFF102040)
private val AzureBlue  = Color(0xFF1565C0)
private val SkyBlue    = Color(0xFF42A5F5)
private val IceWhite   = Color(0xFFF0F6FF)
private val SubtleGray = Color(0xFFB0BEC5)
private val CardBg     = Color(0xFF112240)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPhotoListScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    var backEnabled by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "fade"
    )
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.gallery_list_title), color = IceWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.gallery_back_button), tint = SkyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDeep)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: create new trip */ },
                containerColor = AzureBlue,
                contentColor = IceWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.gallery_list_new_trip))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(NavyDeep, NavyMid)))
                .padding(padding)
                .alpha(alpha)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockPhotoTrips) { trip ->
                    TripCard(trip = trip, onClick = { navController.navigate("trip_gallery/${trip.id}") })
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun TripCard(trip: Trip, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Brush.linearGradient(colors = listOf(trip.coverColor, trip.coverColor.copy(alpha = 0.6f)))),
                contentAlignment = Alignment.BottomStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xCC0A1628))))
                        .padding(12.dp)
                ) {
                    Text(text = stringResource(id = trip.nameResId), color = IceWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = trip.destinationResId), color = SubtleGray, fontSize = 13.sp)
                val countText = if (trip.photos.size == 1) {
                    stringResource(id = R.string.gallery_photo_count_singular)
                } else {
                    stringResource(id = R.string.gallery_photo_count_plural, trip.photos.size)
                }
                Text(
                    text = countText,
                    color = SkyBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}