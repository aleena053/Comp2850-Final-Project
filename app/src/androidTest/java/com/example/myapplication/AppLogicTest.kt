package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*

class AppLogicTests {

    // test that adding a workout increases the history count
    @Test
    fun testWorkoutHistoryUpdate() {
        val workoutHistory = mutableListOf("Morning Run", "Leg Day")
        val newWorkout = "Upper Body"

        workoutHistory.add(newWorkout)

        assertEquals(3, workoutHistory.size)
        assertTrue(workoutHistory.contains("Upper Body"))
    }

    // test the logic for highlighting competition dates
    @Test
    fun testCalendarMarkerLogic() {
        // this simulates your competition dates list
        val competitionDates = listOf("2026-05-20", "2026-06-15")
        val testDate = "2026-05-20"

        // togic to check if the date should have a blue marker
        val shouldShowMarker = competitionDates.contains(testDate)

        assertTrue("The marker should show for a competition date", shouldShowMarker)
    }

    // test sorting past results from upcoming events
    @Test
    fun testCompetitionDateSorting() {
        val currentYear = 2026
        val eventYear = 2025 // A past competition

        val category = if (eventYear < currentYear) "Past" else "Upcoming"

        assertEquals("Past", category)
    }

    // test that empty messages aren't sent
    @Test
    fun testMessageValidation() {
        val messageText = ""
        val isMessageValid = messageText.trim().isNotEmpty()

        assertFalse("Message should not be sent if it's empty", isMessageValid)
    }

    // test that account types lead to the right page
    @Test
    fun testAccountTypeNavigation() {
        val userType = "Business"
        val expectedPage = "ClientManagement"

        val actualPage = if (userType == "Business") "ClientManagement" else "PersonalHome"

        assertEquals(expectedPage, actualPage)
    }
}
