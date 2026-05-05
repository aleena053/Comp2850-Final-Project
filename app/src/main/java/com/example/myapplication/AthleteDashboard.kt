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

    private lateinit var athleteProfile: TextView
    private lateinit var athleteUsername: TextView
    private lateinit var settings: ImageButton
    private lateinit var messages: Button
    private lateinit var logWorkout: LinearLayout
    private lateinit var workoutHistory: LinearLayout
    private lateinit var trainingPlans: LinearLayout
    private lateinit var competitions: Button
    private lateinit var weekDistance: TextView
    private lateinit var weekTime: TextView
    private lateinit var weekAvgPace: TextView
    private lateinit var weekHeartRate: TextView
    private lateinit var lineChartActivity: WeeklyDistanceChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.athlete_dashboard)

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

        val name = intent.getStringExtra("USER_NAME") ?: "user profile"
        val username = intent.getStringExtra("USERNAME") ?: ""

        athleteProfile.text = name
        if (username.isNotBlank()) {
            athleteUsername.text = "@$username"
        } else {
            athleteUsername.text = ""
        }

        settings.setOnClickListener {
            showSettingsDialog()
        }

        logWorkout.setOnClickListener {
            startActivity(Intent(this, ChooseSport::class.java))
        }

        workoutHistory.setOnClickListener {
            startActivity(Intent(this, WorkoutHistory::class.java))
        }

        loadDashboardStats()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.getDashboardStats(userId)
            .enqueue(object : Callback<DashboardStatsResponse> {
                override fun onResponse(
                    call: Call<DashboardStatsResponse>,
                    response: Response<DashboardStatsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val stats = response.body()!!.stats

                        weekDistance.text = "${String.format(Locale.getDefault(), "%.1f", stats.thisWeekDistance)} km"
                        weekTime.text = "${stats.thisWeekDuration}m"
                        weekAvgPace.text = String.format(Locale.getDefault(), "%.1f", stats.thisWeekAvgPace)
                        weekHeartRate.text = "${stats.thisWeekAvgHeartRate.toInt()} bpm"

                        renderLineChart(stats.dailyDistance)
                    } else {
                        Toast.makeText(
                            this@AthleteDashboard,
                            "Failed to load dashboard stats",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DashboardStatsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@AthleteDashboard,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun renderLineChart(items: List<DailyDistanceItem>) {
        lineChartActivity.setData(items)
    }

    private fun showSettingsDialog() {
        val options = arrayOf("Log Out")

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val sessionManager = SessionManager(this)
                        sessionManager.clearSession()

                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
