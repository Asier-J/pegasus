# 🐴 Pegasus — Data Model Diagram Documentation

## 1. Overview

The domain layer is the core of the Pegasus Travel Planner application. It defines the data models and business logic independently of any framework, database, or UI. All classes are implemented as Kotlin `data class` following an **MVVM** architecture pattern.

The package contains the following classes:

| Class | Description |
|---|---|
| `User` | Root entity representing an app user |
| `UserPreferences` | User settings: language, theme, currency |
| `UserAuthentication` | Login/logout state and provider |
| `Trip` | A travel trip with budget and itinerary |
| `ItineraryItem` | A single event or booking within a trip |
| `Image` | A photo stored within a trip |
| `AIRecommendation` | An AI-generated suggestion for a trip |
| `TripMap` | Map state and location logic |
| `MapMarker` | A pin displayed on the TripMap |

---

## 2. Architecture & Design Decisions

### 2.1 Why data classes?

All domain entities use Kotlin `data class`. This provides automatic `equals()`, `hashCode()`, `toString()` and `copy()` implementations, which are essential for immutable state management in Compose and ViewModel.

### 2.2 Immutability

All fields are declared with `val` (immutable). State changes return a new copy of the object via `copy()`, which prevents side effects and makes the codebase easier to test and debug.

```kotlin
// Example: updating preferences returns a new object
fun updatePreferences(newTheme: String): UserPreferences {
    return this.copy(theme = newTheme, lastUpdated = System.currentTimeMillis())
}
```

### 2.3 Integration with SharedPreferences

`UserPreferences.preferredLanguage` and `UserPreferences.theme` are kept in sync with the app's SharedPreferences layer (`PreferencesScreen.kt`). The values match those saved by `saveLanguage()` and `saveTheme()`:

- `preferredLanguage`: `"en"`, `"es"`, or `"ca"`
- `theme`: `"dark"` or `"light"`

### 2.4 Class name conflicts

Three classes were renamed to avoid collisions with Android/Google/Kotlin built-ins:

| Original name | Renamed to | Conflict with |
|---|---|---|
| `Preferences` | `UserPreferences` | `java.util.prefs.Preferences` |
| `Authentication` | `UserAuthentication` | `com.google.api.Authentication` |
| `Map` | `TripMap` | `kotlin.collections.Map` |

---
## Figure 1 — Domain Class Diagram

![Domain Class Diagram](domain_model.png)

*Figure 1 — Pegasus Domain Layer UML Class Diagram*

---

## 3. Class Relationships

```
User ──(1 to 1)──► UserPreferences
User ──(1 to 1)──► UserAuthentication
User ──(1 to *)──► Trip
Trip ──(1 to *)──► ItineraryItem
Trip ──(1 to *)──► Image
Trip ──(1 to *)──► AIRecommendation
TripMap ──(* to *)──► Trip
TripMap ──(1 to *)──► MapMarker
AIRecommendation ──(converts to)──► ItineraryItem
```

| From | Relationship | To | Cardinality & Notes |
|---|---|---|---|
| `User` | has | `UserPreferences` | 1 to 1 — each user has exactly one set of preferences |
| `User` | manages | `UserAuthentication` | 1 to 1 — each user has exactly one auth object |
| `User` | owns | `Trip` | 1 to * — a user can own zero or more trips |
| `Trip` | contains | `ItineraryItem` | 1 to * — a trip holds an ordered list of items |
| `Trip` | stores | `Image` | 1 to * — a trip can have multiple photos |
| `Trip` | gets | `AIRecommendation` | 1 to * — a trip can receive multiple AI suggestions |
| `TripMap` | shows locations | `Trip` | * to * — map can display locations from multiple trips |
| `TripMap` | displays | `MapMarker` | 1 to * — a map holds multiple markers |
| `AIRecommendation` | converts to | `ItineraryItem` | A saved recommendation can become an itinerary item |

---

## 4. Class Reference

### 4.1 `User`

