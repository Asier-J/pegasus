package com.example.pegasus

import com.example.pegasus.data.local.Converters
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Sprint 03 — T1.2: TypeConverters used by Room to persist `LocalDate` and
 * `LocalTime` as ISO-8601 strings. Pure JVM tests — no DB or Android needed.
 *
 * Coverage:
 *  - Round-trip for both types
 *  - Null handling in both directions
 *  - Defensive parsing: malformed input → null instead of crash
 */
class ConvertersTest {

    private lateinit var c: Converters

    @Before fun setUp() { c = Converters() }

    // ── LocalDate ─────────────────────────────────────────────────────────────
    @Test
    fun localDate_roundtrip() {
        val date = LocalDate.of(2026, 6, 15)
        val str = c.fromLocalDate(date)
        assertEquals("2026-06-15", str)
        assertEquals(date, c.toLocalDate(str))
    }

    @Test
    fun localDate_handlesEdgeOfYear() {
        val first = LocalDate.of(2026, 1, 1)
        val last  = LocalDate.of(2026, 12, 31)
        assertEquals(first, c.toLocalDate(c.fromLocalDate(first)))
        assertEquals(last,  c.toLocalDate(c.fromLocalDate(last)))
    }

    @Test
    fun localDate_nullInput_producesNull() {
        assertNull(c.fromLocalDate(null))
        assertNull(c.toLocalDate(null))
    }

    @Test
    fun localDate_invalidString_returnsNullInsteadOfThrowing() {
        // Defensive: malformed input must not crash the DB read path.
        assertNull(c.toLocalDate("not-a-date"))
        assertNull(c.toLocalDate(""))
        assertNull(c.toLocalDate("15/06/2026")) // wrong format (we use ISO-8601)
    }

    // ── LocalTime ─────────────────────────────────────────────────────────────
    @Test
    fun localTime_roundtrip() {
        val time = LocalTime.of(21, 30)
        val str = c.fromLocalTime(time)
        assertEquals(time, c.toLocalTime(str))
    }

    @Test
    fun localTime_handlesMidnightAndEndOfDay() {
        val midnight = LocalTime.of(0, 0)
        val late     = LocalTime.of(23, 59, 59)
        assertEquals(midnight, c.toLocalTime(c.fromLocalTime(midnight)))
        assertEquals(late,     c.toLocalTime(c.fromLocalTime(late)))
    }

    @Test
    fun localTime_nullInput_producesNull() {
        assertNull(c.fromLocalTime(null))
        assertNull(c.toLocalTime(null))
    }

    @Test
    fun localTime_invalidString_returnsNullInsteadOfThrowing() {
        assertNull(c.toLocalTime("nope"))
        assertNull(c.toLocalTime(""))
        assertNull(c.toLocalTime("25:00"))   // hour out of range
    }
}
