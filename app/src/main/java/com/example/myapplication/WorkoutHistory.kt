package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * WorkoutHistory displays a scrollable, searchable list of all workouts the
 * athlete has ever logged.
 *
 * On load (and every time the screen resumes) it fetches the full workout list
 * from the API and hands it to the [Workout] RecyclerView adapter. As the user
 * types in the search box, the adapter filters the visible rows in real time
 * without making additional network requests.
 *
 * Tapping a row navigates to [WorkoutDetail], passing the workout's ID so that
 * screen can load, edit, or delete the specific entry.
 */

class WorkoutHistory : Activity() {

    // UI references
    private lateinit var backWorkoutHistory: Button       // returns to AthleteDashboard
    private lateinit var searchWorkout: EditText          // live-filter input field
    private lateinit var recyclerWorkoutHistory: RecyclerView // the scrollable list
    private lateinit var workoutAdapter: Workout          // adapter that owns the list data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_history)

        // Wire up view references
        backWorkoutHistory = findViewById(R.id.backWorkoutHistory)
        searchWorkout = findViewById(R.id.searchWorkout)
        recyclerWorkoutHistory = findViewById(R.id.recyclerWorkoutHistory)

        // Create the adapter with an empty list; data is loaded below.
        // The lambda is the row-tap callback: it opens WorkoutDetail for the tapped workout.
        workoutAdapter = Workout(mutableListOf()) { workout ->
            val intent = Intent(this, WorkoutDetail::class.java)
            intent.putExtra("WORKOUT_ID", workout.workoutId) // WorkoutDetail reads this extra
            startActivity(intent)
        }

        // Attach a vertical LinearLayoutManager and the adapter to the RecyclerView
        recyclerWorkoutHistory.layoutManager = LinearLayoutManager(this)
        recyclerWorkoutHistory.adapter = workoutAdapter

        // Back arrow: close this screen and return to the dashboard
        backWorkoutHistory.setOnClickListener {
            finish()
        }

        // Live search: whenever the text changes, ask the adapter to filter
        // its visible list. No network call is made — filtering is done in
        // memory against the full dataset the adapter already holds.
        searchWorkout.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                workoutAdapter.filter(s.toString())
            }

            // These two callbacks are required by the interface but unused here
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit
        })

        // Perform the initial data load when the screen is first created
        loadWorkouts()
    }

    /**
     * Called every time the screen returns to the foreground (e.g. after the
     * user edits or deletes a workout in [WorkoutDetail] and presses back).
     * Re-fetches the list so any changes made in WorkoutDetail are reflected.
     */
    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    /**
     * Fetches all workouts for the current user from the API and updates the adapter.
     *
     * Uses [SessionManager] to retrieve the stored userId. If no valid session
     * exists, shows an error Toast and returns early without making a network call.
     *
     * On a successful response the adapter's full dataset is replaced via
     * [Workout.updateData], which also resets any active search filter.
     * An empty list results in an informational Toast.
     */
    private fun loadWorkouts() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        // Guard: a userId of -1 means no session is stored
        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.getWorkouts(userId).enqueue(object : Callback<WorkoutsResponse> {
            override fun onResponse(call: Call<WorkoutsResponse>, response: Response<WorkoutsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val workouts = response.body()?.workouts ?: emptyList()

                    // Push the fresh dataset into the adapter; this clears
                    // any search filter and notifies RecyclerView to redraw
                    workoutAdapter.updateData(workouts)

                    if (workouts.isEmpty()) {
                        Toast.makeText(this@WorkoutHistory, "No workouts logged yet", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@WorkoutHistory, "Failed to load workouts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WorkoutsResponse>, t: Throwable) {
                Toast.makeText(this@WorkoutHistory, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
