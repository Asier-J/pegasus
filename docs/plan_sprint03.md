# Sprint 03 – Planning Document

## 1. Sprint Goal

Replace the in-memory storage from Sprint 02 with a persistent SQLite database using Room,
integrate Firebase Authentication (login, register, password recovery), persist user information
locally, and enforce multi-user trip ownership. The architecture must use the Repository pattern
with Hilt as DI library:
`UI (Screens) → ViewModel → Repository (interface) → RepositoryImpl → Room DAOs → SQLite`

---

## 2. Sprint Backlog

| ID   | Task | Assignee | Estimation (h) | Priority | Done |
|------|------|----------|----------------|----------|------|
| T1.1 | Create Room Database class (`AppDatabase`) | Asier | 1 | Very High | |
| T1.2 | Define Room Entities for `Trip` and `Activity` (datetime, text, integer fields) | Asier | 1.5 | Very High | |
| T1.3 | Create DAOs (`TripDao`, `ActivityDao`) for database operations | Asier | 2 | Very High | |
| T1.4 | Implement CRUD operations using DAOs for trips and activities | Asier | 2 | Very High | |
| T1.5 | Modify ViewModels to use Room Database instead of in-memory storage | Asier | 2 | Very High | |
| T1.6 | Ensure UI updates reactively when database changes (Flow/LiveData) | Asier | 1 | High | |
| T2.1 | Connect app to Firebase (google-services.json, dependencies) | Asier | 1 | Very High | |
| T2.2 | Design Login screen (email & password form) | Asier | 1.5 | Very High | |
| T2.3 | Implement Firebase email/password login logic | Asier | 2 | Very High | |
| T2.4 | Create logout action in the app (e.g., in preferences/menu) | Asier | 0.5 | High | |
| T2.5 | Use Logcat to track all auth operations and errors | Asier | 0.5 | Medium | |
| T3.1 | Design Register screen (registration form) | Asier | 1.5 | High | |
| T3.2 | Implement Firebase registration with email verification (Repository pattern) | Asier | 2 | High | |
| T3.3 | Implement password recovery action and screen | Asier | 1.5 | High | |
| T4.1 | Persist user info in local DB (User table: login, username, birthdate, address, country, phone, accept emails) with unique username check | Asier | 2 | High | |
| T4.2 | Change Trip table to support multiple users; filter trips by logged-in user | Asier | 2 | High | |
| T4.3 | Update `design.md` with database schema and usage documentation | Asier | 1 | Medium | |
| T4.4 | Persist application access log (table with userId, datetime, login/logout event) | Asier | 1.5 | Medium | |
| T5.1 | Write unit tests for DAOs and database interactions | Asier | 2 | High | |
| T5.2 | Implement data validation (prevent duplicate trip names, check valid dates) | Asier | 1 | High | |
| T5.3 | Use Logcat to track database operations and errors | Asier | 0.5 | Medium | |
| T5.4 | Update documentation with test results and database usage at `design.md` | Asier | 0.5 | Low | |

---

## 3. Architecture

```
UI (Screens)
    └── ViewModel (TripViewModel, ActivityViewModel, AuthViewModel, UserViewModel)
            └── Repository interface (TripRepository, ActivityRepository, AuthRepository, UserRepository)
                    └── RepositoryImpl (injected via Hilt)
                            ├── Room DAOs → SQLite (local persistence)
                            └── Firebase Auth (authentication)
```

**New files to create:**
- `data/local/AppDatabase.kt` — Room Database class
- `data/local/dao/TripDao.kt` — DAO for Trip CRUD
- `data/local/dao/ActivityDao.kt` — DAO for Activity CRUD
- `data/local/dao/UserDao.kt` — DAO for User CRUD
- `data/local/dao/AccessLogDao.kt` — DAO for login/logout access log
- `data/local/entity/AccessLogEntity.kt` — Room Entity for access log
- `data/repository/AuthRepositoryImpl.kt` — Firebase Auth repository
- `data/repository/UserRepositoryImpl.kt` — User repository implementation
- `domain/AuthRepository.kt` — Auth repository interface
- `domain/UserRepository.kt` — User repository interface
- `domain/AccessLog.kt` — Access log domain model
- `ui/viewmodels/AuthViewModel.kt` — Handles login, register, logout
- `ui/viewmodels/UserViewModel.kt` — Handles user profile persistence
- `ui/screens/LoginScreen.kt` — Login form
- `ui/screens/RegisterScreen.kt` — Registration form
- `ui/screens/RecoverPasswordScreen.kt` — Password recovery
- `di/AppModule.kt` — Hilt DI module

**Existing files to modify:**
- `domain/Trip.kt` — add userId field, convert to Room `@Entity`
- `domain/Activity.kt` — convert to Room `@Entity`
- `domain/User.kt` — convert to Room `@Entity`, add fields (address, country, phone, acceptEmails)
- `domain/TripRepository.kt` — update interface for Room (suspend/Flow)
- `domain/ActivityRepository.kt` — update interface for Room (suspend/Flow)
- `data/repository/TripRepositoryImpl.kt` — replace in-memory with Room DAO calls
- `data/repository/ActivityRepositoryImpl.kt` — replace in-memory with Room DAO calls
- `data/fakeDB/FakeTripDataSource.kt` — remove or keep as fallback (replaced by Room)
- `ui/viewmodels/TripViewModel.kt` — inject via Hilt, use Room-backed repository
- `ui/viewmodels/ActivityViewModel.kt` — inject via Hilt, use Room-backed repository
- `ui/screens/PreferencesScreen.kt` — add logout action
- `ui/screens/ProfileScreen.kt` — connect to UserViewModel for local DB persistence
- `MainActivity.kt` — add `@AndroidEntryPoint`, auth state check
- `NavGraph.kt` — add login, register, recover_password routes; auth guard
- `app/build.gradle.kts` — add Room, Firebase Auth, Hilt dependencies
- `docs/design.md` — database schema documentation

---

## 4. Definition of Done (DoD)

- [ ] Room Database created with all entities and DAOs
- [ ] In-memory storage fully replaced by Room persistence
- [ ] Firebase Authentication integrated (login, register, email verification, password recovery)
- [ ] Auth guard: app redirects to login if user is not authenticated
- [ ] Logout action available and functional
- [ ] User info persisted in local DB with unique username validation
- [ ] Trips associated to users; only logged-in user's trips shown
- [ ] Access log table records every login/logout with userId and datetime
- [ ] Hilt used as DI library across the app
- [ ] Unit tests for DAOs and database interactions passing
- [ ] Data validation: no duplicate trip names, valid dates enforced
- [ ] Logcat logs for auth and DB operations at appropriate levels
- [ ] `design.md` updated with database schema
- [ ] Release v3.0.0 published
- [ ] Demo video recorded in `docs/evidence/v3.x.x`

---

## 5. Identified Risks

- Firebase configuration requires `google-services.json` — must not be committed to public repo
- Room migrations needed if schema changes after initial release
- Email verification depends on external Firebase service — may have delays
- Hilt integration requires `@HiltAndroidApp`, `@AndroidEntryPoint` annotations across the app
- Access log table may grow large over time — consider cleanup strategy in future sprints