Root entity of the domain. Owns trips and references preferences and authentication as 1-to-1 relationships.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier |
| `email` | `String` | User's email address |
| `displayName` | `String` | Display name shown in the UI |
| `photoUrl` | `String?` | Optional profile picture URL |
| `createdAt` | `Long` | Account creation timestamp (ms) |
| `userPreferences` | `UserPreferences` | 1-to-1: user settings |
| `userAuthentication` | `UserAuthentication` | 1-to-1: auth state |
| `trips` | `List<Trip>` | 1-to-*: all trips owned by this user |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `getFullProfile()` | `String` | Returns `displayName (email)` formatted string |
| `getTripCount()` | `Int` | Returns the number of trips owned |
| `getTravelStats()` | `Map<String, Int>` | @TODO Aggregate travel statistics |
| `exportData()` | `String` | @TODO Export user data to JSON or PDF |
| `deleteAccount()` | `Unit` | @TODO Delete account and cascade to trips |

---

### 4.2 `UserPreferences`

Stores all user-configurable settings. The `preferredLanguage` and `theme` fields are kept in sync with the SharedPreferences layer managed by `PreferencesScreen.kt`.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `userId` | `String` | References the owning User |
| `notificationsEnabled` | `Boolean` | Whether push notifications are on (default: `true`) |
| `preferredLanguage` | `String` | Locale code: `"en"`, `"es"`, or `"ca"` |
| `theme` | `String` | `"dark"` or `"light"` — matches `getSavedTheme()` |
| `currency` | `String` | Preferred currency code (default: `"EUR"`) |
| `distanceUnit` | `String` | `"km"` or `"mi"` (default: `"km"`) |
| `lastUpdated` | `Long` | Timestamp of last preference change (ms) |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `updatePreferences(...)` | `UserPreferences` | Returns new copy with updated fields |
| `isDarkTheme()` | `Boolean` | Returns `true` if `theme == "dark"` |
| `syncToCloud()` | `Unit` | @TODO Sync preferences to backend API |
| `resetToDefaults()` | `UserPreferences` | @TODO Reset all fields to default values |

---

### 4.3 `UserAuthentication`

Manages login state and authentication provider for a User. Currently supports email/password; Google OAuth is a future feature.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `userId` | `String` | References the owning User |
| `isLoggedIn` | `Boolean` | Whether the user has an active session |
| `lastLoginAt` | `Long?` | Timestamp of last successful login (nullable) |
| `provider` | `String` | `"email"`, `"google"`, or `"apple"` |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `login(email, password)` | `UserAuthentication` | @TODO Authenticate with backend/Firebase |
| `logout()` | `UserAuthentication` | @TODO Clear session token, return logged-out copy |
| `resetPassword(email)` | `Unit` | @TODO Send password reset email |
| `loginWithGoogle()` | `UserAuthentication` | @TODO Google Sign-In OAuth flow |
| `updatePassword(old, new)` | `Unit` | @TODO Validate old and set new password |

---

### 4.4 `Trip`

The central entity of the app. Represents a travel trip with budget, dates, and status. Aggregates itinerary items, images and AI recommendations.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique trip identifier |
| `userId` | `String` | References the owning User (many-to-1) |
| `title` | `String` | Human-readable trip name |
| `destination` | `String` | Main destination of the trip |
| `startDate` | `Long` | Trip start timestamp (ms) |
| `endDate` | `Long` | Trip end timestamp (ms) |
| `budget` | `Double` | Total allocated budget |
| `status` | `String` | `"planned"`, `"ongoing"`, or `"completed"` |
| `notes` | `String` | Free text notes |
| `createdAt` | `Long` | Creation timestamp (ms) |
| `itineraryItems` | `List<ItineraryItem>` | 1-to-*: all items in the itinerary |
| `images` | `List<Image>` | 1-to-*: all photos stored in this trip |
| `recommendations` | `List<AIRecommendation>` | 1-to-*: AI suggestions for this trip |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `getRemainingBudget()` | `Double` | `budget` minus sum of all itinerary item prices |
| `getDurationDays()` | `Long` | Number of days between `startDate` and `endDate` |
| `getItemsByType(type)` | `List<ItineraryItem>` | Filter itinerary items by type string |
| `optimizeBudgetDistribution()` | `Unit` | @TODO Smart daily budget algorithm |
| `shareTrip()` | `String` | @TODO Generate shareable link or export |
| `getCarbonFootprint()` | `Double` | @TODO Calculate CO₂ based on transport types |

