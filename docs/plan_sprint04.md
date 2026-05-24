# Sprint 04 – Planning Document

## 1. Sprint Goal

Integrate the app with the **Hotels Demo REST API** (`http://15.224.84.148:8090`) via
**Retrofit** to implement hotel search, room booking, reservation listing and
cancellation. Add a **local image gallery per trip** stored on device. The
architecture keeps MVVM + Repository + Hilt and gains a new remote layer:
`UI (Screens) → ViewModel → Repository (interface) → RepositoryImpl → (Retrofit + Room DAOs + local file storage)`

Each group has its own `group_id`. This project uses **G10** (Juárez Ontiveros, Asier).

---

## 2. Sprint Backlog

| ID   | Task | Assignee | Estimation (h) | Priority | Done |
|------|------|----------|----------------|----------|------|
| T1.1 | Add Retrofit + OkHttp + Gson dependencies and configure the HTTP client | Asier | 0.5 | Very High | Done |
| T1.2 | Define DTOs (`HotelDto`, `RoomDto`, `AvailabilityResponseDto`, `ReserveRequestDto`, `ReservationDto`, `ReservationResponseDto`) and the `HotelApiService` Retrofit interface | Asier | 1.5 | Very High | Done |
| T1.3 | Create `HotelRepository` interface + `HotelRepositoryImpl` and bind via Hilt | Asier | 1.5 | Very High | Done |
| T1.4 | Unit tests mocking the remote connection (`HotelApiService` + `HotelRepositoryImpl`) | Asier | 1.5 | High | Done |
| T2.1 | `HotelSearchScreen` — city dropdown (BCN / PAR / LON) + start/end DatePicker, consumes `/availability` | Asier | 2 | Very High | Done |
| T2.2 | List hotels and their rooms (3 per hotel) returned by the API | Asier | 1 | Very High | Done |
| T2.3 | `HotelDetailScreen` — per-room book button. Booking calls `/reserve` and saves a Reservation row locally (and a Trip if missing) | Asier | 2 | Very High | Done |
| T2.4 | Display hotel and room images using Coil (`/images/{file}` over the API base URL) | Asier | 1 | High | Done |
| T3.1 | UI to attach multiple images to a trip from the system gallery (Photo Picker) | Asier | 1.5 | High | Done |
| T3.2 | Persist images locally: copy file to `filesDir/trip_images/<tripId>/<uuid>` + insert `TripImage` row via Room | Asier | 1.5 | High | Done |
| T3.3 | Show the per-trip gallery inside `TripDetailScreen` (grid + tap to delete) | Asier | 1 | High | Done |
| T4.1 | `ReservationListScreen` — list all local reservations indicating the related trip | Asier | 1.5 | High | Done |
| T4.2 | Cancel a reservation: delete locally and call `DELETE /reservations/{res_id}` | Asier | 1 | High | Done |
| T4.3 | Each reservation card shows hotel + room images (matching the in-class example) | Asier | 0.5 | Medium | Done |
| T4.4 | `TripListScreen` / `TripDetailScreen` — show a "hotel booked" badge and the reservation summary when a trip has one | Asier | 1 | Medium | Done |
| T5.1 | Add bottom-nav entry for "Reservations" and wire all new routes in `NavGraph.kt` | Asier | 0.5 | Medium | Done |
| T5.2 | Update `design.md` (remote layer + new DB schema) and write `final_sprint04.md` | Asier | 0.5 | Low | Done |
| T5.3 | Logcat traces for all remote calls (DEBUG request / INFO success / ERROR failure) | Asier | 0.25 | Low | Done |
| T5.4 | Bump release to **v4.0.0** and record the demo video in `docs/evidence/v4.0.0/` | Asier | 0.5 | Low | Done |

---

## 3. Architecture

```
UI (Screens)
    └── ViewModel (HotelViewModel, ReservationViewModel, TripImageViewModel,
                   + existing TripViewModel, ActivityViewModel, AuthViewModel, UserViewModel)
            └── Repository interface
                    └── RepositoryImpl (injected via Hilt)
                            ├── Retrofit → HotelApiService → REST API   (T1, T2, T4)
                            ├── Room DAOs → SQLite                       (local persistence)
                            └── Internal storage (filesDir/trip_images)  (T3)
```

**Build config (`app/build.gradle.kts`)** — new `buildConfigField`s:

```kotlin
defaultConfig {
    buildConfigField("String", "HOTELS_API_URL", "\"http://15.224.84.148:8090/\"")
    buildConfigField("String", "GROUP_ID",       "\"G10\"")
}
buildFeatures { buildConfig = true }
```

**New dependencies (`libs.versions.toml` + `app/build.gradle.kts`):**

```
com.squareup.retrofit2:retrofit:2.11.0
com.squareup.retrofit2:converter-gson:2.11.0
com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14
io.coil-kt.coil3:coil-compose:3.1.0
io.coil-kt.coil3:coil-network-okhttp:3.1.0
```

**New files (planned):**

- `data/remote/api/HotelApiService.kt` — Retrofit interface (8 endpoints)
- `data/remote/dto/HotelDto.kt`, `RoomDto.kt`, `AvailabilityResponseDto.kt`,
  `ReserveRequestDto.kt`, `ReservationDto.kt`, `ReservationResponseDto.kt`,
  `ApiMessageDto.kt`
