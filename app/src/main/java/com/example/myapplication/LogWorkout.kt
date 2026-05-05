package com.example.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class LogWorkout : Activity() {

    private lateinit var backLogWorkout: Button
    private lateinit var selectedSport: TextView
    private lateinit var workoutDate: EditText
    private lateinit var duration: EditText
    private lateinit var distance: EditText
    private lateinit var avgHeartRate: EditText
    private lateinit var notes: EditText
    private lateinit var calculatedPace: TextView
    private lateinit var saveWorkout: Button
    private lateinit var layoutCardioFields: LinearLayout
    private lateinit var layoutGymFields: LinearLayout
    private lateinit var layoutExerciseContainer: LinearLayout
    private lateinit var addExercise: Button
    private var selectedSportName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_workout)

        bindViews()

        selectedSportName = intent.getStringExtra("sport_name") ?: ""
        if (selectedSportName.isBlank()) {
            showToast("No sport selected")
            finish()
            return
        }

        selectedSport.text = getString(R.string.sport_label, selectedSportName)
        updateFormForSport()

        backLogWorkout.setOnClickListener { finish() }
        workoutDate.setOnClickListener { showDatePicker() }
        addExercise.setOnClickListener { addExerciseRow() }
        saveWorkout.setOnClickListener { saveWorkout() }
    }

    private fun bindViews() {
        backLogWorkout = findViewById(R.id.backLogWorkout)
        selectedSport = findViewById(R.id.selectedSport)
        workoutDate = findViewById(R.id.workoutDate)
        duration = findViewById(R.id.duration)
        distance = findViewById(R.id.distance)
        avgHeartRate = findViewById(R.id.avgHeartRate)
        notes = findViewById(R.id.notes)
        calculatedPace = findViewById(R.id.calculatedPace)
        saveWorkout = findViewById(R.id.saveWorkout)
        layoutCardioFields = findViewById(R.id.layoutCardioFields)
        layoutGymFields = findViewById(R.id.layoutGymFields)
        layoutExerciseContainer = findViewById(R.id.layoutExerciseContainer)
        addExercise = findViewById(R.id.addExercise)
    }

    private fun updateFormForSport() {
        if (isGymSport(selectedSportName)) {
            layoutGymFields.visibility = View.VISIBLE
            layoutCardioFields.visibility = View.GONE
            calculatedPace.text = getString(R.string.pace_na)

            if (layoutExerciseContainer.isEmpty()) {
                addExerciseRow()
            }
        } else {
            layoutGymFields.visibility = View.GONE
            layoutCardioFields.visibility = View.VISIBLE
            layoutExerciseContainer.removeAllViews()
            calculatedPace.text = getString(R.string.pace_auto)
        }
    }

    private fun addExerciseRow() {
        val exerciseView = LayoutInflater.from(this).inflate(
            R.layout.exercise_item,
            layoutExerciseContainer,
            false
        )

        val btnRemove = exerciseView.findViewById<Button>(R.id.removeExercise)
        btnRemove.setOnClickListener { removeExerciseRow(exerciseView) }

        layoutExerciseContainer.addView(exerciseView)
    }

    private fun removeExerciseRow(exerciseView: View) {
        layoutExerciseContainer.removeView(exerciseView)

        if (layoutExerciseContainer.isEmpty()) {
            addExerciseRow()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = String.format(
                    Locale.getDefault(),
                    DATE_PICKER_FORMAT,
                    year,
                    month + MONTH_OFFSET,
                    day
                )

                workoutDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    @Suppress("ReturnCount")
    private fun saveWorkout() {
        val userId = SessionManager(this).getUserId()

        if (userId == INVALID_USER_ID) {
            showToast("User session not found")
            return
        }

        val input = WorkoutInput(
            workoutDate = workoutDate.text.toString().trim(),
            durationText = duration.text.toString().trim(),
            distanceText = distance.text.toString().trim(),
            avgHeartRateText = avgHeartRate.text.toString().trim(),
            notes = notes.text.toString().trim()
        )

        val requestResult = createWorkoutRequest(
            userId = userId,
            selectedSportName = selectedSportName,
            input = input,
            layoutExerciseContainer = layoutExerciseContainer
        )

        val message = requestResult.errorMessage

        if (message != null) {
            showToast(message)
            return
        }

        val request = requestResult.request ?: return

        calculatedPace.text = formatCalculatedPace(
            this,
            request.avgPace,
            selectedSportName
        )

        submitWorkout(request)
    }

    private fun submitWorkout(request: LogWorkoutRequest) {
        RetrofitClient.apiService.createWorkout(request)
            .enqueue(object : Callback<BasicApiResponse> {

                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful &&
                        response.body()?.success == true
                    ) {
                        showToast("Workout saved")
                        finish()
                    } else {
                        showToast(
                            response.body()?.message
                                ?: "Failed to save workout"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<BasicApiResponse>,
                    t: Throwable
                ) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val INVALID_USER_ID = -1
        private const val MONTH_OFFSET = 1
        private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"
    }
}