---

### 4.5 `ItineraryItem`

A single event, booking or activity within a trip's itinerary. Can be of type `flight`, `hotel`, `activity`, `restaurant`, or `transport`.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique item identifier |
| `tripId` | `String` | References the parent Trip (many-to-1) |
| `title` | `String` | Name of the activity or booking |
| `type` | `String` | `"flight"`, `"hotel"`, `"activity"`, `"restaurant"`, `"transport"` |
| `location` | `String` | Location or address |
| `datetime` | `Long` | Scheduled date and time (ms) |
| `price` | `Double` | Cost — contributes to trip budget |
| `notes` | `String` | Optional free text notes |
| `isConfirmed` | `Boolean` | Whether the booking is confirmed |
| `createdAt` | `Long` | Creation timestamp (ms) |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `getFormattedDate()` | `String` | @TODO Format using `UserPreferences.preferredLanguage` |
| `isTransport()` | `Boolean` | Returns `true` if type is `flight` or `transport` |
| `refreshPrice()` | `Double` | @TODO Call external API for live price update |
| `addToCalendar()` | `Unit` | @TODO Android Calendar integration |

---

### 4.6 `Image`

A photo associated with a trip. Includes metadata such as caption, location and upload timestamp. Designed for future cloud storage integration.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique image identifier |
| `tripId` | `String` | References the parent Trip (many-to-1) |
| `url` | `String` | Remote or local URL of the image |
| `caption` | `String` | Optional caption text |
| `location` | `String` | Where the photo was taken |
| `uploadedAt` | `Long` | Upload timestamp (ms) |
| `sizeBytes` | `Long` | File size in bytes |
| `isFavorite` | `Boolean` | Whether the user has starred this photo |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `getThumbnailUrl()` | `String` | @TODO Return resized thumbnail URL |
| `upload(localPath)` | `String` | @TODO Upload to Firebase Storage, return URL |
| `delete()` | `Unit` | @TODO Delete from cloud storage and database |
| `analyzeWithAI()` | `Map<String, String>` | @TODO Vision AI to detect landmarks and tags |

---

### 4.7 `AIRecommendation`

An AI-generated suggestion for a trip, produced from a user prompt. Saved recommendations can be converted directly into `ItineraryItem`s via `toItineraryItem()`.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique recommendation identifier |
| `tripId` | `String` | References the parent Trip (many-to-1) |
| `type` | `String` | `"restaurant"`, `"hotel"`, `"activity"`, `"transport"` |
| `prompt` | `String` | The user's input prompt |
| `result` | `String` | The AI-generated response text |
| `generatedAt` | `Long` | Generation timestamp (ms) |
| `isSaved` | `Boolean` | Whether the user saved this recommendation |
| `isDismissed` | `Boolean` | Whether the user dismissed this recommendation |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `generate(prompt)` | `AIRecommendation` | @TODO Call AI API with trip context and prompt |
| `save()` | `AIRecommendation` | @TODO Persist to database, set `isSaved = true` |
| `dismiss()` | `AIRecommendation` | @TODO Mark as dismissed, hide from UI |
| `toItineraryItem()` | `ItineraryItem` | @TODO Parse result and map to ItineraryItem fields |
| `refine(context)` | `AIRecommendation` | @TODO Follow-up prompt with previous result as context |

---

### 4.8 `TripMap`

