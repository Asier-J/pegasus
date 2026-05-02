# Sprint 03 – Final Report

## 1. Implemented Features

### T1.1 – Room Database Class

`AppDatabase` defined in `data/local/AppDatabase.kt` with `version = 2`, four entities,
the `Converters` type-converter and DAOs exposed through abstract methods:

```
@Database(entities = [User, Trip, Activity, AccessLog], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun activityDao(): ActivityDao
    abstract fun accessLogDao(): AccessLogDao
}
```

Provided as a `@Singleton` through Hilt (`di/AppModule.kt → DatabaseModule.provideAppDatabase`)
with `fallbackToDestructiveMigration()` enabled for development.

### T1.2 – Entities (datetime + text + integer)

| Entity | text | integer | datetime |
|---|---|---|---|
| `Trip` | `title`, `startDate`, `endDate`, `description` | `budget`, `createdAt` | dates as `dd/MM/yyyy` strings, `createdAt` epoch-ms |
| `Activity` | `title`, `description` | `durationMinutes`, `createdAt` | `date` (LocalDate), `time` (LocalTime) |
| `User` | `email`, `username`, `address`, `country`, `phone`, `birthdate` | `acceptEmails`, `createdAt` | — |
| `AccessLog` | `event`, `userId` (plain text, no FK in v2) | `id` (auto), `timestamp` | `timestamp` epoch-ms |

`LocalDate` / `LocalTime` are persisted as ISO-8601 strings via `Converters`.

### T1.3 – DAOs

- `TripDao` — CRUD scoped by `userId`, plus `isTitleTakenByOther(userId, title, excludeId)`.
- `ActivityDao` — CRUD scoped by `tripId`, ordered by `(date, time)`.
- `UserDao` — CRUD plus `findByUsername`, `isUsernameTakenByOther`.
- `AccessLogDao` — append-only `insert`, `observeForUser`, `countForUser`.

All read queries expose both a `suspend` snapshot variant and a `Flow<List<…>>` for reactive UI.

### T1.4 – CRUD via DAO

Implemented in `data/repository/{TripRepositoryImpl, ActivityRepositoryImpl, UserRepositoryImpl}.kt`,
all `@Inject`-constructed and `@Singleton`-bound through Hilt.

### T1.5 – ViewModels using Room

`TripViewModel` and `ActivityViewModel` are now `@HiltViewModel`-annotated, take their
dependencies via constructor injection and call the Room-backed repositories using
suspend operations on `viewModelScope`.

### T1.6 – Reactive UI

`TripViewModel.trips` is a `flatMapLatest` over the Firebase auth-state Flow:

