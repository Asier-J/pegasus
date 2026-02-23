# 🎨 Pegasus — Color Palette

> All colors are defined via `MaterialTheme.colorScheme` in `Theme.kt`.

---

## Dark Theme (Default)

| Token | Hex | Usage |
|---|---|---|
| `background` | `#0A1628` | Screen backgrounds |
| `surfaceVariant` | `#102040` | Gradient end, secondary backgrounds |
| `surface` | `#112240` | Cards, info banners |
| `primary` | `#42A5F5` | Accent, icons, titles, borders |
| `primaryContainer` | `#1565C0` | Gradient start, button backgrounds |
| `onPrimary` | `#0A1628` | Text/icons on primary color |
| `onBackground` | `#F0F6FF` | Main text on backgrounds |
| `onSurface` | `#F0F6FF` | Main text on cards |
| `onSurfaceVariant` | `#B0BEC5` | Subtitles, secondary text, footers |
| `outline` | `#1E3A5F` | Borders, dividers |

---

## Light Theme

| Token | Hex | Usage |
|---|---|---|
| `background` | `#E8F1FB` | Screen backgrounds |
| `surfaceVariant` | `#BDD5EF` | Gradient end, secondary backgrounds |
| `surface` | `#D0E4F7` | Cards, info banners |
| `primary` | `#1565C0` | Accent, icons, titles, borders |
| `primaryContainer` | `#42A5F5` | Gradient start, button backgrounds |
| `onPrimary` | `#FFFFFF` | Text/icons on primary color |
| `onBackground` | `#0A1628` | Main text on backgrounds |
| `onSurface` | `#0D1F38` | Main text on cards |
| `onSurfaceVariant` | `#2E5070` | Subtitles, secondary text, footers |
| `outline` | `#90B8D8` | Borders, dividers |

---

## Usage in Code

Colors are accessed via `MaterialTheme.colorScheme` inside any `@Composable`:

```kotlin
val colors = MaterialTheme.colorScheme

// Background gradient
Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))

// Card
CardDefaults.cardColors(containerColor = colors.surface)

// Primary accent
Text(color = colors.primary)

// Subtle text
Text(color = colors.onSurfaceVariant)

// Gradient button
Brush.horizontalGradient(listOf(colors.primaryContainer, colors.primary))
```

---

## Theme Switching

The active theme is saved in `SharedPreferences` and applied at startup via `MainActivity`:

```kotlin
// Save
saveTheme(context, isDark = true)   // "dark"
saveTheme(context, isDark = false)  // "light"

// Read & apply
val isDark = getSavedTheme(this)
PegasusTheme(darkTheme = isDark) { ... }
```