Handles map state and location logic. Can display markers for multiple trips. Renamed from `Map` to avoid collision with `kotlin.collections.Map`.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `latitude` | `Double` | Current map center latitude |
| `longitude` | `Double` | Current map center longitude |
| `zoomLevel` | `Int` | Current zoom level (default: 12) |
| `markers` | `List<MapMarker>` | 1-to-*: all markers currently displayed |

**Methods:**

| Method | Returns | Description |
|---|---|---|
| `showLocation(lat, lng)` | `TripMap` | @TODO Update camera to given coordinates |
| `getNearbyPlaces()` | `List<String>` | @TODO Call Places API for nearby POIs |
| `centerOnTrip(trip)` | `TripMap` | @TODO Fit camera to bounding box of trip locations |
| `addMarker(location, label)` | `TripMap` | @TODO Geocode location and add MapMarker |
| `getRoute(origin, dest)` | `List<MapMarker>` | @TODO Call Directions API for waypoints |
| `filterMarkersByType(type)` | `TripMap` | @TODO Show only markers of given type |

---

### 4.9 `MapMarker`

A pin displayed on the `TripMap`. Created automatically by `TripMap.addMarker()`. The `type` field can be used to apply different pin icons in the UI layer.

**Fields:**

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Auto-generated UUID |
| `location` | `String` | Human-readable location string |
| `label` | `String` | Label shown on the pin |
| `latitude` | `Double` | Pin latitude coordinate |
| `longitude` | `Double` | Pin longitude coordinate |
| `type` | `String` | `"default"`, `"hotel"`, `"activity"`, etc. |

---

## 5. Pending Implementation (@TODO Summary)

### User
- `getTravelStats()` — aggregate km, countries visited, total spent across all trips
- `exportData()` — export full user data as JSON or PDF
- `deleteAccount()` — cascade delete trips, images and preferences

### UserPreferences
- `syncToCloud()` — sync preferences to backend API
- `resetToDefaults()` — should also call `saveLanguage()` and `saveTheme()` in `PreferencesScreen.kt`

### UserAuthentication
- `login()` — integrate with Firebase Auth or custom backend
- `logout()` — clear session token and local state
- `resetPassword()` — trigger Firebase password reset email
- `loginWithGoogle()` — implement Google Sign-In OAuth
- `updatePassword()` — validate old password before updating

### Trip
- `optimizeBudgetDistribution()` — smart algorithm to suggest daily spending limits
- `shareTrip()` — generate shareable deep link or exportable PDF
- `getCarbonFootprint()` — estimate CO₂ based on transport types in itinerary

### ItineraryItem
- `getFormattedDate()` — format `datetime` using `UserPreferences.preferredLanguage`
- `refreshPrice()` — call external API for live pricing
- `addToCalendar()` — Android Calendar integration

### Image
- `getThumbnailUrl()` — append resize params for cloud storage CDN
- `upload()` — upload to Firebase Storage and return public URL
- `delete()` — remove from cloud storage and database
- `analyzeWithAI()` — call Vision API to detect landmarks and generate tags

### AIRecommendation
- `generate()` — call AI API with trip context and user prompt
- `save()` — persist recommendation to local or remote database
- `dismiss()` — mark dismissed so it is filtered from UI
- `toItineraryItem()` — parse AI result and map fields to ItineraryItem
- `refine()` — send follow-up prompt with previous result as context

### TripMap
- `showLocation()` — update map camera via Maps SDK
- `getNearbyPlaces()` — integrate Google Places API
- `centerOnTrip()` — calculate bounding box and fit camera
- `addMarker()` — geocode string location and pin on map
- `getRoute()` — integrate Google Directions API
- `filterMarkersByType()` — toggle marker visibility by category
---

## 4. Sprint 03 — Persistence & Authentication Layer

Sprint 03 replaced the in-memory `FakeTripDataSource` with a real **Room** SQLite database, integrated **Firebase Authentication**, and adopted **Hilt** as the application-wide DI container.

### 4.1 Architecture

