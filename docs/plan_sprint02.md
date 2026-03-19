# Sprint 02 – Planning Document

## 1. Sprint Goal

Implement the main logic of the Travel Planner application: CRUD operations for trips and
itinerary activities, data validation, user settings persistence, and unit testing.
The architecture follows the MVVM pattern:
`UI (Screens) → ViewModel → Repository (interface) → RepositoryImpl → FakeTripDataSource (InMemory)`

---

## 2. Sprint Backlog

| ID   | Task | Assignee | Estimation (h) | Priority | Done |
|------|------|----------|----------------|----------|------|
| T1.1 | Implement inMemory CRUD for trips (addTrip, editTrip, deleteTrip) following MVVM | Asier | 3 | Very High | Done (3) |
| T1.2 | Implement inMemory CRUD for activities (addActivity, updateActivity, deleteActivity) following MVVM | Asier | 2 | Very High | Done (2.5) |
| T1.3 | Data validation: DatePicker fields, start < end, activity within trip range | Asier | 2 | High | Done (2) |
| T1.4 | User settings with SharedPreferences (username, DOB, dark mode, language) | Asier | 1.5 | High | Done (1.5) |
| T1.5 | Multi-language support (EN, CA, ES) – extend existing strings for new screens | Asier | 1 | Medium | Done (1) |
| T2.1 | Structure itinerary flow: Menu → Travel → Itinerary | Asier | 1 | High | Done (0.5) |
| T2.2 | Implement basic UI flow (TripListScreen, TripDetailScreen, AddEditTripScreen, AddEditActivityScreen) | Asier | 3 | Very High | Done (4) |
| T2.3 | Ensure dynamic updates in trip list and itinerary list | Asier | 0.5 | Medium | Done (0.5) |
| T3.1 | Input validation in ViewModel and UI layers with error messages | Asier | 1 | High | Done (1) |
| T3.2 | Write unit tests for trip and activity CRUD operations | Asier | 2 | High | Done (2) |
| T3.3 | Simulate user interactions and log errors to Logcat | Asier | 0.5 | Medium | Done (0.5) |
| T3.4 | Update documentation with test results and fixes | Asier | 0.5 | Low | Done (0.5) |
| T3.5 | Add Logcat logs (DEBUG/INFO/ERROR) and code comments | Asier | 0.5 | Medium | Done (0.5) |

---

## 3. Architecture

```
UI (Screens)
    └── ViewModel (TripViewModel, ActivityViewModel)
            └── Repository interface (TripRepository, ActivityRepository)
                    └── RepositoryImpl (TripRepositoryImpl, ActivityRepositoryImpl)
                            └── FakeTripDataSource (InMemory singleton)
```

**New files created:**
- `domain/Trip.kt` — updated: title, startDate (dd/MM/yyyy), endDate, description
- `domain/Activity.kt` — title, description, date (LocalDate), time (LocalTime)
- `domain/TripRepository.kt` — CRUD interface
- `domain/ActivityRepository.kt` — CRUD interface
- `data/fakeDB/FakeTripDataSource.kt` — InMemory singleton with preloaded fake dataset
- `data/repository/TripRepositoryImpl.kt`
- `data/repository/ActivityRepositoryImpl.kt`
- `ui/viewmodels/TripViewModel.kt`
- `ui/viewmodels/ActivityViewModel.kt`
- `ui/screens/AddEditTripScreen.kt`
- `ui/screens/TripDetailScreen.kt`
- `ui/screens/AddEditActivityScreen.kt`

**Modified files:**
- `ui/screens/TripListScreen.kt` — connected to TripViewModel
- `ui/screens/PreferencesScreen.kt` — added username, date of birth (SharedPreferences)
- `NavGraph.kt` — added trip_detail, add_trip, edit_trip, add_activity, edit_activity routes
- `app/build.gradle.kts` — added lifecycle-viewmodel-compose, coroutines-test

---

## 4. Definition of Done (DoD)

- [x] InMemory CRUD implemented for trips and activities
- [x] MVVM architecture respected (UI → ViewModel → Repository → DataSource)
- [x] DatePicker used in all date fields (no free-text entry)
- [x] Date validation: start ≤ end, activity within trip range
- [x] User settings persisted with SharedPreferences and loaded on startup
- [x] Multi-language support for all new screens (EN, CA, ES)
- [x] Unit tests for CRUD operations passing
- [x] Logcat logs at appropriate levels (DEBUG, INFO, WARN, ERROR)
- [x] Release v2.0.0 published

---

## 5. Identified Risks

- InMemory data is lost on app restart (by design for this sprint)
- Unit tests use FakeTripDataSource singleton — `clearAll()` must be called in `@Before`
- DatePicker on Material3 requires `ExperimentalMaterial3Api` opt-in
