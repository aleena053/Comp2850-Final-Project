package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientWorkoutHistory : Activity() {

    private lateinit var backClientWorkoutHistory: Button
    private lateinit var viewTrainingPlans: Button
    private lateinit var clientWorkoutHistoryTitle: TextView
    private lateinit var searchClientWorkout: EditText
    private lateinit var recyclerClientWorkoutHistory: RecyclerView
    private lateinit var workoutAdapter: Workout

    private var clientId: Int = INVALID_CLIENT_ID
    private var clientName: String = DEFAULT_CLIENT_NAME

    //prepares the screen, reads the selected client information,
    // sets up the workout list, and loads the client’s workout history.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.client_workout_history)

        bindViews()
        readIntentData()
        setupTitle()
        setupRecyclerView()
        setupListeners()
        loadClientWorkouts()
    }

    override fun onResume() {
        super.onResume()
        loadClientWorkouts()
    }

    private fun bindViews() {
        backClientWorkoutHistory = findViewById(R.id.backClientWorkoutHistory)
        viewTrainingPlans = findViewById(R.id.viewTrainingPlans)
        clientWorkoutHistoryTitle = findViewById(R.id.clientWorkoutHistoryTitle)
        searchClientWorkout = findViewById(R.id.searchClientWorkout)
        recyclerClientWorkoutHistory = findViewById(R.id.recyclerClientWorkoutHistory)
    }

    //gets the client ID and name from the previous screen
    private fun readIntentData() {
        clientId = intent.getIntExtra(CLIENT_ID_EXTRA, INVALID_CLIENT_ID)
        clientName = intent.getStringExtra(CLIENT_NAME_EXTRA) ?: DEFAULT_CLIENT_NAME
    }

    private fun setupTitle() {
        clientWorkoutHistoryTitle.text = getString(
            R.string.client_workout_history_title,
            clientName
        )
    }

    //displays many workout records in a scrollable list.
    private fun setupRecyclerView() {
        //empty first
        workoutAdapter = Workout(mutableListOf()) {
            Toast.makeText(this, TRAINER_VIEW_ONLY_MESSAGE, Toast.LENGTH_SHORT).show()
        }

        recyclerClientWorkoutHistory.layoutManager = LinearLayoutManager(this)
        recyclerClientWorkoutHistory.adapter = workoutAdapter
    }

    private fun setupListeners() {
        //Close current, go back previous
        backClientWorkoutHistory.setOnClickListener {
            finish()
        }
        //opens the selected client’s training plans page
        viewTrainingPlans.setOnClickListener {
            openClientTrainingPlans()
        }

        searchClientWorkout.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //the workout list will be filtered based on the search text
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
    }

    private fun openClientTrainingPlans() {
        val intent = Intent(this, ClientTrainingPlans::class.java).apply {
            putExtra(CLIENT_ID_EXTRA, clientId)
            putExtra(CLIENT_NAME_EXTRA, clientName)
        }
        startActivity(intent)
    }

    private fun loadClientWorkouts() {
        if (clientId == INVALID_CLIENT_ID) {
            showToast(INVALID_CLIENT_MESSAGE)
            finish()
            return
        }

        //Workout list of client
        RetrofitClient.apiService.getWorkouts(clientId)
            .enqueue(object : Callback<WorkoutsResponse> {
                override fun onResponse(
                    call: Call<WorkoutsResponse>,
                    response: Response<WorkoutsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val workouts = response.body()?.workouts ?: emptyList()
                        workoutAdapter.updateData(workouts)

                        if (workouts.isEmpty()) {
                            showToast(NO_WORKOUTS_FOUND_MESSAGE)
                        }
                    } else {
                        showToast(LOAD_WORKOUTS_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<WorkoutsResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val INVALID_CLIENT_ID = -1
        private const val DEFAULT_CLIENT_NAME = "Client"
        private const val CLIENT_ID_EXTRA = "CLIENT_ID"
        private const val CLIENT_NAME_EXTRA = "CLIENT_NAME"
        private const val TRAINER_VIEW_ONLY_MESSAGE = "Trainer view only"
        private const val INVALID_CLIENT_MESSAGE = "Invalid client"
        private const val NO_WORKOUTS_FOUND_MESSAGE = "No workouts found for this client"
        private const val LOAD_WORKOUTS_FAILED_MESSAGE = "Failed to load client workouts"
    }
}