```
UI (Compose Screens)
    └── ViewModel (@HiltViewModel)
            └── Repository interface (TripRepository, ActivityRepository, UserRepository, AuthRepository)
                    └── Implementation (@Inject + @Binds via Hilt)
                            ├── Room DAOs → SQLite (local persistence)
                            └── FirebaseAuth (authentication)
```

### 4.2 Database schema (`pegasus.db`, version 2)

| Table | Columns | Notes |
|---|---|---|
| `users`        | `uid` PK (TEXT), `email` (TEXT), `username` (TEXT, unique), `displayName` (TEXT), `birthdate` (TEXT, dd/MM/yyyy), `address` (TEXT), `country` (TEXT), `phone` (TEXT), `acceptEmails` (INTEGER), `photoUrl` (TEXT, nullable), `createdAt` (INTEGER, epoch ms) | Mirror of the Firebase account + the extended profile (T4.1). Username uniqueness enforced by index + repository check. |
| `trips`        | `id` PK (TEXT), `userId` (TEXT, FK → `users.uid` CASCADE), `title` (TEXT), `startDate` (TEXT, dd/MM/yyyy), `endDate` (TEXT, dd/MM/yyyy), `description` (TEXT), `budget` (INTEGER), `createdAt` (INTEGER, epoch ms) | Multi-user storage (T4.2): every read query is scoped to `userId`. Satisfies T1.2 (text + integer + datetime). |
| `activities`   | `id` PK (TEXT), `tripId` (TEXT, FK → `trips.id` CASCADE), `title` (TEXT), `description` (TEXT), `date` (TEXT, ISO-8601 LocalDate), `time` (TEXT, ISO-8601 LocalTime), `durationMinutes` (INTEGER), `createdAt` (INTEGER, epoch ms) | Cascade-deleted with the parent trip. Satisfies T1.2. |
| `access_logs`  | `id` PK auto (INTEGER), `userId` (TEXT, no FK — see note), `event` (TEXT: `"LOGIN"` / `"LOGOUT"`), `timestamp` (INTEGER, epoch ms) | One row per login/logout (T4.4). FK to `users.uid` was intentionally removed in schema v2 so audit logs are always writable, even before the local user mirror is populated for accounts created outside the app. |

`LocalDate` and `LocalTime` are persisted as ISO-8601 strings via `@TypeConverters(Converters)`.

**v1 → v2 migration strategy:** dropped the FK constraint on `access_logs.userId`. `fallbackToDestructiveMigration()` is enabled in dev (in `di/AppModule.kt`) — for production, write proper `Migration` objects.

### 4.3 New / updated files (Sprint 03)

**New (Sprint 03):**
- `data/local/AppDatabase.kt` + `Converters.kt`
- `data/local/dao/{TripDao, ActivityDao, UserDao, AccessLogDao}.kt`
- `data/repository/{UserRepositoryImpl, AuthRepositoryImpl}.kt`
- `domain/{UserRepository, AuthRepository, AccessLog}.kt`
- `ui/viewmodels/{AuthViewModel, UserViewModel}.kt`
- `ui/screens/{LoginScreen, RegisterScreen, RecoverPasswordScreen}.kt`
- `di/AppModule.kt` (Hilt: `DatabaseModule`, `FirebaseModule`, `RepositoryModule`)
- `PegasusApplication.kt` (`@HiltAndroidApp`)

**Modified (Sprint 03):**
- `domain/{Trip, Activity, User}.kt` — promoted to Room `@Entity` with foreign keys
- `domain/{TripRepository, ActivityRepository}.kt` — interfaces now expose `suspend` + `Flow`
- `data/repository/{TripRepositoryImpl, ActivityRepositoryImpl}.kt` — Room-backed
- `ui/viewmodels/{TripViewModel, ActivityViewModel}.kt` — Hilt-injected, `@HiltViewModel`
- `ui/screens/{TripListScreen, TripDetailScreen, AddEditTripScreen, AddEditActivityScreen, ProfileScreen}.kt` — adapted to new VM API
- `MainActivity.kt` — `@AndroidEntryPoint`
- `NavGraph.kt` — adds `login`, `register`, `recover` routes + auth guard
- `AndroidManifest.xml` — `INTERNET` permission and `android:name=".PegasusApplication"`
- `app/build.gradle.kts`, `gradle/libs.versions.toml`, root `build.gradle.kts` — Hilt, Room, Firebase, KSP, Google Services

