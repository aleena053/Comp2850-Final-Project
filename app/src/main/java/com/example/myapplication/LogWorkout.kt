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

/**
 * LogWorkout is the form screen where an athlete records a new workout.
 *
 * It receives the chosen sport name from [ChooseSport] via an Intent extra and
 * uses it to show the correct set of fields:
 *  - Cardio sports (Running, Cycling, Swimming): date, duration, distance,
 *    heart rate, notes. Pace is calculated automatically on save.
 *  - Gym: date, duration, heart rate, notes, plus a dynamic list of
 *    exercise rows (name / sets / reps / weight).
 *
 * All validation and request-building is delegated to the free functions in
 * WorkoutUtils.kt so this class stays focused on UI.
 */

class LogWorkout : Activity() {

    // UI references
    private lateinit var backLogWorkout: Button           // back arrow
    private lateinit var selectedSport: TextView          // header showing chosen sport
    private lateinit var workoutDate: EditText            // tapping opens a DatePickerDialog
    private lateinit var duration: EditText               // workout duration in minutes
    private lateinit var distance: EditText               // distance in km (cardio only)
    private lateinit var avgHeartRate: EditText           // optional average BPM
    private lateinit var notes: EditText                  // free-text notes
    private lateinit var calculatedPace: TextView         // shows computed min/km after save
    private lateinit var saveWorkout: Button              // validates and submits the form
    private lateinit var layoutCardioFields: LinearLayout // container for distance field (cardio)
    private lateinit var layoutGymFields: LinearLayout    // container for exercise list (gym)
    private lateinit var layoutExerciseContainer: LinearLayout // holds dynamic exercise rows
    private lateinit var addExercise: Button              // appends a new blank exercise row

    // Sport name received from ChooseSport via Intent extra
    private var selectedSportName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_workout)

        bindViews()

        // Read the sport chosen on the previous screen
        selectedSportName = intent.getStringExtra("sport_name") ?: ""
        if (selectedSportName.isBlank()) {
            showToast("No sport selected")
            finish()
            return
        }

        // Display the sport in the header and configure field visibility
        selectedSport.text = getString(R.string.sport_label, selectedSportName)
        updateFormForSport()

        // Click listeners
        backLogWorkout.setOnClickListener { finish() }
        workoutDate.setOnClickListener { showDatePicker() }
        addExercise.setOnClickListener { addExerciseRow() }
        saveWorkout.setOnClickListener { saveWorkout() }
    }

    /** Finds and stores all view references from the inflated layout. */
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

    /**
     * Shows or hides the correct field groups based on whether this is a gym
     * or cardio workout.
     *
     * - Gym: hides distance/pace fields, shows exercise list, adds a blank
     *   exercise row if the container is currently empty.
     * - Cardio: hides the exercise list, clears any existing rows, and resets
     *   the pace label to its placeholder text.
     */
    private fun updateFormForSport() {
        if (isGymSport(selectedSportName)) {
            layoutGymFields.visibility = View.VISIBLE
            layoutCardioFields.visibility = View.GONE
            calculatedPace.text = getString(R.string.pace_na)

            // Always start with at least one exercise row for gym workouts
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

    /**
     * Inflates a single exercise_item.xml row and appends it to the exercise
     * container. Each row has its own "remove" button wired up here.
     *
     * If removing a row would leave the container empty, a fresh blank row is
     * added automatically so the user always has at least one.
     */
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

    /** Removes [exerciseView] from the container and ensures at least one row remains. */
    private fun removeExerciseRow(exerciseView: View) {
        layoutExerciseContainer.removeView(exerciseView)

        if (layoutExerciseContainer.isEmpty()) {
            addExerciseRow()
        }
    }

    /**
     * Opens an Android [DatePickerDialog] pre-set to today's date. On
     * confirmation it writes the chosen date into [workoutDate] as "YYYY-MM-DD".
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                // month is 0-based in Java Calendar, so add 1 for display
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

    /**
     * Reads the form, validates every field via [createWorkoutRequest], and —
     * if validation passes — posts the workout to the API.
     *
     * Validation failures surface as Toast messages; the function returns
     * early rather than submitting.
     *
     * On success, the calculated pace is shown and the screen is closed,
     * which triggers onResume on the dashboard to refresh stats.
     */
    @Suppress("ReturnCount")
    private fun saveWorkout() {
        val userId = SessionManager(this).getUserId()

        if (userId == INVALID_USER_ID) {
            showToast("User session not found")
            return
        }

        // Bundle raw text values for the shared validation helper
        val input = WorkoutInput(
            workoutDate = workoutDate.text.toString().trim(),
            durationText = duration.text.toString().trim(),
            distanceText = distance.text.toString().trim(),
            avgHeartRateText = avgHeartRate.text.toString().trim(),
            notes = notes.text.toString().trim()
        )

        // Delegate all validation + request construction to WorkoutUtils
        val requestResult = createWorkoutRequest(
            userId = userId,
            selectedSportName = selectedSportName,
            input = input,
            layoutExerciseContainer = layoutExerciseContainer
        )

        val message = requestResult.errorMessage

        // If validation failed, show the error and stop
        if (message != null) {
            showToast(message)
            return
        }

        val request = requestResult.request ?: return

        // Show the pace immediately in the UI before the network call completes
        calculatedPace.text = formatCalculatedPace(
            this,
            request.avgPace,
            selectedSportName
        )

        submitWorkout(request)
    }

    /**
     * Posts [request] to the API via Retrofit.
     * On success, shows a confirmation Toast and closes the screen.
     * On failure, shows the server's error message or a generic fallback.
     */
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
                        finish() // return to WorkoutHistory or Dashboard
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

    /** Convenience wrapper so every call site doesn't need the context argument. */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val INVALID_USER_ID = -1
        private const val MONTH_OFFSET = 1
        private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"
    }
}
