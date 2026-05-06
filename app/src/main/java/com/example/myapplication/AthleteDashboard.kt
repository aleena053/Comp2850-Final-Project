package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import android.annotation.SuppressLint

@SuppressLint("SetTextI18n")
class AthleteDashboard : Activity() {
    // Profile text fields
    private lateinit var athleteProfile: TextView
    private lateinit var athleteUsername: TextView
    // Buttons
    private lateinit var settings: ImageButton
    private lateinit var messages: Button
    private lateinit var competitions: Button
    // Clickable dashboard sections
    private lateinit var logWorkout: LinearLayout
    private lateinit var workoutHistory: LinearLayout
    private lateinit var trainingPlans: LinearLayout
    // Weekly stats display
    private lateinit var weekDistance: TextView
    private lateinit var weekTime: TextView
    private lateinit var weekAvgPace: TextView
    private lateinit var weekHeartRate: TextView
    // Custom chart view for weekly cardio activities
    private lateinit var lineChartActivity: WeeklyDistanceChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load dashboard UI layout
        setContentView(R.layout.athlete_dashboard)

        // Connect XML UI components to Kotlin variables
        athleteProfile = findViewById(R.id.athleteProfile)
        athleteUsername = findViewById(R.id.athleteUsername)
        settings = findViewById(R.id.settings)
        messages = findViewById(R.id.messages)
        logWorkout = findViewById(R.id.logWorkout)
        workoutHistory = findViewById(R.id.workoutHistory)
        trainingPlans = findViewById(R.id.trainingPlans)
        competitions = findViewById(R.id.competitions)

        weekDistance = findViewById(R.id.weekDistance)
        weekTime = findViewById(R.id.weekTime)
        weekAvgPace = findViewById(R.id.weekAvgPace)
        weekHeartRate = findViewById(R.id.weekHeartRate)
        lineChartActivity = findViewById(R.id.lineChartActivity)

        // Get user information passed from previous activity
        val name = intent.getStringExtra("USER_NAME") ?: "user profile"
        val username = intent.getStringExtra("USERNAME") ?: ""

        // Display user profile information
        athleteProfile.text = name
        if (username.isNotBlank()) {
            athleteUsername.text = "@$username"
        } else {
            athleteUsername.text = ""
        }

        // Open settings dialog when settings button is clicked
        settings.setOnClickListener {
            showSettingsDialog()
        }

        // Navigate to workout logging screen
        logWorkout.setOnClickListener {
            startActivity(Intent(this, ChooseSport::class.java))
        }

        // Navigate to workout history screen
        workoutHistory.setOnClickListener {
            startActivity(Intent(this, WorkoutHistory::class.java))
        }

        // Load dashboard stats when activity starts
        loadDashboardStats()
    }

    override fun onResume() {
        super.onResume()
        // Refresh dashboard stats whenever user returns to this screen
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        // Retrieve current logged-in user session
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        // Check if user session exists
        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        // API request to fetch dashboard statistics
        RetrofitClient.apiService.getDashboardStats(userId)
            .enqueue(object : Callback<DashboardStatsResponse> {
                override fun onResponse(
                    call: Call<DashboardStatsResponse>,
                    response: Response<DashboardStatsResponse>
                ) {
                    // Check if API request was successful
                    if (response.isSuccessful && response.body()?.success == true) {
                        val stats = response.body()!!.stats

                        // Display weekly distance
                        weekDistance.text = "${String.format(Locale.getDefault(), "%.1f", stats.thisWeekDistance)} km"
                        // Display total weekly workout time
                        weekTime.text = "${stats.thisWeekDuration}m"
                        // Display average pace
                        weekAvgPace.text = String.format(Locale.getDefault(), "%.1f", stats.thisWeekAvgPace)
                        // Display average heart rate
                        weekHeartRate.text = "${stats.thisWeekAvgHeartRate.toInt()} bpm"

                        // Update graph/chart with daily distance data
                        renderLineChart(stats.dailyDistance)
                    } else {
                        // API returned unsuccessful response
                        Toast.makeText(
                            this@AthleteDashboard,
                            "Failed to load dashboard stats",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DashboardStatsResponse>, t: Throwable) {
                    // API request failed due to network issue
                    Toast.makeText(
                        this@AthleteDashboard,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // Sends daily distance data to custom chart
    private fun renderLineChart(items: List<DailyDistanceItem>) {
        lineChartActivity.setData(items)
    }

    private fun showSettingsDialog() {
        // Available settings options
        val options = arrayOf("Log Out")

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    // Logout option
                    0 -> {
                        val sessionManager = SessionManager(this)
                        // Clear stored login session
                        sessionManager.clearSession()

                        // Return user to login screen
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)

                        // CLose dashboard activity
                        finish()
                    }
                }
            }

                // Close dialogue button
            .setNegativeButton("Close", null)
            .show()
    }
}
