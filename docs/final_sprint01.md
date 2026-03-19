# Sprint 01 – Final Report

## 1. Implemented Features

### T1.1 – Domain Model

Domain classes defined in `domain/`:

| Class | Key Fields |
|-------|-----------|
| `Trip.kt` | `id`, `title`, `destination`, `startDate`, `endDate`, `budget`, `status` |
| `Activity.kt` | `id`, `tripId`, `title`, `description`, `date`, `time` |
| `User.kt` | `id`, `name`, `email`, `profilePicture`, `trips` |
| `UserPreferences.kt` | `language`, `theme`, `notifications` |
| `UserAuthentication.kt` | Authentication stub |
| `AIRecommendation.kt` | AI recommendation model |
| `Image.kt` | Photo/gallery model |
| `ItineraryItem.kt` | Itinerary item model |
| `TripMap.kt` | Map location model |

All classes include their core fields and method stubs (`@todo` placeholders for business logic).

### T1.2 – Logo

App logo created and set as the launcher icon (used in the splash screen and app bar).

### T1.3 & T1.4 – Android & Kotlin Version

| Setting | Value |
|---------|-------|
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` / `compileSdk` | 36 (Android 16) |
| Kotlin | via Jetpack Compose BOM |
| `versionName` | 1.0.0 |

### T1.5 – Project Initialization

Android Studio project created with Jetpack Compose template. Package: `com.example.pegasus`.

### T2.1 – GitHub Repository

Repository created and initialized. Source code pushed. Branch strategy: `main`.

### T2.2 – License

GPL-3.0 license selected and included in the repository.

### T2.3 – Documentation Files

- `docs/design.md` — architecture overview and data model diagram
- `docs/color-palette.md` — color palette reference
- `README.md` — project description, setup instructions, tech stack
- `CONTRIBUTING.md` — contribution guidelines

### T2.4 & T2.5 – Planning & Repository Organization

`docs/plan_sprint01.md` written. Repository folder structure organized:

```
app/src/main/java/com/example/pegasus/
    ├── domain/          ← data model classes
    ├── data/            ← (prepared for Sprint 02)
    ├── ui/
    │   ├── screens/     ← Compose screens
    │   ├── viewmodels/  ← (prepared for Sprint 02)
    │   └── theme/       ← color palette & typography
    └── NavGraph.kt
```

### T3.1 – Core Screen Layouts

All main screens implemented as Jetpack Compose composables:

| Screen | Route | Description |
|--------|-------|-------------|
| `HomeScreen` | `home` | Greeting, quick stats, navigation shortcuts |
| `TripListScreen` | `trips` | List of trips with stat chips |
| `MapScreen` | `map` | Placeholder map with location list |
| `AIRecommendationScreen` | `ai` | AI recommendation cards |
| `ProfileScreen` | `profile` | User profile with settings navigation rows |
| `AboutScreen` | `about` | App info, tech stack, team, license |
| `TermsAndConditionsScreen` | `terms` | Full terms & conditions text |
| `PreferencesScreen` | `preferences` | Language and theme selectors |
| `SplashScreen` | — | Animated launch screen with logo |

### T3.2 – Navigation

Full `NavGraph.kt` implemented using Navigation Compose. Bottom navigation bar shown on main routes (`home`, `trips`, `map`, `ai`, `profile`). Top-level screens (About, Terms, Preferences) use back-navigation only.

### T3.3 & T3.5 – Design Documentation

- Data model class diagram documented in `docs/design.md`
- Color palette (primary, secondary, background, surface, error tokens) documented in `docs/color-palette.md`

### T3.4 – Domain Classes & Method Stubs

All domain classes implemented with their fields. Method signatures defined with `TODO()` bodies, ready for Sprint 02 implementation.

### T4.1 – Splash Screen

`SplashScreen` implemented using AndroidX `core-splashscreen`. Displays the Pegasus logo with a fade-in animation before navigating to `HomeScreen`.

### T4.2 – About & Terms Screens

- `AboutScreen`: app name, version badge, summary, tech stack table, team member card, license section, footer.
- `TermsAndConditionsScreen`: 10 sections of legal text, scrollable, with back navigation.

### T4.3 – Preferences Screen (Multi-language)

`PreferencesScreen` implemented with language selector (EN, ES, CA) and theme toggle (Dark / Light). Language switching uses `applyLocaleToContext()` + activity restart. All strings translated in `res/values/`, `res/values-es/`, `res/values-ca/`.

---

## 2. Test Results

No automated unit tests in Sprint 01 — testing infrastructure and CRUD tests are scheduled for Sprint 02.

Manual smoke testing performed on emulator (Pixel 6, API 34):

| Test | Result |
|------|--------|
| App launches without crash | ✅ PASS |
| Splash screen shows and transitions to Home | ✅ PASS |
| Bottom navigation switches between all 5 tabs | ✅ PASS |
| About screen opens and closes | ✅ PASS |
| Terms screen opens and closes | ✅ PASS |
| Preferences screen opens; language switch restarts activity | ✅ PASS |
| Preferences screen: theme toggle switches dark/light | ✅ PASS |

---

## 3. Fixes Applied During Sprint

| Issue | Fix |
|-------|-----|
| Actual time spent on T2.3 exceeded estimate (0.5 h → 1 h) | Design documentation was more detailed than planned; time re-allocated from T1.1 and T1.4 |
| T3.1 exceeded estimate (3 h → 4 h) | More screens required than initially scoped (Gallery, TripPhotoList added) |
| T3.3 exceeded estimate (1 h → 2 h) | Domain model diagram iterated multiple times to reflect all entities correctly |

---

## 4. Known Limitations

- All screens display **static / fake data** — no real data layer yet (by design for Sprint 01).
- Trip and activity CRUD is not functional — implemented in Sprint 02.
- Language preference is not persisted between app restarts (added in Sprint 02 via SharedPreferences).
- Theme preference is not persisted between app restarts (added in Sprint 02 via SharedPreferences).
- Navigation from `TripListScreen` to trip detail is a placeholder.
