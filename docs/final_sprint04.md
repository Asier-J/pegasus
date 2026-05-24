# Sprint 04 – Final Report

## 1. Implemented Features

### T1.1 – Retrofit configuration

`app/build.gradle.kts` declares two new `buildConfigField` constants and the
`buildConfig = true` feature so they reach the source set:

```kotlin
buildConfigField("String", "HOTELS_API_URL", "\"http://15.224.84.148:8090/\"")
buildConfigField("String", "GROUP_ID",       "\"G10\"")
buildFeatures { buildConfig = true }
```

Manifest: kept `INTERNET` from Sprint 03 and added
`android:usesCleartextTraffic="true"` so the HTTP-only demo API can be reached.

Hilt provides `OkHttpClient` (with `HttpLoggingInterceptor`), `Retrofit` and
`HotelApiService` from `di/NetworkModule.kt`, plus two named strings
(`@Named("hotelsBase")` and `@Named("groupId")`) consumed by
`HotelRepositoryImpl`.

### T1.2 – DTOs + Retrofit interface (MVVM)

`data/remote/dto/` hosts seven DTO files matching the API JSON 1:1
(`HotelDto`, `RoomDto`, `AvailabilityResponseDto`, `ReserveRequestDto`,
`ReservationDto`, `ReservationResponseDto`, `ApiMessageDto`).

`data/remote/api/HotelApiService.kt` exposes five suspend endpoints:

| Method   | Path                                | Purpose                  |
|----------|-------------------------------------|--------------------------|
| GET      | `hotels/{group_id}/hotels`          | All hotels + rooms       |
| GET      | `hotels/{group_id}/availability`    | Availability search      |
| POST     | `hotels/{group_id}/reserve`         | Reserve a room           |
| GET      | `hotels/{group_id}/reservations`    | List reservations        |
| DELETE   | `reservations/{res_id}`             | Cancel a reservation     |

`data/remote/mapper/DtoMappers.kt` converts DTOs into domain models (`Hotel`,
`Room`) and turns the API's relative image paths
(e.g. `/images/BCN01.png`) into absolute URLs.

### T1.3 – Repository layer

| Repository | File | Role |
|---|---|---|
| `HotelRepository` (interface) | `domain/HotelRepository.kt` | Remote-only — wraps Retrofit |
| `HotelRepositoryImpl` | `data/repository/` | Calls the API, maps DTOs, logs every call |
| `ReservationRepository` (interface) | `domain/ReservationRepository.kt` | Local Room-only |
| `ReservationRepositoryImpl` | `data/repository/` | Wraps `ReservationDao` |
| `TripImageRepository` (interface) | `domain/TripImageRepository.kt` | Local Room + file I/O |
| `TripImageRepositoryImpl` | `data/repository/` | Combines `TripImageDao` + `ImageFileStorage` |

All three are `@Singleton`-bound in `di/AppModule.kt` (`RepositoryModule`).

### T1.4 – Unit tests (54 new tests, 177 total — all passing)

| Test class | Tests | Purpose |
|---|---|---|
| `HotelApiServiceTest.kt` | 6 | MockWebServer round-trips for the five endpoints + 400-error path |
| `HotelRepositoryImplTest.kt` | 7 | Mocked `HotelApiService` checks group_id wiring, mapper, captures the request body for `reserveRoom`, propagation of failures |
| `HotelViewModelTest.kt` | 12 | Search validation + happy/failure paths, booking guards (no user / no email / no dates), booking persistence path (Trip + Reservation), trip-title de-duplication, clear helpers |
| `ReservationDaoTest.kt` | 7 | CRUD, observe scoped by tripId, DESC ordering by createdAt, deleteForTrip, cascade on trip delete, REPLACE semantics |
| `TripImageDaoTest.kt` | 6 | CRUD, observe scoped, DESC ordering by addedAt, cascade on trip delete, deleteForTrip |
| `ReservationViewModelTest.kt` | 9 | Cancel paths (remote OK, remote OK + keep trip, remote failure cleans up locally + surfaces error), `tripTitlesById` map (logged-in vs anonymous), file-cleanup before trip delete, clearError, StateFlow proxy |
| `TripImageViewModelTest.kt` | 7 | Empty state until `setTripId`, flow swap on `setTripId`, `addImage`/`deleteImage` happy + failure paths, `clearError` |
| **TOTAL (Sprint 04 only)** | **54** | All passing |

Combined with the 123 Sprint 03 tests, the suite is now **177/177 passing**.

### T2.1 / T2.2 – Hotel search screen

`ui/screens/HotelSearchScreen.kt`:

- `ExposedDropdownMenuBox` for the city (Barcelona / Paris / London → API
  codes `BCN` / `PAR` / `LON`).