```kotlin
val trips: StateFlow<List<Trip>> =
    authRepository.observeCurrentUser()
        .flatMapLatest { authUser ->
            if (authUser == null) flowOf(emptyList())
            else repository.observeTrips(authUser.uid)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

This way the trip list switches **automatically** when the user logs in / out, and any
DB write reaches the UI without manual refresh.

### T2.1 – Firebase Connection

- Added `com.google.gms.google-services` plugin and `firebase-bom 33.7.0`.
- `FirebaseModule.provideFirebaseAuth()` provides `FirebaseAuth` to Hilt.
- `INTERNET` and `ACCESS_NETWORK_STATE` permissions added to the manifest.
- `google-services.json` is **not** committed (gitignored) — the developer must drop it into `app/`.

### T2.2 / T2.3 – Login screen + Firebase logic

`LoginScreen.kt` with email/password fields, "forgot password" link and "sign up"
link. Submits to `AuthViewModel.login()` which calls
`AuthRepository.login(email, password)` → `FirebaseAuth.signInWithEmailAndPassword().await()`.
On success the screen navigates to `home` and clears `login` from the back stack.

The startup flow now checks the auth state (`SplashScreen` reads
`AuthViewModel.currentUser`) and routes the app to either `home` (logged in) or `login`
(logged out). A second guard inside `NavGraph` redirects any protected route back to
`login` if the session expires mid-use.

### T2.4 – Logout

`ProfileScreen` → "Cerrar sesión" row → `AuthViewModel.logout()`.
Logout (a) writes a `LOGOUT` row in `access_logs`, (b) clears the Firebase session.
The auth guard in `NavGraph` then redirects to `login` automatically.

### T2.5 / T5.3 – Logcat

All auth and DB layers log every operation:

| Tag | Sample levels |
|---|---|
| `AuthRepository` | `D` attempt, `I` success, `E` failure with throwable |
| `TripRepository` | `D` reads, `I` writes |
| `ActivityRepository` | `D` reads, `I` writes |
| `UserRepository` | `I` writes / `W` username collision |
| `TripViewModel`, `ActivityViewModel`, `AuthViewModel` | `I` ok / `W` validation / `E` failure |
| `PegasusApplication` | `I` Hilt init |

### T3.1 – Register screen

`RegisterScreen.kt` with all required fields (email, password, username, birthdate
via DatePicker, address, country, phone) and a checkbox for "accept emails".
Disables the submit button while a request is in flight (`uiState.isLoading`).

### T3.2 – Register + email verification + Repository

`AuthRepositoryImpl.register()`:

1. `createUserWithEmailAndPassword().await()`
2. `user.sendEmailVerification().await()` — sends the Firebase verification link.

The `AuthViewModel.register()` then writes the extended profile into Room and
records a `LOGIN` access-log row. Username uniqueness is checked **before** hitting
Firebase to avoid orphan accounts.

### T3.3 – Recover password

New screen `RecoverPasswordScreen.kt` (route `recover`). Submits to
`AuthViewModel.recoverPassword(email)` →
`AuthRepository.sendPasswordResetEmail(email)` → Firebase sends the reset link.

### T4.1 – User table

Schema:
```
users(
  uid TEXT PK,
  email TEXT,
  username TEXT UNIQUE,
  displayName TEXT,
  birthdate TEXT,
  address TEXT,
  country TEXT,
  phone TEXT,
  acceptEmails INTEGER,
  photoUrl TEXT,
  createdAt INTEGER
)
```

`@Entity(indices = [Index(value = ["username"], unique = true)])` enforces uniqueness
at the SQL level. `UserRepository.saveUser()` also pre-checks
`isUsernameTakenByOther()` and returns a `Result.failure` with a localised message
when the username collides — surfaced through the registration UI.

### T4.2 – Multi-user trips

`Trip.userId` is a foreign key to `users.uid` with `ON DELETE CASCADE`. All trip queries
in `TripDao` are scoped: `WHERE userId = :userId`. The `flatMapLatest` in
`TripViewModel` wires the trip list to the active Firebase session, so logging in /
out instantly swaps the visible data set.

### T4.3 – design.md updated

`docs/design.md §4 — Sprint 03 Persistence & Authentication Layer` documents the
architecture diagram, full schema, new/modified files and the auth flow.

### T4.4 – Access log

```
access_logs(
  id INTEGER PK AUTOINCREMENT,
  userId TEXT FK→users.uid CASCADE,
  event TEXT,                -- "LOGIN" | "LOGOUT"
  timestamp INTEGER
)
```

`AuthViewModel.login()` writes a `LOGIN` row, `logout()` writes a `LOGOUT` row.

### T5.1 – Unit tests

The DAO/Repository tests use Robolectric + an in-memory Room DB; the ViewModel
tests use `mockito-kotlin` + a `StandardTestDispatcher`. All 122 production
tests run on the JVM (no emulator needed):

| File | Tests | Purpose |
|---|---|---|
| `TripCrudTest.kt` | 22 | CRUD, multi-user isolation (T4.2), title-uniqueness incl. self-exclusion (T5.2), reactive `Flow` (T1.6), cascade-to-activities, ordering, PK collision, unknown-id no-ops |
| `ActivityCrudTest.kt` | 16 | CRUD, ordering by (date asc, time asc), reactive `Flow`, `LocalDate`/`LocalTime` round-trip via `Converters`, cascade on trip delete, integer + datetime field persistence |
| `UserAccessLogTest.kt` | 18 | User CRUD, unique username on `saveUser` and `updateUser` (with self-edit allowed), `findByUsername`, observe Flow, access log: insert / scoped per-user / sorted DESC / `countForUser` / `getAll` / persists without FK / `runCatching` swallows errors |
| `ConvertersTest.kt` | 8 | `LocalDate`/`LocalTime` round-trip, edge of year, midnight, `null` handling in both directions, defensive parsing of malformed strings |
| `TripViewModelTest.kt` | 18 | Every validation path (empty title / start / end / description, end < start, unparseable date, duplicate title with self-exclusion, no auth user), happy paths verifying repository arguments via `argumentCaptor`, trimming, `clearError`, **trips StateFlow flips when auth state changes (T1.6)** |
| `ActivityViewModelTest.kt` | 15 | Validation (empty title, null date/time, before-start, after-end, exact-bounds inclusive, unknown trip, unparseable parent dates), happy paths, `loadActivities`, `clearError` |
| `AuthViewModelTest.kt` | 17 | Login / Register / Recover / Logout / Resend-verification flows, `ensureLocalProfile` upsert (skipped when row already exists), Firebase failure paths, username collision short-circuit, `clearMessages`, logout writes `LOGOUT` log |
| `UserViewModelTest.kt` | 8 | `currentProfile` flow logged-out/logged-in/auth-state-change, `accessLogs` flow with `WhileSubscribed` semantics, `updateProfile` success + failure surfacing, `clearError` |
| `ExampleUnitTest.kt` | 1 | (legacy) |
| **TOTAL** | **123** | All passing |

### T5.2 – Validation

| Rule | Where enforced |
|---|---|
| `startDate ≤ endDate` | `TripViewModel.isStartBeforeOrEqualEnd` |
| Non-empty title / dates / description | `TripViewModel.validateTripFields` |
| No duplicate trip title per user | `TripDao.isTitleTakenByOther` + `TripViewModel.addTrip/editTrip` |
| Activity date inside trip range | `ActivityViewModel.isDateWithinTripRange` |
| Unique username across the app | `UserDao` index + `UserRepository.saveUser` |

### T5.4 – Documentation

`docs/design.md` extended with §4 "Sprint 03 — Persistence & Authentication Layer".
`docs/plan_sprint03.md` Done column populated.

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
  TripCrudTest          22/22 PASS
  ActivityCrudTest      16/16 PASS
  UserAccessLogTest     18/18 PASS
  ConvertersTest         8/8  PASS
  TripViewModelTest     18/18 PASS
  ActivityViewModelTest 15/15 PASS
  AuthViewModelTest     17/17 PASS
  UserViewModelTest      8/8  PASS
  ExampleUnitTest        1/1  PASS
  ─────────────────────────────────
  TOTAL                123/123 PASS
```

