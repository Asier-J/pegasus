package com.example.pegasus.data.fakeDB

/**
 * Sprint 02: in-memory data source for trips/activities.
 * Sprint 03: REPLACED by Room (see `data/local/AppDatabase.kt` and the DAOs).
 *
 * This object is kept only as a compatibility stub and should NOT be used
 * by new code. It will be removed in a later cleanup commit.
 */
@Deprecated(
    "Replaced by Room (Sprint 03). Use TripRepository / ActivityRepository instead.",
    level = DeprecationLevel.WARNING
)
object FakeTripDataSource {
    /** Test helper retained so existing Sprint 02 tests still compile if referenced. */
    fun clearAll() { /* no-op in Sprint 03 */ }
}
