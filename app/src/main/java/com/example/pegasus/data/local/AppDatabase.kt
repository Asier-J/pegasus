package com.example.pegasus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pegasus.data.local.dao.AccessLogDao
import com.example.pegasus.data.local.dao.ActivityDao
import com.example.pegasus.data.local.dao.TripDao
import com.example.pegasus.data.local.dao.UserDao
import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.User

/**
 * Sprint 03: Room database for Pegasus.
 *
 * Schema v2:
 *  - users          (uid PK, unique username)
 *  - trips          (id PK, userId FK → users.uid, CASCADE)
 *  - activities     (id PK, tripId FK → trips.id, CASCADE)
 *  - access_logs    (id PK auto, userId — plain text, no FK so audit logs are
 *                    always writable even if the local user mirror isn't ready)
 *
 * v1 → v2: dropped the FK constraint on access_logs.userId.
 *
 * `fallbackToDestructiveMigration` is enabled in the Hilt module ONLY for
 * development convenience — proper Migration objects must be added before
 * shipping a production release.
 */
@Database(
    entities = [
        User::class,
        Trip::class,
        Activity::class,
        AccessLog::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun activityDao(): ActivityDao
    abstract fun accessLogDao(): AccessLogDao

    companion object {
        const val DB_NAME = "pegasus.db"
    }
}