---

## 3. Fixes Applied During Sprint

| Issue | Fix |
|---|---|
| Sprint 02 ViewModels exposed synchronous CRUD; Room is suspend-only | Migrated all repository methods to `suspend` + `Flow`; ViewModels run them in `viewModelScope`; screens consume via `collectAsState()` and use callbacks (`onResult: (Boolean) -> Unit`) for save buttons |
| `getTripById(id)` was synchronous and used directly inside Composables | Replaced with `LaunchedEffect` + `mutableStateOf` to load asynchronously when the screen enters composition |
| Activity entity used `LocalDate`/`LocalTime`, not natively supported by Room | Added `Converters` (`@TypeConverters`) that persist them as ISO-8601 strings |
| `Trip` entity had no integer column (PDF requires datetime+text+integer) | Added `budget: Int` and `createdAt: Long` to `Trip`; added `durationMinutes: Int` and `createdAt: Long` to `Activity` |
| Manual `Task<T>.await()` helper had unsafe cast for `Task<Void>` | Replaced with the official `kotlinx-coroutines-play-services` `Task.await()` |
| Splash always navigated to `home`, ignoring auth state | `SplashScreen` now reads `AuthViewModel.currentUser` and routes to `home` or `login` based on session state |
| `loadTrips()` calls inside screens after every navigation back | Removed — `TripViewModel.trips` is now a hot Flow scoped to the auth state |
| Initial gradle config used `libs.plugins.google-services` — Kotlin DSL parsed `-` as the minus operator | Renamed alias accessor to `libs.plugins.google.services` |
| AGP 9.0.1 (canary) was incompatible with Hilt's `BaseExtension` lookup | Downgraded AGP to **8.11.1** stable + Gradle wrapper to **8.13** + bumped Hilt to **2.56.2** |
| `kotlin.android` plugin missing in app module → `compileDebugKotlin` was skipped → APK shipped without Kotlin classes → `ClassNotFoundException: PegasusApplication` at launch | Re-added `alias(libs.plugins.kotlin.android)` to the app module |
| KSP failed with *"Inconsistent JVM-target compatibility (Java 11 vs Kotlin 22)"* | Added explicit `kotlinOptions { jvmTarget = "11" }` to align with `compileOptions` |
| Login crashed for accounts created in Firebase Console (no row in local `users` table → FK violation on `access_logs.userId`) | Added `AuthViewModel.ensureLocalProfile()` which upserts a stub User row on first login |
| Logout occasionally exited the app instead of returning to Login | `ProfileScreen` now navigates explicitly to `login` after `authViewModel.logout()`, with `popUpTo(navController.graph.id) { inclusive = true }` |
| `access_logs.userId` FK could still cause a crash under transient state | Schema bumped to **v2**: removed FK on `access_logs.userId` (audit logs are best-effort and must always be writable). `UserRepositoryImpl.logAccess` also wraps the insert in `runCatching` |
| `ActivityViewModel` only refreshed via manual `loadActivities()` after each CRUD | Re-implemented as a hot Flow: `_currentTripId` + `flatMapLatest` + `observeByTripId(tripId)`. The detail screen sets the trip id once on entry and Room writes propagate automatically (proper T1.6) |
| `androidx.test.core.app.ApplicationProvider` was missing from `testImplementation` | Added `androidx.test:core:1.6.1` to `libs.versions.toml` and the app module |
| `about_version` string still said `v2.0.0` | Updated to `v3.0.0` in `values/`, `values-es/`, `values-ca/` |

