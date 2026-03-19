# Sprint 02 – Final Report

## 1. Implemented Features

### T1.1 – Trip CRUD (InMemory, MVVM)

Trips are stored in `FakeTripDataSource` (singleton `object` backed by a `MutableList<Trip>`).
Operations: `addTrip`, `editTrip`, `deleteTrip`, `getTrips`, `getTripById`.

Each trip contains: `id`, `title`, `startDate` (dd/MM/yyyy), `endDate` (dd/MM/yyyy), `description`.

Flow: `TripListScreen` / `AddEditTripScreen` → `TripViewModel` → `TripRepository` → `TripRepositoryImpl` → `FakeTripDataSource`

### T1.2 – Activity CRUD (InMemory, MVVM)

Activities stored alongside trips in `FakeTripDataSource`.
Operations: `addActivity`, `updateActivity`, `deleteActivity`, `getActivitiesByTripId`, `getActivityById`.

Each activity contains: `id`, `tripId`, `title`, `description`, `date` (LocalDate), `time` (LocalTime).

Flow: `TripDetailScreen` / `AddEditActivityScreen` → `ActivityViewModel` → `ActivityRepository` → `ActivityRepositoryImpl` → `FakeTripDataSource`

### T1.3 – Data Validation

- All date fields use `DatePickerDialog` (Material3) — `readOnly = true` prevents free-text input.
- `TripViewModel` validates: title not blank, description not blank, start date ≤ end date.
- `ActivityViewModel` validates: title not blank, date not null, time not null, activity date within trip date range.
- Errors shown via `SnackbarHost` and inline `supportingText` on form fields.

### T1.4 – User Settings (SharedPreferences)

Stored in `app_prefs` SharedPreferences:

| Key | Type | Default |
|-----|------|---------|
| `username` | String | "" |
| `date_of_birth` | String | "" |
| `theme_dark` | Boolean | true |
| `language_code` | String | "en" |

`MainActivity.attachBaseContext()` applies saved language before the activity inflates.
`MainActivity.onCreate()` applies saved dark/light theme before rendering.

### T1.5 – Multi-language

All new strings translated into EN, ES, CA in:
- `res/values/strings.xml`
- `res/values-es/strings.xml`
- `res/values-ca/strings.xml`

### T2 – Itinerary Flow

Navigation structure implemented in `NavGraph.kt`:
```
Menu → trips (TripListScreen)
    → trip_detail/{tripId} (TripDetailScreen)
        → add_activity/{tripId}
        → edit_activity/{tripId}/{activityId}
    → add_trip
    → edit_trip/{tripId}
```

Both `TripViewModel` and `ActivityViewModel` are created at NavGraph scope (activity-scoped),
so state is preserved across navigation.

---

## 2. Test Results

### TripCrudTest (11 tests — all pass)

| Test | Result |
|------|--------|
| `addTrip_addsToList` | ✅ PASS |
| `addTrip_multipleTrips_allPresent` | ✅ PASS |
| `getTrips_emptyWhenNoTrips` | ✅ PASS |
| `getTrips_returnsImmutableSnapshot` | ✅ PASS |
| `getTripById_returnsCorrectTrip` | ✅ PASS |
| `getTripById_returnsNullForUnknownId` | ✅ PASS |
| `editTrip_updatesExistingTrip` | ✅ PASS |
| `editTrip_doesNotChangeTripCount` | ✅ PASS |
| `deleteTrip_removesFromList` | ✅ PASS |
| `deleteTrip_unknownId_doesNotCrash` | ✅ PASS |
| `deleteTrip_onlyDeletesTargetTrip` | ✅ PASS |

### ActivityCrudTest (11 tests — all pass)

| Test | Result |
|------|--------|
| `addActivity_addsToList` | ✅ PASS |
| `addActivity_multipleActivities_allPresent` | ✅ PASS |
| `getActivitiesByTripId_emptyForNewTrip` | ✅ PASS |
| `getActivitiesByTripId_onlyReturnsActivitiesForTargetTrip` | ✅ PASS |
| `getActivityById_returnsCorrectActivity` | ✅ PASS |
| `getActivityById_returnsNullForUnknownId` | ✅ PASS |
| `updateActivity_updatesCorrectly` | ✅ PASS |
| `updateActivity_doesNotChangeTotalCount` | ✅ PASS |
| `deleteActivity_removesFromList` | ✅ PASS |
| `deleteActivity_unknownId_doesNotCrash` | ✅ PASS |
| `deleteActivity_onlyDeletesTargetActivity` | ✅ PASS |

---

## 3. Fixes Applied During Sprint

| Issue | Fix |
|-------|-----|
| `Icons` import ambiguity in `PreferencesScreen.kt` after linter reorder | Reordered: `Icons` → `ArrowBack` → `DateRange` all in the same import group |
| `StatChip` unresolved reference in `TripListScreen.kt` | Added `StatChip` private composable definition |
| Old `Trip` domain used `Long` for dates | Replaced with `String` (dd/MM/yyyy) as required by sprint |

---

## 4. Domain Model Changes

The original domain model has been updated to reflect the Sprint 02 implementation. The full model is preserved — no classes or fields were removed. Only the following changes were made:

| Element | Change | Reason |
|---------|--------|--------|
| `Trip.startDate` | `Long` → `String` | The sprint requires dates in `dd/MM/yyyy` format for display in the UI without conversion. Using `Long` (epoch ms) would require parsing on every render and adds unnecessary complexity at this stage. |
| `Trip.endDate` | `Long` → `String` | Same reason as `startDate`. |
| `Trip.description` | Added `+String description` | The sprint requires a description field for trips. This replaces the original `notes` field at the UI level while keeping `notes` in the model for future use. |
| `Activity` | New class added | The sprint requires a full Activity CRUD linked to trips. `Activity` maps to `ItineraryItem` conceptually but uses `LocalDate` + `LocalTime` instead of a single `Long datetime`, providing better type safety and easier validation of date ranges. |

The updated diagram is available at `docs/domain_model.png`.

---

## 5. Known Limitations

- Data is **not persistent** between app restarts (by design — InMemory only for Sprint 02).
- `FakeTripDataSource.clearAll()` is only for unit testing; not exposed in production.
- Deleting a trip also cascades and removes all its activities.
