package com.example.englishcentre

import com.example.englishcentre.data.Course
import com.example.englishcentre.ui.clash
import com.example.englishcentre.ui.money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private fun course(id: Long, day: Int, start: String, end: String) =
        Course(id, "C$id", "Course $id", 1, day, start, end, "A1", 0, 10, "2026-01-01")

    @Test
    fun moneyFormat() = assertEquals("4.500.000 ₫", money(4500000))

    @Test
    fun timetableClash() {
        val a = course(1, 1, "18:00", "19:30")
        assertTrue(a.clash(course(2, 1, "19:00", "20:30")))
        assertFalse(a.clash(course(3, 1, "19:30", "21:00")))
        assertFalse(a.clash(course(4, 2, "18:00", "19:30")))
        assertFalse(a.clash(a))
    }
}
