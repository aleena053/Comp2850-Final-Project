package com.example.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class CreateTrainingPlan : Activity() {

    private var clientId: Int = -1

    private lateinit var backTrainingPlan: Button
    private lateinit var planName: EditText
    private lateinit var planDescription: EditText
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var saveTrainingPlan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_training_plan)

        clientId = intent.getIntExtra("CLIENT_ID", -1)

        backTrainingPlan = findViewById(R.id.backTrainingPlan)
        planName = findViewById(R.id.planName)
        planDescription = findViewById(R.id.planDescription)
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        saveTrainingPlan = findViewById(R.id.saveTrainingPlan)

        backTrainingPlan.setOnClickListener {
            finish()
        }

        startDate.setOnClickListener {
            showDatePicker(startDate)
        }

        endDate.setOnClickListener {
            showDatePicker(endDate)
        }

        saveTrainingPlan.setOnClickListener {
            saveTrainingPlan()
        }
    }
    private fun showDatePicker(targetField: EditText) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year, month + 1, day
                )
                targetField.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun saveTrainingPlan() {
        val planName = planName.text.toString().trim()
        val description = planDescription.text.toString().trim()
        val startDate = startDate.text.toString().trim()
        val endDate = endDate.text.toString().trim()

        if (planName.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = SessionManager(this).getUserId()

        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        val targetUserId = if (clientId != -1) clientId else currentUserId

        val request = CreateTrainingPlanRequest(
            userId = targetUserId,
            createdByUserId = currentUserId,
            planName = planName,
            description = description,
            startDate = startDate,
            endDate = endDate
        )

        RetrofitClient.apiService.createTrainingPlan(request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val message = if (clientId != -1) {
                            "Training plan created for client"
                        } else {
                            "Training plan created"
                        }

                        Toast.makeText(
                            this@CreateTrainingPlan,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@CreateTrainingPlan,
                            "Failed to create training plan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@CreateTrainingPlan,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
