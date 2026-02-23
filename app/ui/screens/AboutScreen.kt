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
private val CardBg      = Color(0xFF112240)

// ─── Data ─────────────────────────────────────────────────────────────────────
private data class TeamMember(val name: String, val role: String)

private val teamMembers = listOf(
    TeamMember("Asier Juárez", "Lead Developer")
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
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
            .background(Brush.verticalGradient(colors = listOf(NavyDeep, NavyMid)))
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
                        text = stringResource(id = R.string.about_title),
                        color = IceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.about_back_button_description),
                            tint = SkyBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── App Hero ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "🐴", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.about_app_name),
                        color = SkyBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(listOf(AzureBlue, SkyBlue)),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.about_version),
                            color = IceWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // ── Summary Card ──────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(text = stringResource(id = R.string.about_summary_title))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.about_summary_body),
                            color = Color(0xFFCDD6E8),
                            fontSize = 13.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                // ── Tech Stack Card ───────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(text = stringResource(id = R.string.about_tech_stack_title))
                        Spacer(modifier = Modifier.height(12.dp))
                        TechItem(
                            label = stringResource(id = R.string.about_tech_stack_language),
                            value = stringResource(id = R.string.about_tech_stack_language_value)
                        )
                        TechItem(
                            label = stringResource(id = R.string.about_tech_stack_ui_framework),
                            value = stringResource(id = R.string.about_tech_stack_ui_framework_value)
                        )
                        TechItem(
                            label = stringResource(id = R.string.about_tech_stack_navigation),
                            value = stringResource(id = R.string.about_tech_stack_navigation_value)
                        )
                        TechItem(
                            label = stringResource(id = R.string.about_tech_stack_min_sdk),
                            value = stringResource(id = R.string.about_tech_stack_min_sdk_value)
                        )
                        TechItem(
                            label = stringResource(id = R.string.about_tech_stack_target_sdk),
                            value = stringResource(id = R.string.about_tech_stack_target_sdk_value)
                        )
                    }
                }

                // ── Team Card ─────────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(text = stringResource(id = R.string.about_team_title))
                        Spacer(modifier = Modifier.height(12.dp))
                        teamMembers.forEach { member ->
                            TeamMemberRow(
                                member = TeamMember(
                                    name = stringResource(id = R.string.about_team_member_name),
                                    role = stringResource(id = R.string.about_team_member_role)
                                )
                            )
                        }
                    }
                }

                // ── Footer ────────────────────────────────────────────────────
                Text(
                    text = stringResource(id = R.string.about_footer),
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

// ─── Helpers ──────────────────────────────────────────────────────────────────
@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(AzureBlue, SkyBlue)),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = SkyBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TechItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SubtleGray, fontSize = 13.sp)
        Text(text = value, color = IceWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TeamMemberRow(member: TeamMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AzureBlue, SkyBlue))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.first().toString(),
                color = IceWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = member.name, color = IceWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = member.role, color = SubtleGray, fontSize = 12.sp)
        }
    }
}