### 4.4 Authentication flow

```
[Splash] → (no session) → Login ─┬── Register (creates user, sends verification email,
                                  │                  saves profile in Room, logs LOGIN)
                                  └── Recover (sends Firebase reset email)

[Login OK]   → AccessLog(LOGIN)  → home
[Logout]     → AccessLog(LOGOUT) → login (auth guard)
```

### 4.5 Multi-user data ownership (T4.2)

Every trip row carries the owner's Firebase `uid`. `TripViewModel.trips` is a `flatMapLatest` over the Firebase auth-state Flow, so logging in/out automatically swaps the visible trip list to the active user's data set without manual refresh.

### 4.6 Validation (T5.2)

- Trip dates: `startDate ≤ endDate` (existing rule).
- Trip title: per-user uniqueness check via `TripDao.isTitleTakenByOther`.
- Username: per-app uniqueness check via `UserDao` index + `UserRepository.isUsernameTakenByOther`.
- Activity date: must fall inside the parent trip's `[startDate, endDate]` range.

---

## 5. Sprint 04 — Remote Persistence & Local Gallery

### 5.1 Architecture

```
UI (Screens)
    └── ViewModel  (HotelViewModel, ReservationViewModel, TripImageViewModel
                    + Sprint 03 VMs)
            └── Repository interface
                    └── RepositoryImpl  (Hilt @Singleton)
                            ├── Retrofit → HotelApiService  →  REST API   (T1 / T2 / T4)
                            ├── Room DAOs   → SQLite                       (local persistence)
                            └── Internal storage (filesDir/trip_images/<id>)  (T3 — image files)
```

`HotelApiService` is built by `di/NetworkModule.kt` from
`BuildConfig.HOTELS_API_URL` and the chosen `BuildConfig.GROUP_ID` (`"G10"`),
both wired in `app/build.gradle.kts`. The same module provides the
`OkHttpClient` (with `HttpLoggingInterceptor` at `BODY` in debug) and the
`Retrofit` instance.

### 5.2 Remote API consumed

Base URL: `http://15.224.84.148:8090/` · Group id: **G10**.

| Verb   | Path                                | Used by                              |
|--------|-------------------------------------|--------------------------------------|
| GET    | `/hotels/G10/hotels`                | `HotelRepository.listAllHotels`      |
| GET    | `/hotels/G10/availability`          | `HotelRepository.checkAvailability`  |
| POST   | `/hotels/G10/reserve`               | `HotelRepository.reserveRoom`        |
| GET    | `/hotels/G10/reservations`          | (available, currently unused)        |
| DELETE | `/reservations/{res_id}`            | `HotelRepository.cancelReservation`  |

The API speaks dates as `YYYY-MM-DD` and only accepts bookings in
**May–June 2025**. The local Trip layer still uses `dd/MM/yyyy` for backward
compatibility; the conversion happens inside `HotelViewModel.bookRoom`.

### 5.3 Schema v3

Two new tables join the v2 schema:

| Table | Fields | Notes |
|---|---|---|
| `reservations` | `id` (TEXT, PK), `tripId` (TEXT, FK → `trips.id`, CASCADE), `hotelId`, `hotelName`, `hotelAddress`, `hotelImageUrl`, `roomId`, `roomType`, `roomImageUrl`, `pricePerNight` (REAL), `startDate`, `endDate` (both `YYYY-MM-DD`), `nights` (INTEGER), `guestName`, `guestEmail`, `createdAt` (INTEGER) | Denormalised hotel/room snapshots so the bookings list survives offline (T2.3, T4). |
| `trip_images` | `id` (TEXT, PK), `tripId` (TEXT, FK → `trips.id`, CASCADE), `localPath` (TEXT — absolute path), `addedAt` (INTEGER) | The image bytes live on disk under `filesDir/trip_images/<tripId>/<uuid>.jpg` (T3). |

