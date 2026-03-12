package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

// ─── Mock Data ────────────────────────────────────────────────────────────────
private data class MockRecommendation(
    val id: String,
    val type: String,
    val emoji: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val tripResId: Int,
    var isSaved: Boolean = false,
    var isDismissed: Boolean = false
)

private val mockRecommendations = listOf(
    MockRecommendation("1", "restaurant", "🍜", R.string.rec_1_title,
        R.string.rec_1_desc,
        R.string.rec_1_trip),
    MockRecommendation("2", "activity",   "🌸", R.string.rec_2_title,
        R.string.rec_2_desc,
        R.string.rec_1_trip),
    MockRecommendation("3", "hotel",      "🏨", R.string.rec_3_title,
        R.string.rec_3_desc,
        R.string.rec_3_trip),
    MockRecommendation("4", "activity",   "🗼", R.string.rec_4_title,
        R.string.rec_4_desc,
        R.string.rec_3_trip),
    MockRecommendation("5", "restaurant", "🍕", R.string.rec_5_title,
        R.string.rec_5_desc,
        R.string.rec_5_trip),
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRecommendationScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "fade"
    )

    var recommendations by remember { mutableStateOf(mockRecommendations) }
    var prompt by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(id = R.string.ai_rec_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // ── Prompt input ──────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = prompt,
                    onValueChange = { prompt = it },
                    placeholder   = { Text(stringResource(id = R.string.ai_rec_prompt_placeholder), fontSize = 13.sp) },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedTextColor     = colors.onSurface,
                        unfocusedTextColor   = colors.onSurface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { /* @TODO call AI API with prompt */ },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary)))
                        .size(52.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = stringResource(id = R.string.ai_rec_generate_description), tint = colors.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Recommendations list ──────────────────────────────────────────
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recommendations.filter { !it.isDismissed }) { rec ->
                    RecommendationCard(
                        rec       = rec,
                        onSave    = { recommendations = recommendations.map { if (it.id == rec.id) it.copy(isSaved = !it.isSaved) else it } },
                        onDismiss = { recommendations = recommendations.map { if (it.id == rec.id) it.copy(isDismissed = true) else it } }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── Recommendation Card ──────────────────────────────────────────────────────
@Composable
private fun RecommendationCard(
    rec:       MockRecommendation,
    onSave:    () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = rec.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text       = stringResource(id = rec.titleResId),
                            color      = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.primaryContainer.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text       = rec.type.replaceFirstChar { it.uppercase() },
                                color      = colors.primary,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.ai_rec_dismiss_description), tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = stringResource(id = rec.descriptionResId),
                color      = colors.onSurfaceVariant,
                fontSize   = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = "✈️ ${stringResource(id = rec.tripResId)}",
                    color    = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onSave,
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, colors.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector        = if (rec.isSaved) Icons.Filled.Favorite  else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(id = R.string.ai_rec_save_description),
                            modifier           = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (rec.isSaved) stringResource(id = R.string.ai_rec_status_saved) else stringResource(id = R.string.ai_rec_status_save), fontSize = 12.sp)
                    }
                    Button(
                        onClick        = { /* @TODO convert to ItineraryItem */ },
                        shape          = RoundedCornerShape(8.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier       = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.ai_rec_button_add), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(id = R.string.ai_rec_button_add), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}