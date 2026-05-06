@file:Suppress("LongMethod")

package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class Competitions : Activity() {

    private lateinit var backCompetitions: Button
    private lateinit var addCompetition: Button
    private lateinit var viewCalendar: Button
    private lateinit var layoutUpcomingHeader: LinearLayout
    private lateinit var layoutPastResultsHeader: LinearLayout
    private lateinit var recyclerUpcomingCompetitions: RecyclerView
    private lateinit var recyclerCompetitionResults: RecyclerView
    private lateinit var competitionAdapter: Competition
    private lateinit var resultAdapter: CompetitionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.competitions)

        backCompetitions = findViewById(R.id.backCompetitions)
        addCompetition = findViewById(R.id.addCompetition)
        viewCalendar = findViewById(R.id.viewCalendar)
        layoutUpcomingHeader = findViewById(R.id.layoutUpcomingHeader)
        layoutPastResultsHeader = findViewById(R.id.layoutPastResultsHeader)
        recyclerUpcomingCompetitions = findViewById(R.id.recyclerUpcomingCompetitions)
        recyclerCompetitionResults = findViewById(R.id.recyclerCompetitionResults)

        competitionAdapter = Competition(this, mutableListOf()) {
            loadCompetitions()
            loadResults()
        }

        resultAdapter = CompetitionResult(this, mutableListOf()) {
            loadCompetitions()
            loadResults()
        }
        recyclerUpcomingCompetitions.layoutManager = LinearLayoutManager(this)
        recyclerUpcomingCompetitions.adapter = competitionAdapter
        recyclerUpcomingCompetitions.isNestedScrollingEnabled = false

        recyclerCompetitionResults.layoutManager = LinearLayoutManager(this)
        recyclerCompetitionResults.adapter = resultAdapter
        recyclerCompetitionResults.isNestedScrollingEnabled = false

        backCompetitions.setOnClickListener {
            finish()
        }
        addCompetition.setOnClickListener {
            showAddCompetitionDialog()
        }
        viewCalendar.setOnClickListener {
            startActivity(Intent(this, CompetitionCalendar::class.java))
        }
        layoutUpcomingHeader.setOnClickListener {
            startActivity(Intent(this, UpcomingCompetitions::class.java))
        }
        layoutPastResultsHeader.setOnClickListener {
            startActivity(Intent(this, PastResults::class.java))
        }

        loadCompetitions()
        loadResults()
    }

    override fun onResume() {
        super.onResume()
        loadCompetitions()
        loadResults()
    }
    private fun loadCompetitions() {
        val userId = SessionManager(this).getUserId()

        RetrofitClient.apiService.getCompetitions(userId)
            .enqueue(object : Callback<CompetitionsResponse> {
                override fun onResponse(
                    call: Call<CompetitionsResponse>,
                    response: Response<CompetitionsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val summaryList = response.body()!!.competitions.take(SUMMARY_LIMIT)
                        competitionAdapter.updateData(summaryList)
                    } else {
                        Toast.makeText(
                            this@Competitions,
                            LOAD_COMPETITIONS_FAILED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<CompetitionsResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@Competitions,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    private fun loadResults() {
        val userId = SessionManager(this).getUserId()

        RetrofitClient.apiService.getCompetitionResults(userId)
            .enqueue(object : Callback<CompetitionResultsResponse> {
                override fun onResponse(
                    call: Call<CompetitionResultsResponse>,
                    response: Response<CompetitionResultsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val summaryList = response.body()!!.results.take(SUMMARY_LIMIT)
                        resultAdapter.updateData(summaryList)
                    } else {
                        Toast.makeText(
                            this@Competitions,
                            LOAD_RESULTS_FAILED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<CompetitionResultsResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@Competitions,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    private fun showAddCompetitionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_competition, null)

        val name = dialogView.findViewById<EditText>(R.id.competitionName)
        val location = dialogView.findViewById<EditText>(R.id.competitionLocation)
        val date = dialogView.findViewById<EditText>(R.id.competitionDate)
        val competitionSport =
            dialogView.findViewById<Spinner>(R.id.competitionSport)
        val competitionEventType =
            dialogView.findViewById<EditText>(R.id.competitionEventType)
        val description =
            dialogView.findViewById<EditText>(R.id.competitionDescription)

        val sports = listOf(RUNNING_TEXT, CYCLING_TEXT, SWIMMING_TEXT)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sports
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        competitionSport.adapter = adapter

        date.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    date.setText(
                        String.format(
                            Locale.getDefault(),
                            DATE_PICKER_FORMAT,
                            year,
                            month + MONTH_OFFSET,
                            day
                        )
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(ADD_COMPETITION_TITLE)
            .setView(dialogView)
            .setPositiveButton(SAVE_TEXT) { _, _ ->
                val userId = SessionManager(this).getUserId()

                val selectedSportId = when (
                    competitionSport.selectedItem.toString()
                ) {
                    RUNNING_TEXT -> RUNNING_ID
                    CYCLING_TEXT -> CYCLING_ID
                    SWIMMING_TEXT -> SWIMMING_ID
                    else -> RUNNING_ID
                }

                val request = CreateCompetitionRequest(
                    userId = userId,
                    name = name.text.toString().trim(),
                    location =location.text.toString().trim(),
                    competitionDate = date.text.toString().trim(),
                    sportId = selectedSportId,
                    eventType = competitionEventType.text.toString().trim(),
                    description = description.text.toString().trim()
                )

                RetrofitClient.apiService.createCompetition(request)
                    .enqueue(object : Callback<BasicApiResponse> {
                        override fun onResponse(
                            call: Call<BasicApiResponse>,
                            response: Response<BasicApiResponse>
                        ) {
                            if (response.isSuccessful &&
                                response.body()?.success == true
                            ) {
                                loadCompetitions()
                            } else {
                                val message = response.body()?.message
                                    ?: CREATE_COMPETITION_FAILED_MESSAGE
                                Toast.makeText(
                                    this@Competitions,
                                    message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<BasicApiResponse>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@Competitions,
                                "Error: ${t.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
            .setNegativeButton(CANCEL_TEXT, null)
            .show()
    }

    companion object {
        private const val SUMMARY_LIMIT = 1
        private const val MONTH_OFFSET = 1
        private const val RUNNING_ID = 1
        private const val CYCLING_ID = 2
        private const val SWIMMING_ID = 3
        private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"
        private const val RUNNING_TEXT = "Running"
        private const val CYCLING_TEXT = "Cycling"
        private const val SWIMMING_TEXT = "Swimming"
        private const val ADD_COMPETITION_TITLE = "Add Competition"
        private const val SAVE_TEXT = "Save"
        private const val CANCEL_TEXT = "Cancel"
        private const val LOAD_COMPETITIONS_FAILED_MESSAGE =
            "Failed to load competitions"
        private const val LOAD_RESULTS_FAILED_MESSAGE = "Failed to load results"
        private const val CREATE_COMPETITION_FAILED_MESSAGE =
            "Failed to create competition"
    }
}
