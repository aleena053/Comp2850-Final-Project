package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CompetitionCalendar : Activity() {

    private lateinit var backCompetitionCalendar: Button
    private lateinit var previousMonth: Button
    private lateinit var nextMonth: Button
    private lateinit var calendarMonth: TextView
    private lateinit var competitionCalendarView: CompetitionCalendarView
    private lateinit var calendarSummary: TextView

    private var allCompetitions: List<CompetitionItem> = emptyList()
    private var selectedMonth: YearMonth = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.competition_calendar)

        bindViews()
        setupListeners()
        loadCompetitionsForCalendar()
    }

    override fun onResume() {
        super.onResume()
        if (allCompetitions.isNotEmpty()) {
            updateCalendarSection(allCompetitions)
        }
    }

    // links xml views to code
    private fun bindViews() {
        backCompetitionCalendar = findViewById(R.id.backCompetitionCalendar)
        previousMonth = findViewById(R.id.previousMonth)
        nextMonth = findViewById(R.id.nextMonth)
        calendarMonth = findViewById(R.id.calendarMonth)
        competitionCalendarView = findViewById(R.id.competitionCalendarView)
        calendarSummary = findViewById(R.id.calendarSummary)
    }

    // handles clicks for back and month swaps
    private fun setupListeners() {
        backCompetitionCalendar.setOnClickListener {
            finish()
        }

        previousMonth.setOnClickListener {
            selectedMonth = selectedMonth.minusMonths(MONTH_STEP)
            updateCalendarSection(allCompetitions)
        }

        nextMonth.setOnClickListener {
            selectedMonth = selectedMonth.plusMonths(MONTH_STEP)
            updateCalendarSection(allCompetitions)
        }
    }

    // gets all events from api
    private fun loadCompetitionsForCalendar() {
        val userId = SessionManager(this).getUserId()

        RetrofitClient.apiService.getCompetitions(userId)
            .enqueue(object : Callback<CompetitionsResponse> {
                override fun onResponse(
                    call: Call<CompetitionsResponse>,
                    response: Response<CompetitionsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        allCompetitions = response.body()!!.competitions
                        updateCalendarSection(allCompetitions)
                    } else {
                        showToast(LOAD_COMPETITIONS_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<CompetitionsResponse>, t: Throwable) {
                    showToast("error: ${t.localizedMessage}")
                }
            })
    }

    // refreshes the calendar grid and text summary
    private fun updateCalendarSection(competitions: List<CompetitionItem>) {
        val competitionsThisMonth = getCompetitionsThisMonth(competitions)
        val competitionDays = competitionsThisMonth.map { it.first.dayOfMonth }.toSet()

        competitionCalendarView.setMonth(selectedMonth)
        competitionCalendarView.setCompetitionDates(competitionDays)

        calendarMonth.text = selectedMonth
            .atDay(FIRST_DAY_OF_MONTH)
            .format(monthFormatter)

        calendarSummary.text = createCalendarSummary(competitionsThisMonth)
    }

    // filters the list for the chosen month
    private fun getCompetitionsThisMonth(
        competitions: List<CompetitionItem>
    ): List<Pair<LocalDate, CompetitionItem>> {
        return competitions.mapNotNull { item ->
            convertCompetitionDate(item)?.let { date ->
                date to item
            }
        }.filter { (date, _) ->
            date.year == selectedMonth.year &&
                    date.monthValue == selectedMonth.monthValue
        }.sortedBy { it.first }
    }

    // converts date string to localdate
    private fun convertCompetitionDate(item: CompetitionItem): LocalDate? {
        return try {
            LocalDate.parse(
                item.competitionDate.take(ISO_DATE_LENGTH),
                dateParser
            )
        } catch (_: Exception) {
            null
        }
    }

    // builds the text summary for events
    private fun createCalendarSummary(
        competitionsThisMonth: List<Pair<LocalDate, CompetitionItem>>
    ): String {
        return if (competitionsThisMonth.isEmpty()) {
            NO_EVENTS_THIS_MONTH_MESSAGE
        } else {
            competitionsThisMonth.joinToString(NEW_LINE_SEPARATOR) { (date, item) ->
                "${date.format(displayDateFormatter)} • ${item.name}"
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MONTH_STEP = 1L
        private const val FIRST_DAY_OF_MONTH = 1
        private const val ISO_DATE_LENGTH = 10
        private const val ISO_DATE_PATTERN = "yyyy-MM-dd"
        private const val MONTH_DISPLAY_PATTERN = "MMMM yyyy"
        private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"
        private const val NEW_LINE_SEPARATOR = "\n"
        private const val LOAD_COMPETITIONS_FAILED_MESSAGE =
            "failed to load competitions"
        private const val NO_EVENTS_THIS_MONTH_MESSAGE =
            "no upcoming events this month"
        private val dateParser: DateTimeFormatter =
            DateTimeFormatter.ofPattern(ISO_DATE_PATTERN, Locale.ENGLISH)
        private val monthFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(MONTH_DISPLAY_PATTERN, Locale.ENGLISH)
        private val displayDateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN, Locale.ENGLISH)
    }
}
