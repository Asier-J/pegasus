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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R
import com.example.pegasus.safePopBackStack

private data class TermsSection(val title: String, val body: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    val termsSections = listOf(
        TermsSection(stringResource(R.string.terms_acceptance_title),    stringResource(R.string.terms_acceptance_body)),
        TermsSection(stringResource(R.string.terms_use_title),           stringResource(R.string.terms_use_body)),
        TermsSection(stringResource(R.string.terms_privacy_title),       stringResource(R.string.terms_privacy_body)),
        TermsSection(stringResource(R.string.terms_location_title),      stringResource(R.string.terms_location_body)),
        TermsSection(stringResource(R.string.terms_third_party_title),   stringResource(R.string.terms_third_party_body)),
        TermsSection(stringResource(R.string.terms_ip_title),            stringResource(R.string.terms_ip_body)),
        TermsSection(stringResource(R.string.terms_disclaimer_title),    stringResource(R.string.terms_disclaimer_body)),
        TermsSection(stringResource(R.string.terms_liability_title),     stringResource(R.string.terms_liability_body)),
        TermsSection(stringResource(R.string.terms_changes_title),       stringResource(R.string.terms_changes_body)),
        TermsSection(stringResource(R.string.terms_governing_law_title), stringResource(R.string.terms_governing_law_body))
    )

    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "fade"
    )

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
                        text       = stringResource(R.string.terms_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description),
                            tint               = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            Column(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🐴", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text          = stringResource(R.string.app_name),
                    color         = colors.primary,
                    fontSize      = 28.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text     = stringResource(R.string.terms_last_updated),
                    color    = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text       = stringResource(R.string.terms_intro),
                    color      = colors.onSurfaceVariant,
                    fontSize   = 13.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = colors.outline, thickness = 1.dp)
            }

            Column(
                modifier            = Modifier
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
                    text      = stringResource(R.string.terms_footer),
                    color     = colors.onSurfaceVariant,
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TermsSectionCard(section: TermsSection) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(
                            Brush.verticalGradient(listOf(colors.primaryContainer, colors.primary)),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = section.title,
                    color      = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text       = section.body,
                color      = colors.onSurface,
                fontSize   = 13.sp,
                lineHeight = 21.sp
            )
        }
    }
}