---

## 4. Domain Model Changes

| Element | Change | Reason |
|---|---|---|
| `Trip` | added `userId` (FK), `budget: Int`, `createdAt: Long`; promoted to `@Entity` | T1.2 (integer field), T4.2 (multi-user) |
| `Activity` | added `durationMinutes: Int`, `createdAt: Long`; promoted to `@Entity` | T1.2 |
| `User` | promoted to `@Entity`; added `username` (unique index), `birthdate`, `address`, `country`, `phone`, `acceptEmails` | T4.1 |
| `AccessLog` | new domain model + `@Entity` | T4.4 |
| `TripRepository`, `ActivityRepository` | switched to `suspend` + `Flow` | Room API |
| `AuthRepository`, `UserRepository` | new interfaces | T2 / T3 / T4 |

`docs/domain_model.png` will be regenerated for v3.0.0.

---

## 5. Deviations from `plan_sprint03.md`

The plan was followed end-to-end (22/22 tasks complete, full architecture matches),
with two deliberate deviations made during implementation. Both are documented
here for transparency.

### 5.1 `AccessLog` — single class, not split into Entity + domain model

| Plan said | What was implemented |
|---|---|
| Two files: `data/local/entity/AccessLogEntity.kt` (Room `@Entity`) + `domain/AccessLog.kt` (domain model) | Single file: `domain/AccessLog.kt` that is both the Room `@Entity` and the model exposed through repositories |