- Two `OutlinedTextField`s tied to Material3 `DatePicker` dialogs (read-only
  so the user **cannot** type a date manually).
- "Search" button calls `HotelViewModel.searchHotels()`, which delegates to
  `HotelRepository.checkAvailability`.
- Renders an empty hint (before the first search), a loading spinner during
  the call, an "empty result" message, or a list of `HotelCard`s otherwise.
- Each card shows the hotel image (Coil `AsyncImage`), name, address,
  "from €X / night" derived from the cheapest room, and the rooms count.

### T2.3 – Book a room → save reservation locally

`ui/screens/HotelDetailScreen.kt` lists every room for the selected hotel and
exposes a "Reserve" button per room. The flow is:

1. `HotelViewModel.bookRoom(hotel, room)` calls `HotelRepository.reserveRoom`.
2. On success, the ViewModel builds a `Trip` (title = `"<hotel name> (<id>)"`,
   description = `"<address> — <roomType>"`, dates converted from ISO to
   `dd/MM/yyyy`, budget = price × nights) and persists it via `TripRepository`.
   If the title is already taken the ViewModel auto-appends `" #2"` / `" #3"` …
3. It builds a `Reservation` with denormalised hotel/room snapshots (so the
   list keeps working offline) and persists it via `ReservationRepository`.
4. `_lastBooking` flips, the detail screen shows a confirmation dialog with a
   "My bookings" action that opens `ReservationListScreen`.

### T2.4 – Hotel and room images

- `HotelSearchScreen` shows the hotel cover image in each card.
- `HotelDetailScreen` shows the cover image plus, **per room**, a horizontal
  carousel (`LazyRow` + `AsyncImage`) that displays **all** images returned by
  the API. Tested against the BCN03 / PAR03 / LON03 hotels which the lab PDF
  ships with multiple images.
- Coil 3 + the network-okhttp engine handle remote loading; for local files
  (gallery T3), we pass a `java.io.File` directly.

### T3.1 / T3.2 – Add images to a trip + local storage

`TripGalleryScreen.kt` (rewritten on top of the Sprint 03 mock) drives the
gallery for the currently-open trip via `TripImageViewModel`:

- The FAB launches Android's **Photo Picker** (`ActivityResultContracts
  .PickVisualMedia`). This is the modern API that **doesn't** require
  `READ_MEDIA_IMAGES` at runtime, sidestepping the runtime-permission dance.
- `TripImageRepositoryImpl.addImage` copies the picked URI into
  `filesDir/trip_images/<tripId>/<uuid>.jpg` via `ImageFileStorage` and inserts
  a `TripImage` row.
- Tapping a thumbnail opens a "Delete photo?" dialog; confirming removes both
  the Room row and the on-disk file.

### T3.3 – Per-trip gallery shown in trip details

`TripDetailScreen.kt` was extended with a new `TripGallerySection` composable
that renders a horizontal `LazyRow` of up to 8 thumbnails plus a "View all"
button that opens `TripGalleryScreen`. If the trip has no photos, the section
shows a tap-to-add hint that opens the gallery directly.

### T4.1 / T4.3 – Reservation list with hotel & room images

`ReservationListScreen.kt`:

- `ReservationViewModel.reservations` exposes a hot `StateFlow<List<Reservation>>`
  fed by `ReservationRepository.observeReservations`.
- Each card shows the hotel + room data alongside a `LazyRow` carousel with
  both the hotel cover and the room image (matching the `RemotePersistence.zip`
  layout described in the lab PDF).
- A "Open in bookings" / card tap navigates back to the trip's detail screen.

### T4.2 – Cancel a reservation

The card has a trash icon that opens a confirmation dialog. On confirm,
`ReservationViewModel.cancel` runs the following sequence:

1. `HotelRepository.cancelReservation(id)` — server-side DELETE.
2. `ReservationRepository.deleteReservation(id)` — local row removal.
3. By default, also `TripRepository.deleteTrip(tripId)` because the local Trip
   was created together with the Reservation. This can be opted out via
   `removeTrip = false`.

If step 1 fails (offline, server error, already cancelled) the local cleanup
still runs and a snackbar surfaces `ERROR_REMOTE_CANCEL` — we never leave the
user staring at a phantom reservation.

### T4.4 – "Hotel" badge in My Trips

`TripListScreen` now observes `ReservationViewModel.reservations` and shows a
🏨 chip on every trip card whose `id` appears in the reservation list.
`TripDetailScreen` shows a full `ReservationSummary` card (hotel image, name,
room, dates, nights, total price) that links back to the bookings screen.

### T5.1 – NavGraph + bottom bar

- Bottom-nav tab `Gallery → trip_photo_list` (Sprint 03 mock) is replaced by
  `Hotels → hotel_search`. The mock files `TripPhotoListScreen.kt` and
  `TripData.kt` were deleted.
- New routes: `hotel_search`, `hotel_detail/{hotelId}`, `reservations`. The
  `trip_gallery/{tripId}` argument type changed from `IntType` to `StringType`
  to match the real `Trip.id`.
- `HotelSearchScreen`'s top bar carries a `BookOnline` IconButton that opens
  the reservations screen.

### T5.2 – Documentation

- `docs/plan_sprint04.md` updated with `Done` markers on every task.
- `docs/design.md` extended with §5 "Sprint 04 — Remote Persistence & Local
  Gallery" covering the new layers, schema v3, and DI graph.
- `docs/final_sprint04.md` (this file).

### T5.3 – Logcat

| Tag | Sample levels |
|---|---|
| `HotelRepository` | `D` attempt, `I` success, `E` failure |
| `ReservationRepository` | `D` reads, `I` writes |
| `TripImageRepository` | `D` reads, `I` writes |
| `HotelViewModel` | `I` ok / `W` validation / `E` failure |
| `ReservationViewModel` | `I` cancel ok / `W` partial cancel / `E` failure |
| `TripImageViewModel` | `I` add/delete ok / `E` failure |

OkHttp `HttpLoggingInterceptor` is configured at `BODY` level in debug builds
and `BASIC` in release, so every HTTP request/response surfaces in Logcat.

### T5.4 – Release / evidence

- `versionCode = 4`, `versionName = "4.0.0"` in `app/build.gradle.kts`.
- `about_version` strings still pull from `R.string.about_version` — they are
  intentionally **not** bumped from `v3.0.0` until the v4.0.0 git tag is
  published; this matches the workflow used in Sprints 2 and 3.
- `docs/evidence/v4.0.0/` is created and ready for the demo video.

---

## 2. Test Results

Run with:
```
./gradlew :app:testDebugUnitTest --tests "com.example.pegasus.*"
```

```
> Task :app:testDebugUnitTest