Cascade rules: deleting a Trip wipes its reservations *and* gallery rows.
`fallbackToDestructiveMigration()` stays enabled; a proper `Migration` will be
added before the production release.

### 5.4 DTO ↔ Domain mapping

`data/remote/dto/` holds JSON-mirroring DTOs
(`HotelDto`, `RoomDto`, `AvailabilityResponseDto`, `ReserveRequestDto`,
`ReservationDto`, `ReservationResponseDto`, `ApiMessageDto`).
`data/remote/mapper/DtoMappers.kt` turns them into `Hotel` / `Room` domain
models and converts the API's relative image URLs (e.g.
`/images/BCN01.png`) into absolute URLs using the base URL.

### 5.5 New / updated files (Sprint 04)

**New (Sprint 04):**
- `data/remote/api/HotelApiService.kt`
- `data/remote/dto/{HotelDto, RoomDto, AvailabilityResponseDto, ReserveRequestDto,
  ReservationDto, ReservationResponseDto, ApiMessageDto}.kt`
- `data/remote/mapper/DtoMappers.kt`
- `data/repository/{HotelRepositoryImpl, ReservationRepositoryImpl, TripImageRepositoryImpl}.kt`
- `data/local/dao/{ReservationDao, TripImageDao}.kt`
- `data/local/ImageFileStorage.kt`
- `domain/{Hotel, Room, Reservation, TripImage}.kt`
- `domain/{HotelRepository, ReservationRepository, TripImageRepository}.kt`
- `ui/viewmodels/{HotelViewModel, ReservationViewModel, TripImageViewModel}.kt`
- `ui/screens/{HotelSearchScreen, HotelDetailScreen, ReservationListScreen}.kt`
- `di/NetworkModule.kt`

**Modified (Sprint 04):**
- `data/local/AppDatabase.kt` — v3, adds `Reservation` + `TripImage`
- `di/AppModule.kt` — new DAO providers + bindings for the three new repositories
- `AndroidManifest.xml` — `usesCleartextTraffic="true"` + photo-read permissions
- `app/build.gradle.kts` — version 4.0.0, `buildConfigField`s, Retrofit/OkHttp/Coil deps
- `gradle/libs.versions.toml` — `retrofit`, `okhttp`, `coil`, `mockwebserver`
- `NavGraph.kt` — `hotel_search`, `hotel_detail/{id}`, `reservations`,
  `trip_gallery/{tripId}` (now `String`)
- `ui/screens/HomeScreen.kt` — `BottomNavItem.Hotels` replaces `Gallery`
- `ui/screens/TripDetailScreen.kt` — embeds gallery + reservation summary
- `ui/screens/TripListScreen.kt` — `🏨 Hotel` badge when a trip has a reservation
- `ui/screens/TripGalleryScreen.kt` — rewritten on top of `TripImageViewModel`
- `res/values/strings.xml` (+ `values-es`, `values-ca`) — new strings for the
  hotels, reservations and gallery flows

**Removed (Sprint 04):**
- `ui/screens/TripPhotoListScreen.kt`, `ui/screens/TripData.kt` (Sprint 03 mocks)

### 5.6 Logging

| Tag | Sample levels |
|---|---|
| `HotelRepository` | `D` attempt, `I` success (size / id), `E` failure |
| `ReservationRepository` | `D` reads, `I` writes |
| `TripImageRepository` | `D` reads, `I` writes |
| `HotelViewModel`, `ReservationViewModel`, `TripImageViewModel` | `I` ok / `W` validation / `E` failure |

OkHttp `HttpLoggingInterceptor` is configured at `BODY` in debug (full request
and response bodies) and `BASIC` in release.
