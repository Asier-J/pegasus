package com.example.pegasus.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

/**
 * Sprint 03: Room TypeConverters.
 * - LocalDate persisted as ISO-8601 String (yyyy-MM-dd)
 * - LocalTime persisted as ISO-8601 String (HH:mm[:ss])
 *
 * Rationale: storing as ISO strings keeps rows human-readable and stable across
 * timezones/locales, and avoids epoch-day ambiguity when migrating schemas.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? =
        value?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
}
