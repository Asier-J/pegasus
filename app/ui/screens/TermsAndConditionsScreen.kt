package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R

// ─── Colors ───────────────────────────────────────────────────────────────────
private val NavyDeep    = Color(0xFF0A1628)
private val NavyMid     = Color(0xFF102040)
private val AzureBlue   = Color(0xFF1565C0)
private val SkyBlue     = Color(0xFF42A5F5)
private val IceWhite    = Color(0xFFF0F6FF)
private val SubtleGray  = Color(0xFFB0BEC5)
private val DividerLine = Color(0xFF1E3A5F)

// ─── Data ─────────────────────────────────────────────────────────────────────
private data class TermsSection(val title: String, val body: String)


// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    navController: NavController
) {
    val termsSections = listOf(
        TermsSection(
            stringResource(id = R.string.terms_acceptance_title),
            stringResource(id = R.string.terms_acceptance_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_use_title),
            stringResource(id = R.string.terms_use_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_privacy_title),
            stringResource(id = R.string.terms_privacy_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_location_title),
            stringResource(id = R.string.terms_location_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_third_party_title),
            stringResource(id = R.string.terms_third_party_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_ip_title),
            stringResource(id = R.string.terms_ip_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_disclaimer_title),
            stringResource(id = R.string.terms_disclaimer_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_liability_title),
            stringResource(id = R.string.terms_liability_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_changes_title),
            stringResource(id = R.string.terms_changes_body)
        ),
        TermsSection(
            stringResource(id = R.string.terms_governing_law_title),
            stringResource(id = R.string.terms_governing_law_body)
        )
    )

    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, NavyMid)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms & Conditions",
                        color = IceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SkyBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🐴",
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pegasus",
                    color = SkyBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Last updated: February 2026",
                    color = SubtleGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Please read these terms carefully before using the app. By using Pegasus, you agree to the following conditions.",
                    color = SubtleGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DividerLine, thickness = 1.dp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                termsSections.forEach { section ->
                    TermsSectionCard(section = section)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "© 2026 Pegasus. All rights reserved.",
                    color = SubtleGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TermsSectionCard(section: TermsSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF112240)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(AzureBlue, SkyBlue)
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = section.title,
                    color = SkyBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = section.body,
                color = Color(0xFFCDD6E8),
                fontSize = 13.sp,
                lineHeight = 21.sp
            )
        }
    }
}
