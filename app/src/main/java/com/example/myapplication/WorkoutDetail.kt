package com.example.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Suppress("TooManyFunctions")
class WorkoutDetail : Activity() {

    private var workoutId: Int = INVALID_WORKOUT_ID
    private lateinit var backWorkoutDetail: Button
    private lateinit var sportDetail: Spinner
    private lateinit var workoutDateDetail: EditText
    private lateinit var durationDetail: EditText
    private lateinit var distanceDetail: EditText
    private lateinit var avgHeartRateDetail: EditText
    private lateinit var notesDetail: EditText
    private lateinit var calculatedPaceDetail: TextView
    private lateinit var updateWorkout: Button
    private lateinit var deleteWorkout: Button

    private lateinit var layoutCardioFieldsDetail: LinearLayout
    private lateinit var layoutGymFieldsDetail: LinearLayout
    private lateinit var layoutExerciseContainerDetail: LinearLayout
    private lateinit var addExerciseDetail: Button

    private val sports = listOf(
        RUNNING_SPORT,
        CYCLING_SPORT,
        SWIMMING_SPORT,
        GYM_SPORT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_detail)

        bindViews()
        workoutId = intent.getIntExtra(WORKOUT_ID_EXTRA, INVALID_WORKOUT_ID)

        setupSportSpinner()
        setupListeners()
        loadWorkoutDetail()
    }

    private fun bindViews() {
        backWorkoutDetail = findViewById(R.id.backWorkoutDetail)
        sportDetail = findViewById(R.id.sportDetail)
        workoutDateDetail = findViewById(R.id.workoutDateDetail)
        durationDetail = findViewById(R.id.durationDetail)
        distanceDetail = findViewById(R.id.distanceDetail)
        avgHeartRateDetail = findViewById(R.id.avgHeartRateDetail)
        notesDetail = findViewById(R.id.notesDetail)
        calculatedPaceDetail = findViewById(R.id.calculatedPaceDetail)
        updateWorkout = findViewById(R.id.updateWorkout)
        deleteWorkout = findViewById(R.id.deleteWorkout)

        layoutCardioFieldsDetail = findViewById(R.id.layoutCardioFieldsDetail)
        layoutGymFieldsDetail = findViewById(R.id.layoutGymFieldsDetail)
        layoutExerciseContainerDetail = findViewById(R.id.layoutExerciseContainerDetail)
        addExerciseDetail = findViewById(R.id.addExerciseDetail)
    }

    private fun setupListeners() {
        backWorkoutDetail.setOnClickListener { finish() }

        workoutDateDetail.setOnClickListener {
            showDatePicker(workoutDateDetail)
        }

        addExerciseDetail.setOnClickListener {
            addExerciseRow()
        }

        updateWorkout.setOnClickListener {
            updateWorkout()
        }

        deleteWorkout.setOnClickListener {
            deleteWorkout()
        }

        sportDetail.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateFormForSport(sportDetail.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupSportSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sports
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sportDetail.adapter = adapter
    }

    private fun updateFormForSport(sportName: String) {
        if (isGymSport(sportName)) {
            showGymForm()
        } else {
            showCardioForm()
        }
    }

    private fun showGymForm() {
        layoutGymFieldsDetail.visibility = View.VISIBLE
        layoutCardioFieldsDetail.visibility = View.GONE
        calculatedPaceDetail.text = NOT_AVAILABLE_TEXT

        if (layoutExerciseContainerDetail.isEmpty()) {
            addExerciseRow()
        }
    }

    private fun showCardioForm() {
        layoutGymFieldsDetail.visibility = View.GONE
        layoutCardioFieldsDetail.visibility = View.VISIBLE
        layoutExerciseContainerDetail.removeAllViews()

        val duration = durationDetail.text.toString().trim().toIntOrNull()
        val distance = distanceDetail.text.toString().trim().toDoubleOrNull()
        val pace = duration?.let { calculatePace(it, distance) }

        calculatedPaceDetail.text = if (pace != null) {
            String.format(Locale.getDefault(), PACE_FORMAT, pace)
        } else {
            AUTO_PACE_TEXT
        }
    }

    private fun addExerciseRow(
        exerciseName: String = "",
        sets: String = "",
        reps: String = "",
        weight: String = ""
    ) {
        val exerciseView = LayoutInflater.from(this).inflate(
            R.layout.exercise_item,
            layoutExerciseContainerDetail,
            false
        )

        val etExerciseName = exerciseView.findViewById<EditText>(R.id.exerciseNameItem)
        val etSets = exerciseView.findViewById<EditText>(R.id.setsItem)
        val etReps = exerciseView.findViewById<EditText>(R.id.repsItem)
        val etWeight = exerciseView.findViewById<EditText>(R.id.weightKgItem)
        val btnRemove = exerciseView.findViewById<Button>(R.id.removeExercise)

        etExerciseName.setText(exerciseName)
        etSets.setText(sets)
        etReps.setText(reps)
        etWeight.setText(weight)

        btnRemove.setOnClickListener {
            removeExerciseRow(exerciseView)
        }

        layoutExerciseContainerDetail.addView(exerciseView)
    }

    private fun removeExerciseRow(exerciseView: View) {
        layoutExerciseContainerDetail.removeView(exerciseView)
        if (layoutExerciseContainerDetail.isEmpty()) {
            addExerciseRow()
        }
    }

    private fun showDatePicker(targetField: EditText) {
        val calendar = getCalendarFromField(targetField.text.toString().trim())

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
                targetField.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getCalendarFromField(currentText: String): Calendar {
        val calendar = Calendar.getInstance()

        if (currentText.matches(DATE_REGEX.toRegex())) {
            try {
                val parts = currentText.split(DATE_SEPARATOR)
                calendar.set(
                    parts[YEAR_INDEX].toInt(),
                    parts[MONTH_INDEX].toInt() - MONTH_OFFSET,
                    parts[DAY_INDEX].toInt()
                )
            } catch (_: Exception) {
            }
        }

        return calendar
    }

    private fun loadWorkoutDetail() {
        if (workoutId == INVALID_WORKOUT_ID) {
            showToast(INVALID_WORKOUT_MESSAGE)
            finish()
            return
        }

        RetrofitClient.apiService.getWorkoutDetail(workoutId)
            .enqueue(object : Callback<WorkoutDetailResponse> {
                override fun onResponse(
                    call: Call<WorkoutDetailResponse>,
                    response: Response<WorkoutDetailResponse>
                ) {
                    val workout = response.body()?.workout

                    if (response.isSuccessful && response.body()?.success == true && workout != null) {
                        bindWorkoutToScreen(workout)
                    } else {
                        showToast(LOAD_WORKOUT_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<WorkoutDetailResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun bindWorkoutToScreen(workout: WorkoutItem) {
        setSelectedSport(workout.sportName)
        workoutDateDetail.setText(formatWorkoutDate(workout.workoutDate))
        durationDetail.setText(workout.duration.toString())
        distanceDetail.setText(workout.distanceKm?.toString() ?: "")
        avgHeartRateDetail.setText(workout.avgHeartRate?.toString() ?: "")
        notesDetail.setText(workout.notes ?: "")

        if (isGymSport(workout.sportName)) {
            bindGymWorkout(workout)
        } else {
            bindCardioWorkout(workout)
        }

        updateFormForSport(workout.sportName)
    }

    private fun setSelectedSport(sportName: String) {
        val sportIndex = sports.indexOfFirst {
            it.equals(sportName, ignoreCase = true)
        }
        if (sportIndex >= ZERO_INT) {
            sportDetail.setSelection(sportIndex)
        }
    }

    private fun bindGymWorkout(workout: WorkoutItem) {
        layoutExerciseContainerDetail.removeAllViews()

        val exerciseList = workout.exercises ?: emptyList()
        if (exerciseList.isEmpty()) {
            addExerciseRow()
        } else {
            exerciseList.forEach { exercise ->
                addExerciseRow(
                    exerciseName = exercise.exerciseName,
                    sets = exercise.setsCount.toString(),
                    reps = exercise.repsCount.toString(),
                    weight = exercise.weightKg.toString()
                )
            }
        }

        calculatedPaceDetail.text = NOT_AVAILABLE_TEXT
    }

    private fun bindCardioWorkout(workout: WorkoutItem) {
        val pace = calculatePace(workout.duration, workout.distanceKm)
        calculatedPaceDetail.text = if (pace != null) {
            String.format(Locale.getDefault(), PACE_FORMAT, pace)
        } else {
            NOT_AVAILABLE_TEXT
        }
    }

    private fun updateWorkout() {
        val requestResult = buildUpdateWorkoutRequest()
        val errorMessage = requestResult.errorMessage

        if (errorMessage != null) {
            showToast(errorMessage)
            return
        }

        val request = requestResult.request ?: return
        calculatedPaceDetail.text = requestResult.paceText
        submitWorkoutUpdate(request)
    }

    private fun buildUpdateWorkoutRequest(): UpdateWorkoutRequestResult {
        val formValues = readWorkoutFormValues()
        val formError = validateRequiredWorkoutFields(formValues)

        if (formError != null) {
            return UpdateWorkoutRequestResult(errorMessage = formError)
        }

        val durationResult = getPositiveDuration(formValues.durationText)
        val distanceResult = getWorkoutDistance(formValues.sport, formValues.distanceText)
        val heartRateResult = getWorkoutHeartRate(formValues.avgHeartRateText)
        val exerciseResult = buildWorkoutExercises(formValues.sport)

        val firstError = listOfNotNull(
            durationResult.errorMessage,
            distanceResult.errorMessage,
            heartRateResult.errorMessage,
            exerciseResult.errorMessage
        ).firstOrNull()

        return if (firstError != null) {
            UpdateWorkoutRequestResult(errorMessage = firstError)
        } else {
            createWorkoutUpdateResult(
                formValues = formValues,
                duration = durationResult.value ?: ZERO_INT,
                distance = distanceResult.value,
                heartRate = heartRateResult.value,
                exercises = exerciseResult.value
            )
        }
    }

    private fun readWorkoutFormValues(): WorkoutFormValues {
        return WorkoutFormValues(
            sport = sportDetail.selectedItem.toString(),
            workoutDate = workoutDateDetail.text.toString().trim(),
            durationText = durationDetail.text.toString().trim(),
            distanceText = distanceDetail.text.toString().trim(),
            avgHeartRateText = avgHeartRateDetail.text.toString().trim(),
            notes = notesDetail.text.toString().trim()
        )
    }

    private fun validateRequiredWorkoutFields(formValues: WorkoutFormValues): String? {
        val missingRequiredFields = formValues.workoutDate.isBlank() ||
                formValues.durationText.isBlank()

        return if (missingRequiredFields) {
            MISSING_REQUIRED_FIELDS_MESSAGE
        } else {
            null
        }
    }

    private fun createWorkoutUpdateResult(
        formValues: WorkoutFormValues,
        duration: Int,
        distance: Double?,
        heartRate: Int?,
        exercises: List<ExerciseEntry>?
    ): UpdateWorkoutRequestResult {
        val avgPace = if (isGymSport(formValues.sport)) {
            null
        } else {
            calculatePace(duration, distance)
        }

        val paceText = if (isGymSport(formValues.sport)) {
            NOT_AVAILABLE_TEXT
        } else {
            formatPaceText(avgPace)
        }

        val request = UpdateWorkoutRequest(
            sport = formValues.sport,
            workoutDate = formValues.workoutDate,
            duration = duration,
            distanceKm = distance,
            avgPace = avgPace,
            avgHeartRate = heartRate,
            notes = formValues.notes.ifBlank { null },
            exercises = exercises
        )

        return UpdateWorkoutRequestResult(
            request = request,
            paceText = paceText
        )
    }

    private fun getPositiveDuration(durationText: String): ValidationResult<Int> {
        val duration = durationText.toIntOrNull()
        return if (duration != null && duration > ZERO_INT) {
            ValidationResult(value = duration)
        } else {
            ValidationResult(errorMessage = INVALID_DURATION_MESSAGE)
        }
    }

    private fun getWorkoutDistance(
        sport: String,
        distanceText: String
    ): ValidationResult<Double?> {
        return when {
            isGymSport(sport) || distanceText.isBlank() -> {
                ValidationResult(value = null)
            }
            distanceText.toDoubleOrNull()?.let { it > ZERO_DOUBLE } == true -> {
                ValidationResult(value = distanceText.toDouble())
            }
            else -> {
                ValidationResult(errorMessage = INVALID_DISTANCE_MESSAGE)
            }
        }
    }

    private fun getWorkoutHeartRate(avgHeartRateText: String): ValidationResult<Int?> {
        return when {
            avgHeartRateText.isBlank() -> {
                ValidationResult(value = null)
            }
            avgHeartRateText.toIntOrNull()?.let { it > ZERO_INT } == true -> {
                ValidationResult(value = avgHeartRateText.toInt())
            }
            else -> {
                ValidationResult(errorMessage = INVALID_HEART_RATE_MESSAGE)
            }
        }
    }

    private fun buildWorkoutExercises(sport: String): ValidationResult<List<ExerciseEntry>?> {
        return if (!isGymSport(sport)) {
            ValidationResult(value = null)
        } else {
            collectGymExercises()
        }
    }

    private fun collectGymExercises(): ValidationResult<List<ExerciseEntry>?> {
        val exerciseList = mutableListOf<ExerciseEntry>()
        var errorMessage: String? = null

        for (i in ZERO_INT until layoutExerciseContainerDetail.childCount) {
            val row = layoutExerciseContainerDetail.getChildAt(i)
            val rowResult = getWorkoutExerciseRow(row)

            if (rowResult.errorMessage != null) {
                errorMessage = rowResult.errorMessage
                break
            }

            rowResult.value?.let { exerciseList.add(it) }
        }

        return when {
            errorMessage != null -> {
                ValidationResult(errorMessage = errorMessage)
            }
            exerciseList.isEmpty() -> {
                ValidationResult(errorMessage = ADD_EXERCISE_MESSAGE)
            }
            else -> {
                ValidationResult(value = exerciseList)
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun getWorkoutExerciseRow(row: View): ValidationResult<ExerciseEntry> {
        val etExerciseName = row.findViewById<EditText>(R.id.exerciseNameItem)
        val etSets = row.findViewById<EditText>(R.id.setsItem)
        val etReps = row.findViewById<EditText>(R.id.repsItem)
        val etWeight = row.findViewById<EditText>(R.id.weightKgItem)

        val exerciseName = etExerciseName.text.toString().trim()
        val sets = etSets.text.toString().trim().toIntOrNull()
        val reps = etReps.text.toString().trim().toIntOrNull()
        val weight = etWeight.text.toString().trim().toDoubleOrNull()

        val hasMissingFields = exerciseName.isBlank() ||
                sets == null ||
                reps == null ||
                weight == null

        val hasInvalidNumbers = sets != null &&
                reps != null &&
                weight != null &&
                (sets <= ZERO_INT || reps <= ZERO_INT || weight <= ZERO_DOUBLE)

        return when {
            hasMissingFields -> {
                ValidationResult(errorMessage = COMPLETE_EXERCISE_FIELDS_MESSAGE)
            }
            hasInvalidNumbers -> {
                ValidationResult(errorMessage = INVALID_EXERCISE_VALUES_MESSAGE)
            }
            else -> {
                ValidationResult(
                    value = ExerciseEntry(
                        exerciseName = exerciseName,
                        setsCount = sets,
                        repsCount = reps,
                        weightKg = weight
                    )
                )
            }
        }
    }

    private fun submitWorkoutUpdate(request: UpdateWorkoutRequest) {
        RetrofitClient.apiService.updateWorkout(workoutId, request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        showToast(WORKOUT_UPDATED_MESSAGE)
                        finish()
                    } else {
                        val message = response.body()?.message ?: UPDATE_WORKOUT_FAILED_MESSAGE
                        showToast(message)
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun deleteWorkout() {
        RetrofitClient.apiService.deleteWorkout(workoutId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        showToast(WORKOUT_DELETED_MESSAGE)
                        finish()
                    } else {
                        showToast(DELETE_WORKOUT_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun calculatePace(durationMinutes: Int, distanceKm: Double?): Double? {
        return if (distanceKm == null || distanceKm <= ZERO_DOUBLE) {
            null
        } else {
            durationMinutes / distanceKm
        }
    }

    private fun formatPaceText(avgPace: Double?): String {
        return if (avgPace != null) {
            String.format(Locale.getDefault(), PACE_FORMAT, avgPace)
        } else {
            NOT_AVAILABLE_TEXT
        }
    }

    private fun formatWorkoutDate(rawDate: String): String {
        val formatters = listOf(
            SimpleDateFormat(GMT_DATE_PATTERN, Locale.ENGLISH),
            SimpleDateFormat(BASIC_DATE_PATTERN, Locale.getDefault()),
            SimpleDateFormat(DATETIME_PATTERN, Locale.getDefault())
        )

        formatters.forEach { formatter ->
            try {
                formatter.timeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
                val parsed = formatter.parse(rawDate)
                if (parsed != null) {
                    return SimpleDateFormat(
                        BASIC_DATE_PATTERN,
                        Locale.getDefault()
                    ).format(parsed)
                }
            } catch (_: Exception) {
            }
        }

        return if (rawDate.length >= DATE_TEXT_LENGTH) {
            rawDate.take(DATE_TEXT_LENGTH)
        } else {
            rawDate
        }
    }

    private fun isGymSport(sportName: String): Boolean {
        return sportName.equals(GYM_SPORT, ignoreCase = true)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val INVALID_WORKOUT_ID = -1
        private const val ZERO_INT = 0
        private const val ZERO_DOUBLE = 0.0

        private const val YEAR_INDEX = 0
        private const val MONTH_INDEX = 1
        private const val DAY_INDEX = 2
        private const val MONTH_OFFSET = 1
        private const val DATE_TEXT_LENGTH = 10

        private const val WORKOUT_ID_EXTRA = "WORKOUT_ID"
        private const val DATE_SEPARATOR = "-"
        private const val DATE_REGEX = """\d{4}-\d{2}-\d{2}"""

        private const val RUNNING_SPORT = "Running"
        private const val CYCLING_SPORT = "Cycling"
        private const val SWIMMING_SPORT = "Swimming"
        private const val GYM_SPORT = "Gym"

        private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"
        private const val PACE_FORMAT = "%.2f min/km"

        private const val GMT_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
        private const val BASIC_DATE_PATTERN = "yyyy-MM-dd"
        private const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
        private const val UTC_TIMEZONE = "UTC"

        private const val NOT_AVAILABLE_TEXT = "N/A"
        private const val AUTO_PACE_TEXT = "Will be calculated automatically"

        private const val INVALID_WORKOUT_MESSAGE = "Invalid workout"
        private const val LOAD_WORKOUT_FAILED_MESSAGE = "Failed to load workout"
        private const val MISSING_REQUIRED_FIELDS_MESSAGE =
            "Please fill in date and duration"
        private const val INVALID_DURATION_MESSAGE =
            "Duration must be a valid number greater than 0"
        private const val INVALID_DISTANCE_MESSAGE =
            "Distance must be a valid number greater than 0"
        private const val INVALID_HEART_RATE_MESSAGE =
            "Heart rate must be a valid number greater than 0"
        private const val COMPLETE_EXERCISE_FIELDS_MESSAGE =
            "Please complete all exercise fields"
        private const val INVALID_EXERCISE_VALUES_MESSAGE =
            "Sets, reps and weight must be greater than 0"
        private const val ADD_EXERCISE_MESSAGE = "Add at least one exercise"
        private const val WORKOUT_UPDATED_MESSAGE = "Workout updated"
        private const val UPDATE_WORKOUT_FAILED_MESSAGE = "Failed to update workout"
        private const val WORKOUT_DELETED_MESSAGE = "Workout deleted"
        private const val DELETE_WORKOUT_FAILED_MESSAGE = "Failed to delete workout"
    }
}

private data class WorkoutFormValues(
    val sport: String,
    val workoutDate: String,
    val durationText: String,
    val distanceText: String,
    val avgHeartRateText: String,
    val notes: String
)

private data class UpdateWorkoutRequestResult(
    val request: UpdateWorkoutRequest? = null,
    val paceText: String = "N/A",
    val errorMessage: String? = null
)
