package com.example.pegasus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R
import com.example.pegasus.safePopBackStack

private data class TeamMember(val name: String, val role: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val colors      = MaterialTheme.colorScheme
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
            // ── Top Bar ──────────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.about_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.about_back_button_description),
                            tint               = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            Column(
                modifier            = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── App Hero ─────────────────────────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "🐴", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text          = stringResource(R.string.about_app_name),
                        color         = colors.primary,
                        fontSize      = 32.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary)),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = stringResource(R.string.about_version),
                            color      = colors.onPrimary,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // ── Summary Card ──────────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutSectionTitle(text = stringResource(R.string.about_summary_title))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text       = stringResource(R.string.about_summary_body),
                            color      = colors.onSurface,
                            fontSize   = 13.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                // ── Tech Stack Card ───────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutSectionTitle(text = stringResource(R.string.about_tech_stack_title))
                        Spacer(modifier = Modifier.height(12.dp))
                        TechItem(stringResource(R.string.about_tech_stack_language),    stringResource(R.string.about_tech_stack_language_value))
                        TechItem(stringResource(R.string.about_tech_stack_ui_framework),stringResource(R.string.about_tech_stack_ui_framework_value))
                        TechItem(stringResource(R.string.about_tech_stack_navigation),  stringResource(R.string.about_tech_stack_navigation_value))
                        TechItem(stringResource(R.string.about_tech_stack_min_sdk),     stringResource(R.string.about_tech_stack_min_sdk_value))
                        TechItem(stringResource(R.string.about_tech_stack_target_sdk),  stringResource(R.string.about_tech_stack_target_sdk_value))
                    }
                }

                // ── Team Card ─────────────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutSectionTitle(text = stringResource(R.string.about_team_title))
                        Spacer(modifier = Modifier.height(12.dp))
                        TeamMemberRow(
                            member = TeamMember(
                                name = stringResource(R.string.about_team_member_name),
                                role = stringResource(R.string.about_team_member_role)
                            )
                        )
                    }
                }

                // ── Footer ────────────────────────────────────────────────────
                Text(
                    text      = stringResource(R.string.about_footer),
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
private fun AboutSectionTitle(text: String) {
    val colors = MaterialTheme.colorScheme
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
        Text(text = text, color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun TechItem(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.onSurfaceVariant, fontSize = 13.sp)
        Text(text = value, color = colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TeamMemberRow(member: TeamMember) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.primary))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = member.name.first().toString(),
                color      = colors.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = member.name, color = colors.onSurface,        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = member.role, color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}