- `data/remote/mapper/DtoMappers.kt` — DTO ↔ Domain
- `data/repository/HotelRepositoryImpl.kt`
- `data/repository/ReservationRepositoryImpl.kt`
- `data/repository/TripImageRepositoryImpl.kt`
- `data/local/dao/ReservationDao.kt`
- `data/local/dao/TripImageDao.kt`
- `data/local/ImageFileStorage.kt` — write/delete local image files
- `domain/Hotel.kt`, `Room.kt`
- `domain/Reservation.kt` (Room `@Entity`)
- `domain/TripImage.kt` (Room `@Entity`)
- `domain/HotelRepository.kt`, `ReservationRepository.kt`, `TripImageRepository.kt`
- `ui/viewmodels/HotelViewModel.kt`
- `ui/viewmodels/ReservationViewModel.kt`
- `ui/viewmodels/TripImageViewModel.kt`
- `ui/screens/HotelSearchScreen.kt`
- `ui/screens/HotelDetailScreen.kt`
- `ui/screens/ReservationListScreen.kt`
- `di/NetworkModule.kt` — provides `OkHttpClient`, `Retrofit`, `HotelApiService`,
  and binds the new repositories

**Existing files to modify:**

- `app/build.gradle.kts` — new deps, `buildConfigField`s, `buildFeatures.buildConfig = true`
- `gradle/libs.versions.toml` — new version aliases
- `data/local/AppDatabase.kt` — register `Reservation` + `TripImage`, bump to `version = 3`
- `di/AppModule.kt` — provide the two new DAOs and bind the new repositories
- `AndroidManifest.xml` — already has `INTERNET`; cleartext for the demo IP
  (`usesCleartextTraffic="true"`) since the API is plain HTTP
- `NavGraph.kt` — add `hotel_search`, `hotel_detail/{hotelId}`, `reservations` routes
- `ui/screens/TripDetailScreen.kt` — embed gallery section + reservation summary
- `ui/screens/TripListScreen.kt` — badge "hotel booked" when a trip has a reservation
- `ui/screens/TripGalleryScreen.kt` — rewrite the mock-only version against the new
  `TripImageViewModel` (now backed by Room + local files)
- `docs/design.md` — new "Sprint 04 — Remote Persistence & Local Gallery" section

---

## 4. API Endpoints used (group_id = **G10**)

| Verb   | Path                                | Purpose                                                  |
|--------|-------------------------------------|----------------------------------------------------------|
| GET    | `/hotels/G10/hotels`                | List all hotels and their rooms                          |
| GET    | `/hotels/G10/availability`          | Available rooms for `city` + `start_date` + `end_date`   |
| POST   | `/hotels/G10/reserve`               | Reserve a room (returns reservation id + nights)         |
| GET    | `/hotels/G10/reservations`          | List reservations for the group (optionally by email)    |
| DELETE | `/reservations/{res_id}`            | Cancel a reservation by id                               |

Dates use the format **`YYYY-MM-DD`** (different from the local Trip date format
`dd/MM/yyyy`); the repository / mapper layer handles the conversion.

City prefixes accepted by the API: `BCN`, `PAR`, `LON`.
Availability only allows reservations in **May–June 2025** (server-enforced; the
search screen pre-validates dates and surfaces the API error otherwise).

---

## 5. Definition of Done (DoD)

- [x] Retrofit + OkHttp + Gson + Coil added; HTTP client logs request/response
- [x] `HotelApiService` covers the 5 endpoints listed in §4
- [x] `HotelRepositoryImpl` + `ReservationRepositoryImpl` provide a clean domain
      API for the ViewModels; both are `@Singleton`-bound via Hilt
- [x] Hotel search screen with city dropdown (London / Paris / Barcelona) and
      start/end DatePickers
- [x] Hotel detail screen lists rooms (typically 3) with all images, price, and
      a "Reserve" button
- [x] Booking writes a `Reservation` row locally (linked to a `Trip`) and the
      server returns 200 OK
- [x] Reservations screen lists every local reservation with hotel + room images
- [x] Cancel reservation removes it locally and on the server
- [x] `TripListScreen` / `TripDetailScreen` flag trips that include a reservation
- [x] Per-trip image gallery: add from device gallery (Photo Picker), saved to
      app internal storage, listed in a grid, swipe/tap to delete
- [x] All date and time fields use Material3 `DatePicker`
- [x] Unit tests for `HotelApiService` (mocked), `HotelRepositoryImpl`,
      `ReservationDao`, `TripImageDao`, `HotelViewModel`, `ReservationViewModel`
- [x] Logcat traces for every remote call (DEBUG attempt, INFO ok, ERROR fail)
- [x] `design.md` updated with the remote layer and schema v3
- [ ] Demo video uploaded to `docs/evidence/v4.0.0/`
- [ ] Release **v4.0.0** tagged

---

## 6. Identified Risks

- The Hotels API is plain **HTTP** — Android 9+ blocks cleartext traffic by
  default, so the app needs `usesCleartextTraffic="true"` (or a more granular
  network-security config) to talk to `15.224.84.148:8090`.
- Server-side date constraint: only **May–June 2025** windows are accepted.
  Searches outside that range return `400 Bad Request` with a Spanish-only
  detail message — the UI must surface this gracefully.
- Image URLs returned by the API are **relative paths** (`/images/BCN01.png`).
  They must be combined with the base URL before passing them to Coil.
- Coil 3.x changed package coordinates (`io.coil-kt.coil3:*`). Picking the
  wrong artifact silently breaks `AsyncImage` at runtime.
- Local storage: copying images to internal storage prevents permission issues
  with shared media but increases the app footprint; we accept that for now.
- Room v2 → v3 needs `fallbackToDestructiveMigration` (already enabled), so any
  installed dev build will wipe local data on first launch.