BUILD SUCCESSFUL

Tests:
  Sprint 03 suite      123/123 PASS  (unchanged)
  HotelApiServiceTest    6/6   PASS  (Sprint 04 — T1.4)
  HotelRepositoryImplTest 7/7  PASS  (Sprint 04 — T1.4)
  HotelViewModelTest    12/12  PASS  (Sprint 04 — T1.4)
  ReservationDaoTest     7/7   PASS  (Sprint 04 — T1.4)
  TripImageDaoTest       6/6   PASS  (Sprint 04 — T1.4)
  ReservationViewModelTest 9/9 PASS  (Sprint 04 — T1.4)
  TripImageViewModelTest   7/7 PASS  (Sprint 04 — T1.4)
  ─────────────────────────────────
  TOTAL                177/177 PASS
```

---

## 3. Fixes Applied During Sprint

| Issue | Fix |
|---|---|
| `buildConfigField` was unreachable because `buildFeatures.buildConfig` defaults to false on AGP 8.x | Enabled `buildConfig = true` in the `buildFeatures` block. |
| Coil 3 changed Maven coordinates from `io.coil-kt:coil-compose:2.x` to `io.coil-kt.coil3:coil-compose:3.x`. Using the 2.x artifact silently broke `AsyncImage` at runtime | Pinned `coil = "3.1.0"` and imported `coil3.compose.AsyncImage` everywhere. |
| Android 9+ blocks plain HTTP, but the demo API is HTTP-only | Set `android:usesCleartextTraffic="true"` on `<application>`. A future production build should narrow this to a `network_security_config.xml` whitelist for the demo IP. |
| `HotelRepositoryImplTest.checkAvailability` failed with NPE because mockito-kotlin's `any()` returns the type's default value (null) for nullable params, which collides with `eq` matchers in the same call | Switched to `anyOrNull()` for `hotelId: String?`. |
| Sprint 03's `TripGalleryScreen` consumed a hardcoded `mockPhotoTrips` and `tripId: Int`, but the real Trip primary key is a `String` UUID | Rewrote the screen to take `tripId: String`, drive it with `TripImageViewModel`, use the Photo Picker, and render Coil images from `java.io.File`. Deleted the now-obsolete `TripPhotoListScreen.kt` and `TripData.kt`. |
| `AuthRepository.AuthUser` doesn't carry a `displayName` field, so the booking VM had no clean name to send to `/reserve` | Use `email.substringBefore('@')` as a sensible default `guest_name`. |
| `BottomNavItem.Gallery` (Sprint 03 mock) and the new `Hotels` tab would push the bottom bar past six items | Replaced `Gallery` with `Hotels` (6 items total — unchanged count) and exposed `Reservations` from the Hotels top-bar IconButton instead of a separate tab. |
| `trip_photo_list` route was still registered in `bottomNavRoutes` / `NavGraph` after the mock screens were deleted | Replaced it with `hotel_search` everywhere. |
| **`HotelSearchScreen` and `HotelDetailScreen` were each getting their own `HotelViewModel` instance** because both called `hiltViewModel()` (which scopes to the local NavBackStackEntry). The detail screen therefore had an empty `_hotels.value`, so `hotelById()` always returned `null` and the room list never rendered | Promoted the VM to the `hotel_search` back-stack entry: `NavGraph` now calls `hiltViewModel<HotelViewModel>(parent = navController.getBackStackEntry("hotel_search"))` in both destinations so they share state. |
| **Trip image files leaked on disk after a reservation cancel** — `tripRepository.deleteTrip` cascades the SQL FK, removing the `trip_images` rows, but the JPEG files under `filesDir/trip_images/<tripId>/` stayed on disk forever | `ReservationViewModel.cancel` now calls `tripImageRepository.deleteAllForTrip` (which removes both the rows AND the files) **before** `tripRepository.deleteTrip`. Verified by two new tests in `ReservationViewModelTest`. |
| Original `ReservationListScreen` didn't surface the parent trip title — T4.1 explicitly asks to "list all local reservations **indicating the trip related**" | `ReservationViewModel` now exposes a `tripTitlesById: StateFlow<Map<String,String>>` driven by the logged-in user's trip list; each `ReservationCard` shows `"Viaje: <title>"`. Covered by two new `tripTitlesById` tests. |
| `TripImageViewModel` had no unit-test coverage | Added `TripImageViewModelTest` with 7 tests (Robolectric + real `Uri.parse` because `Uri` is `final` and not mock-able without `mockito-inline`). |

---

## 4. Domain Model Changes

| Element | Change | Reason |
|---|---|---|
| `Hotel`, `Room` | new domain models | T1.2 / T1.3 |
| `Reservation` | new domain model **and** Room `@Entity` (FK → `trips.id`, CASCADE) | T2.3 / T4.* |
| `TripImage` | new domain model **and** Room `@Entity` (FK → `trips.id`, CASCADE) | T3.* |
| `HotelRepository`, `ReservationRepository`, `TripImageRepository` | new interfaces | All Sprint 04 features |
| `AppDatabase` | bumped to `version = 3`, registered the two new entities + DAOs | Schema growth |

`docs/domain_model.png` will be regenerated for v4.0.0.

---

## 5. Deviations from `plan_sprint04.md`

The plan was followed end-to-end, with one minor deviation:

### 5.1 `Reservations` is **not** a bottom-nav tab

| Plan said | What was implemented |
|---|---|
| Add a bottom-nav entry "Reservations" | The reservations screen is reachable from a `BookOnline` IconButton in the `HotelSearchScreen` top bar (and from the booking-confirmation dialog) instead of getting its own tab. |

**Why this is better:**

1. Material 3 `NavigationBar` is designed for 3–5 items. The Sprint 03 layout
   already shipped six, and adding both *Hotels* and *Reservations* would push
   it to seven — visibly crammed on small phones.
2. The flow makes sense: you only check your bookings after you've made one,
   so reaching them from the booking flow (the top bar shortcut or the
   confirmation dialog) costs one tap, not zero.
3. The plan's `"Reservations" tab` was a soft requirement: the PDF (T4.1) only
   asks for "a screen to list all local reservations", not for a specific
   navigation slot.

**Trade-off accepted:** users who want to inspect their bookings without first
opening the Hotels tab need an extra tap. We considered that a fair price for
a less crowded primary navigation.

---

## 6. Known Limitations

- The Hotels API is **plain HTTP**. We accept it via `usesCleartextTraffic`
  because the lab API doesn't expose TLS. A production app would either talk
  to a TLS-fronted version or narrow the cleartext whitelist via a
  `network_security_config.xml`.
- The server-side date constraint (only May–June 2025 is bookable) is enforced
  by the API. We catch the `400 Bad Request` and show the error message
  verbatim — localising it would require either a regex on the Spanish
  response or a server-side i18n endpoint, neither of which is in scope.
- Photos copied into internal storage do not survive an app uninstall — this
  matches the lab's "saved locally" requirement and is the simpler privacy
  posture, but the user cannot easily back them up. A future sprint could
  expose them via `MediaStore` if shareability becomes a requirement.
- Room v2 → v3 still relies on `fallbackToDestructiveMigration`. Any installed
  v3.0.0 dev build will lose its local data on first launch of v4.0.0.
- The demo video lives at `docs/evidence/v4.0.0/video.txt` (external link,
  same convention as Sprints 2 and 3 — see `evidence/v4.0.0/README.md`).
