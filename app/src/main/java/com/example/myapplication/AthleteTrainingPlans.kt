package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AthleteTrainingPlans : Activity() {

    private lateinit var backAthletePlans: Button
    private lateinit var createTrainingPlan: Button
    private lateinit var recyclerTrainingPlans: RecyclerView
    private lateinit var trainingPlanAdapter: TrainingPlan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.athlete_training_plans)
        //connects buttons and RV to from XML to Kt
        backAthletePlans = findViewById(R.id.backAthletePlans)
        createTrainingPlan = findViewById(R.id.createTrainingPlan)
        recyclerTrainingPlans = findViewById(R.id.recyclerTrainingPlans)
        //handles the display of each training plan and also manages the delete button action for each item
        trainingPlanAdapter = TrainingPlan(
            mutableListOf(),
            onDeleteClick = { plan ->
                showDeletePlanDialog(plan)
            }
        )
        //display all training plans dynamically
        recyclerTrainingPlans.layoutManager = LinearLayoutManager(this)
        recyclerTrainingPlans.adapter = trainingPlanAdapter

        backAthletePlans.setOnClickListener {
            finish()
        }
        //opens CreatetrainingPlan page when clicked
        createTrainingPlan.setOnClickListener {
            startActivity(Intent(this, CreateTrainingPlan::class.java))
        }

        loadTrainingPlans()
    }
    //makes sure training plan list is refreshed when user returns from another page
    override fun onResume() {
        super.onResume()
        loadTrainingPlans()
    }

    private fun loadTrainingPlans() {
        //gets the current logged-in user’s ID from the session
        val userId = SessionManager(this).getUserId()
        //checks whether user session exists
        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }
        //communicate with the backend server and retrieve training plan data
        //".enqueue" runs asynchronously (app does not freeze)
        RetrofitClient.apiService.getTrainingPlans(userId)
            .enqueue(object : Callback<TrainingPlansResponse> {
                override fun onResponse(
                    call: Call<TrainingPlansResponse>,
                    response: Response<TrainingPlansResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val plans = response.body()?.plans ?: emptyList()
                        trainingPlanAdapter.updateData(plans)
                    } else {
                        Toast.makeText(
                            this@AthleteTrainingPlans,
                            "Failed to load training plans",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                //This runs when there is a network problem
                override fun onFailure(call: Call<TrainingPlansResponse>, t: Throwable) {
                    Toast.makeText(
                        this@AthleteTrainingPlans,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    //popup message before deleting a training plan
    private fun showDeletePlanDialog(plan: TrainingPlanItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Training Plan")
            .setMessage("Delete '${plan.planName}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTrainingPlan(plan.planId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteTrainingPlan(planId: Int) {
        RetrofitClient.apiService.deleteTrainingPlan(planId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@AthleteTrainingPlans,
                            "Training plan deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadTrainingPlans()
                    } else {
                        val message = response.body()?.message ?: "Failed to delete training plan"
                        Toast.makeText(
                            this@AthleteTrainingPlans,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@AthleteTrainingPlans,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