**Why this is better:**

1. **Consistency with `Trip`, `Activity`, `User`.** The same plan states those three live in `domain/` *and* are `@Entity`-annotated (see "Existing files to modify"). Treating `AccessLog` differently — splitting it in two — would create an inconsistency where one of four tables follows a different convention.
2. **Avoids ~30 lines of dead mapping code.** A separate `AccessLogEntity` would only differ from the domain model in the `@Entity` annotation. We'd have to write `toDomain()`/`toEntity()` mappers that do nothing but copy fields one-to-one. Kotlin data classes already give us `copy()` for everything we need.
3. **No leakage in either direction.** The "split entity vs domain" pattern is valuable when:
   - The persistence model has DB-only metadata (revision tags, sync flags…) you don't want UI to see, *or*
   - The domain model has transient computed fields you don't want to persist.
   `AccessLog` has neither — `id`, `userId`, `event`, `timestamp` are exactly the same in both worlds.
4. **`Trip` and `Activity` already proved this works.** Sprint 02 had `Trip` as a plain `data class` in `domain/`. Sprint 03 just added the `@Entity` annotation. Repositories return `Trip` directly. Same approach, no friction.

**Trade-off accepted:** if a future sprint needs to expose a different `AccessLog` shape to the UI (e.g. enriched with a username), we'll split then. YAGNI for now.

### 5.2 Logout button location: `ProfileScreen` instead of `PreferencesScreen`

| Plan said | What was implemented |
|---|---|
| Add the logout action in `ui/screens/PreferencesScreen.kt` | Logout sits in `ui/screens/ProfileScreen.kt`, in the existing "Account" section |

**Why this is better:**

1. **The PDF (T2.4) explicitly allows either** — it says *"create an action in the app to allow the user log out"*, with no constraint on which screen. The plan was an internal choice, not a hard requirement.
2. **The slot was already there.** Sprint 02's `ProfileScreen` had a `ProfileRow` with `Icons.Filled.Logout`, "Cerrar sesión" label, and a `// @TODO implement logout` comment. Implementing the action *in the slot the previous sprint left for it* avoided creating a new UI element and dead code in two places.
3. **It matches the mental model users already have.** Across consumer apps (Google, Twitter/X, Instagram, GitHub mobile) "Sign out" lives under the **Profile / Account** section, not under generic "Settings/Preferences". Putting it where users instinctively look reduces friction.
4. **`PreferencesScreen` is for *preferences*, not session control.** That screen handles language, theme, notifications, username/DOB editing — all things that survive logout. Mixing a destructive session-ending action with cosmetic toggles is a UX smell.
5. **Single source of truth.** Adding logout to *both* screens would require keeping their behavior in sync (same access-log row, same back-stack reset). Keeping it in one place avoids that duplication.

**Trade-off accepted:** none. The plan didn't justify the Preferences placement; the original Sprint 02 code already pointed to Profile; and Profile is the standard convention.

> Note: `plan_sprint03.md` itself has *not* been retroactively edited to reflect these deviations — the plan stays as the planning artefact it was, and this final report is the source of truth for what actually shipped.

---

## 6. Known Limitations

- `google-services.json` is required to build — not committed to the public repo.
  The developer must download it from the Firebase console and place it in `app/`.
- The Room DB uses `fallbackToDestructiveMigration()` so any future schema change
  will wipe local data on the dev's device. A proper `Migration` will be added for
  the first production release.
- Email verification arrival depends on the Firebase service — the user has to
  click the link in the email manually; the app does **not** block login until verified.
- Access-log table has no retention policy yet — it grows monotonically. A periodic
  cleanup is scheduled for a future sprint.
- The demo video is hosted externally (OneDrive UdL) and linked from
  `docs/evidence/v3.0.0/video.txt`, as in Sprint 02. The link covers the full
  Sprint 03 flow with focus on the auth pieces (login, register, recover, logout).
