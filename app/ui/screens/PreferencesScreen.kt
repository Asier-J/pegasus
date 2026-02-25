package com.example.pegasus.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pegasus.R
import com.example.pegasus.safePopBackStack
import java.util.Locale

// ─── Data ─────────────────────────────────────────────────────────────────────
enum class AppLanguage(val label: String, val flag: String, val code: String) {
    ENGLISH("English", "🇬🇧", "en"),
    SPANISH("Español", "🇪🇸", "es"),
    CATALAN("Català",  "🏴",   "ca")
}

enum class AppTheme(val labelResId: Int, val icon: String) {
    DARK(R.string.preferences_theme_dark,   "🌙"),
    LIGHT(R.string.preferences_theme_light, "☀️")
}

// ─── SharedPreferences helpers ────────────────────────────────────────────────
private const val PREFS_NAME   = "app_prefs"
private const val KEY_LANGUAGE = "language_code"
private const val KEY_THEME    = "theme_dark"

fun saveLanguage(context: Context, code: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_LANGUAGE, code).apply()
}

fun getSavedLanguage(context: Context): String =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, "en") ?: "en"

fun saveTheme(context: Context, isDark: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_THEME, isDark).apply()
}

fun getSavedTheme(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_THEME, true)

// ─── Locale application ───────────────────────────────────────────────────────
fun applyLocaleToContext(context: Context, code: String): Context {
    val locale = Locale(code)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(locale))
    } else {
        @Suppress("DEPRECATION")
        config.locale = locale
    }
    return context.createConfigurationContext(config)
}

// ─── Activity restart helper ──────────────────────────────────────────────────
private fun Context.restartActivity() {
    val activity = generateSequence(this) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<Activity>()
        .firstOrNull() ?: return

    val intent = Intent(activity, activity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    activity.finish()
    activity.startActivity(intent)
    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(navController: NavController) {
    val context     = LocalContext.current
    val colors      = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "fade"
    )

    var selectedLanguage by remember {
        val saved = getSavedLanguage(context)
        val match = AppLanguage.values().firstOrNull { it.code == saved } ?: AppLanguage.ENGLISH
        mutableStateOf(match)
    }

    var selectedTheme by remember {
        mutableStateOf(if (getSavedTheme(context)) AppTheme.DARK else AppTheme.LIGHT)
    }

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
                        text       = stringResource(id = R.string.preferences_title),
                        color      = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.preferences_back_button_description),
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
                // ── Language Card ─────────────────────────────────────────────
                PreferenceCard(title = stringResource(id = R.string.preferences_language_title)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.values().forEach { language ->
                            OptionRow(
                                label    = "${language.flag}  ${language.label}",
                                selected = selectedLanguage == language,
                                onClick  = {
                                    if (selectedLanguage != language) {
                                        selectedLanguage = language
                                        saveLanguage(context, language.code)
                                        context.restartActivity()
                                    }
                                }
                            )
                        }
                    }
                }

                // ── Theme Card ────────────────────────────────────────────────
                PreferenceCard(title = stringResource(id = R.string.preferences_theme_title)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTheme.values().forEach { theme ->
                            ThemeChip(
                                label    = "${theme.icon}  ${stringResource(id = theme.labelResId)}",
                                selected = selectedTheme == theme,
                                onClick  = {
                                    if (selectedTheme != theme) {
                                        selectedTheme = theme
                                        saveTheme(context, theme == AppTheme.DARK)
                                        context.restartActivity()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── Info banner ───────────────────────────────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "ℹ️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text       = stringResource(id = R.string.preferences_info_banner),
                        color      = colors.onSurfaceVariant,
                        fontSize   = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────
@Composable
private fun PreferenceCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
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
                Text(text = title, color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) colors.primaryContainer.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text       = label,
            color      = if (selected) colors.onSurface else colors.onSurfaceVariant,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        Brush.radialGradient(listOf(colors.primaryContainer, colors.primary)),
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = colors.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected)
                    Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary))
                else
                    Brush.horizontalGradient(listOf(colors.surface, colors.surface))
            )
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outline,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = if (selected) colors.onPrimary else colors.onSurfaceVariant,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
