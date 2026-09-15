package com.example

import com.example.data.SessionManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun sessionManager_validWithin30Days() {
        val now = 1700000000000L
        val twentyNineDaysAgo = now - (29L * 24 * 60 * 60 * 1000L)
        assertTrue("Session active within 29 days should be valid", SessionManager.isSessionValid(twentyNineDaysAgo, now))

        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000L)
        assertTrue("Session active exactly at 30 days should be valid", SessionManager.isSessionValid(thirtyDaysAgo, now))
    }

    @Test
    fun sessionManager_expiredAfter30Days() {
        val now = 1700000000000L
        val thirtyOneDaysAgo = now - (31L * 24 * 60 * 60 * 1000L)
        assertFalse("Session active 31 days ago should be expired", SessionManager.isSessionValid(thirtyOneDaysAgo, now))

        val sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000L)
        assertFalse("Session active 60 days ago should be expired", SessionManager.isSessionValid(sixtyDaysAgo, now))

        assertFalse("Zero timestamp should be invalid", SessionManager.isSessionValid(0L, now))
    }
}

