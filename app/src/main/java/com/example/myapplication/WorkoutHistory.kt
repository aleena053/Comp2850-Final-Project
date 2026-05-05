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

class WorkoutHistory : Activity() {

    private lateinit var backWorkoutHistory: Button
    private lateinit var searchWorkout: EditText
    private lateinit var recyclerWorkoutHistory: RecyclerView
    private lateinit var workoutAdapter: Workout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_history)

        backWorkoutHistory = findViewById(R.id.backWorkoutHistory)
        searchWorkout = findViewById(R.id.searchWorkout)
        recyclerWorkoutHistory = findViewById(R.id.recyclerWorkoutHistory)

        workoutAdapter = Workout(mutableListOf()) { workout ->
            val intent = Intent(this, WorkoutDetail::class.java)
            intent.putExtra("WORKOUT_ID", workout.workoutId)
            startActivity(intent)
        }

        recyclerWorkoutHistory.layoutManager = LinearLayoutManager(this)
        recyclerWorkoutHistory.adapter = workoutAdapter

        backWorkoutHistory.setOnClickListener {
            finish()
        }

        searchWorkout.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                workoutAdapter.filter(s.toString())
            }

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

        loadWorkouts()
    }

    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    private fun loadWorkouts() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.getWorkouts(userId).enqueue(object : Callback<WorkoutsResponse> {
            override fun onResponse(call: Call<WorkoutsResponse>, response: Response<WorkoutsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val workouts = response.body()?.workouts ?: emptyList()
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
