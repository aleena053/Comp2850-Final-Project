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

/**
 * WorkoutDetail lets an athlete view, edit, or delete a single previously
 * logged workout.
 *
 * It receives the target workout's ID from [WorkoutHistory] via an Intent
 * extra ("WORKOUT_ID"), fetches the full record from the API, and populates
 * all form fields. The user can then:
 *  - Change any field and tap "Update" to save the changes.
 *  - Tap "Delete" to permanently remove the workout.
 *
 * The form logic mirrors [LogWorkout]: gym workouts show an exercise list;
 * cardio workouts show distance and a calculated pace. Switching the sport
 * via the Spinner dynamically toggles between the two layouts.
 *
 * Validation for the update path is handled locally (private helpers) rather
 * than reusing WorkoutUtils, because the update request type ([UpdateWorkoutRequest])
 * differs from the create type ([LogWorkoutRequest]).
 */
@Suppress("TooManyFunctions")
class WorkoutDetail : Activity() {

    // The ID of the workout being displayed; set from the Intent extra on create
    private var workoutId: Int = INVALID_WORKOUT_ID
    // UI references
    private lateinit var backWorkoutDetail: Button           // returns to WorkoutHistory
    private lateinit var sportDetail: Spinner                // dropdown to change sport type
    private lateinit var workoutDateDetail: EditText         // tapping opens a DatePickerDialog
    private lateinit var durationDetail: EditText            // duration in minutes
    private lateinit var distanceDetail: EditText            // distance in km (cardio only)
    private lateinit var avgHeartRateDetail: EditText        // optional average BPM
    private lateinit var notesDetail: EditText               // free-text notes
    private lateinit var calculatedPaceDetail: TextView      // shows computed min/km
    private lateinit var updateWorkout: Button               // validates and submits the update
    private lateinit var deleteWorkout: Button               // deletes this workout

    // Sport-specific layout containers (toggled by the Spinner selection)
    private lateinit var layoutCardioFieldsDetail: LinearLayout     // wraps the distance field
    private lateinit var layoutGymFieldsDetail: LinearLayout        // wraps the exercise list
    private lateinit var layoutExerciseContainerDetail: LinearLayout // holds dynamic exercise rows
    private lateinit var addExerciseDetail: Button                  // appends a blank exercise row

    /** The ordered list of sports shown in the Spinner dropdown. */
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

        // Read the workout ID passed by WorkoutHistory
        workoutId = intent.getIntExtra(WORKOUT_ID_EXTRA, INVALID_WORKOUT_ID)

        setupSportSpinner()
        setupListeners()
        loadWorkoutDetail()
    }

    /** Finds and stores all view references from the inflated layout. */
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

    /** Wires up click listeners and the Spinner selection callback. */
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

        // Switching the sport Spinner immediately toggles the form layout
        // so the user sees the correct fields before tapping Update
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

    /**
     * Populates the sport Spinner with [sports] using the standard Android
     * dropdown item layouts.
     */
    private fun setupSportSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sports
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sportDetail.adapter = adapter
    }

    // Form layout toggling

    /** Delegates to [showGymForm] or [showCardioForm] based on [sportName]. */
    private fun updateFormForSport(sportName: String) {
        if (isGymSport(sportName)) {
            showGymForm()
        } else {
            showCardioForm()
        }
    }

    /**
     * Shows the gym layout (exercise list) and hides the cardio layout.
     * Ensures at least one exercise row exists so the user has something to fill in.
     */
    private fun showGymForm() {
        layoutGymFieldsDetail.visibility = View.VISIBLE
        layoutCardioFieldsDetail.visibility = View.GONE
        calculatedPaceDetail.text = NOT_AVAILABLE_TEXT

        if (layoutExerciseContainerDetail.isEmpty()) {
            addExerciseRow()
        }
    }

    /**
     * Shows the cardio layout (distance field) and hides the gym layout.
     * Clears any existing exercise rows and recalculates the displayed pace
     * from whatever is currently in the duration/distance fields.
     */
    private fun showCardioForm() {
        layoutGymFieldsDetail.visibility = View.GONE
        layoutCardioFieldsDetail.visibility = View.VISIBLE
        layoutExerciseContainerDetail.removeAllViews()

        // Recalculate pace from the current field values (may be empty on first load)
        val duration = durationDetail.text.toString().trim().toIntOrNull()
        val distance = distanceDetail.text.toString().trim().toDoubleOrNull()
        val pace = duration?.let { calculatePace(it, distance) }

        calculatedPaceDetail.text = if (pace != null) {
            String.format(Locale.getDefault(), PACE_FORMAT, pace)
        } else {
            AUTO_PACE_TEXT
        }
    }

    // Exercise row management

    /**
     * Inflates exercise_item.xml and appends it to the exercise container,
     * optionally pre-filling the four fields with existing data.
     *
     * Default empty strings are used when adding a blank new row;
     * saved exercise data is passed in when loading an existing gym workout.
     *
     * @param exerciseName pre-fill value for the exercise name field
     * @param sets         pre-fill value for the sets field
     * @param reps         pre-fill value for the reps field
     * @param weight       pre-fill value for the weight (kg) field
     */
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

        // Pre-fill fields (no-ops if all arguments are empty strings)
        val etExerciseName = exerciseView.findViewById<EditText>(R.id.exerciseNameItem)
        val etSets = exerciseView.findViewById<EditText>(R.id.setsItem)
        val etReps = exerciseView.findViewById<EditText>(R.id.repsItem)
        val etWeight = exerciseView.findViewById<EditText>(R.id.weightKgItem)

        // Wire up the remove button; ensure at least one row always remains
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

    /** Removes [exerciseView] and replaces it with a blank row if it was the last one. */
    private fun removeExerciseRow(exerciseView: View) {
        layoutExerciseContainerDetail.removeView(exerciseView)
        if (layoutExerciseContainerDetail.isEmpty()) {
            addExerciseRow()
        }
    }

    // Date picker

    /**
     * Opens a [DatePickerDialog] for [targetField].
     *
     * The dialog is pre-set to the date already in the field (if it matches
     * the YYYY-MM-DD pattern) so the user doesn't have to navigate far.
     * On confirmation the field is updated with the chosen date as "YYYY-MM-DD".
     */
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

    /**
     * Parses [currentText] as "YYYY-MM-DD" and returns a [Calendar] set to
     * that date. Falls back to today if the text doesn't match the pattern or
     * parsing throws an exception.
     */
    private fun getCalendarFromField(currentText: String): Calendar {
        val calendar = Calendar.getInstance()

        if (currentText.matches(DATE_REGEX.toRegex())) {
            try {
                val parts = currentText.split(DATE_SEPARATOR)
                calendar.set(
                    parts[YEAR_INDEX].toInt(),
                    parts[MONTH_INDEX].toInt() - MONTH_OFFSET, // Calendar.MONTH is 0-indexed
                    parts[DAY_INDEX].toInt()
                )
            } catch (_: Exception) {
                // Parsing failed — fall through and return today's Calendar
            }
        }

        return calendar
    }

    // Data loading

    /**
     * Fetches the full workout record from the API using [workoutId] and
     * populates all form fields via [bindWorkoutToScreen].
     *
     * Shows an error and closes the screen if [workoutId] is invalid or the
     * API returns an unsuccessful response.
     */
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

    /**
     * Populates all form fields from [workout] after a successful API load.
     *
     * Order matters here:
     *  1. Set the Spinner selection (which triggers [updateFormForSport] via its listener).
     *  2. Populate the shared fields (date, duration, heart rate, notes).
     *  3. Populate the sport-specific fields (exercises or pace).
     *  4. Call [updateFormForSport] explicitly to ensure correct field visibility,
     *     since the Spinner listener may fire before the fields are populated.
     */
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

        // Re-apply form visibility in case the Spinner fired before fields were set
        updateFormForSport(workout.sportName)
    }

    /**
     * Sets the Spinner to the position matching [sportName] (case-insensitive).
     * If the sport is not found in [sports], the selection is left unchanged.
     */
    private fun setSelectedSport(sportName: String) {
        val sportIndex = sports.indexOfFirst {
            it.equals(sportName, ignoreCase = true)
        }
        if (sportIndex >= ZERO_INT) {
            sportDetail.setSelection(sportIndex)
        }
    }

    /**
     * Populates the exercise container from [workout.WorkoutItem].
     * Clears any existing rows first, then adds one pre-filled row per exercise.
     * Falls back to a single blank row if the list is empty.
     */
    private fun bindGymWorkout(workout: WorkoutItem) {
        layoutExerciseContainerDetail.removeAllViews()

        val exerciseList = workout.exercises ?: emptyList()
        if (exerciseList.isEmpty()) {
            addExerciseRow() // blank placeholder row
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

        calculatedPaceDetail.text = NOT_AVAILABLE_TEXT // pace has no meaning for gym
    }

    /** Calculates and displays the pace for a cardio workout loaded from the API. */
    private fun bindCardioWorkout(workout: WorkoutItem) {
        val pace = calculatePace(workout.duration, workout.distanceKm)
        calculatedPaceDetail.text = if (pace != null) {
            String.format(Locale.getDefault(), PACE_FORMAT, pace)
        } else {
            NOT_AVAILABLE_TEXT
        }
    }

    // Update / Delete

    /**
     * Entry point for the "Update" button tap.
     *
     * Builds an [UpdateWorkoutRequest] via [buildUpdateWorkoutRequest], surfaces
     * any validation error as a Toast, then calls [submitWorkoutUpdate] if
     * everything is valid. Also updates the displayed pace immediately.
     */
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

    /**
     * Reads the form, validates every field, and builds an [UpdateWorkoutRequest].
     *
     * Uses an [UpdateWorkoutRequestResult] wrapper (defined at the bottom of
     * this file) that holds the ready-to-submit request, the formatted pace
     * string, and an optional error message — similar to [WorkoutRequestResult]
     * in WorkoutUtils.kt but typed for the update endpoint.
     */
    private fun buildUpdateWorkoutRequest(): UpdateWorkoutRequestResult {
        val formValues = readWorkoutFormValues()

        // Quick check: date and duration must not be blank
        val formError = validateRequiredWorkoutFields(formValues)

        if (formError != null) {
            return UpdateWorkoutRequestResult(errorMessage = formError)
        }

        // Parse each field independently
        val durationResult = getPositiveDuration(formValues.durationText)
        val distanceResult = getWorkoutDistance(formValues.sport, formValues.distanceText)
        val heartRateResult = getWorkoutHeartRate(formValues.avgHeartRateText)
        val exerciseResult = buildWorkoutExercises(formValues.sport)

        // Surface the first error found
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

    /** Reads all editable form fields into a [WorkoutFormValues] snapshot. */
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

    /** Returns an error string if date or duration is blank; null otherwise. */
    private fun validateRequiredWorkoutFields(formValues: WorkoutFormValues): String? {
        val missingRequiredFields = formValues.workoutDate.isBlank() ||
                formValues.durationText.isBlank()

        return if (missingRequiredFields) {
            MISSING_REQUIRED_FIELDS_MESSAGE
        } else {
            null
        }
    }

    /**
     * Assembles the [UpdateWorkoutRequestResult] once all fields are validated.
     * Calculates pace (null for gym), formats the pace display string, and
     * constructs the [UpdateWorkoutRequest].
     */
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

    // Per-field validation helpers (private to WorkoutDetail)

    /** Parses duration; returns an error if non-positive or non-numeric. */
    private fun getPositiveDuration(durationText: String): ValidationResult<Int> {
        val duration = durationText.toIntOrNull()
        return if (duration != null && duration > ZERO_INT) {
            ValidationResult(value = duration)
        } else {
            ValidationResult(errorMessage = INVALID_DURATION_MESSAGE)
        }
    }

    /**
     * Parses distance; null is valid for gym or blank input.
     * Returns an error only when a non-blank, non-positive value is entered.
     */
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

    /** Parses heart rate; null is valid (field is optional). */
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

    /**
     * Returns null exercises for cardio; delegates to [collectGymExercises]
     * for gym workouts.
     */
    private fun buildWorkoutExercises(sport: String): ValidationResult<List<ExerciseEntry>?> {
        return if (!isGymSport(sport)) {
            ValidationResult(value = null)
        } else {
            collectGymExercises()
        }
    }

    /**
     * Iterates every child View in [layoutExerciseContainerDetail] and parses
     * each as an exercise row via [getWorkoutExerciseRow].
     * Returns the first row error encountered, or an error if the list is empty.
     */
    private fun collectGymExercises(): ValidationResult<List<ExerciseEntry>?> {
        val exerciseList = mutableListOf<ExerciseEntry>()
        var errorMessage: String? = null

        for (i in ZERO_INT until layoutExerciseContainerDetail.childCount) {
            val row = layoutExerciseContainerDetail.getChildAt(i)
            val rowResult = getWorkoutExerciseRow(row)

            if (rowResult.errorMessage != null) {
                errorMessage = rowResult.errorMessage
                break // fail fast on the first bad row
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

    /**
     * Reads and validates one exercise row in the detail screen.
     *
     * Checks:
     *  1. All four fields (name, sets, reps, weight) must be present and parseable.
     *  2. Numeric values must be greater than zero.
     *
     * Note: this is a private duplicate of [parseExerciseRow] in WorkoutUtils
     * because [WorkoutDetail] uses different field variable names and an extra
     * null-safety guard ([hasMissingFields] / [hasInvalidNumbers] split).
     */
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

        // hasInvalidNumbers is only evaluated when all fields parsed successfully
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

    // API calls

    /**
     * PUTs the [UpdateWorkoutRequest] to the API for [workoutId].
     * On success, shows a confirmation and closes the screen (onResume on
     * WorkoutHistory will reload the updated list).
     */
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

    /**
     * DELETEs the workout for [workoutId] from the API.
     * On success, shows a confirmation and closes the screen.
     */
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

    // Utility helpers

    /**
     * Calculates pace as duration / distance.
     * Returns null rather than throwing if [distanceKm] is null or zero.
     */
    private fun calculatePace(durationMinutes: Int, distanceKm: Double?): Double? {
        return if (distanceKm == null || distanceKm <= ZERO_DOUBLE) {
            null
        } else {
            durationMinutes / distanceKm
        }
    }

    /** Formats [avgPace] as "X.XX min/km", or "N/A" when pace is null. */
    private fun formatPaceText(avgPace: Double?): String {
        return if (avgPace != null) {
            String.format(Locale.getDefault(), PACE_FORMAT, avgPace)
        } else {
            NOT_AVAILABLE_TEXT
        }
    }

    /**
    * Converts [rawDate] (which may arrive from the API in several formats)
    * into a plain "YYYY-MM-DD" string.
    *
    * Three formatters are tried in order:
    *  1. RFC-822 / HTTP-date: "EEE, dd MMM yyyy HH:mm:ss z"  (e.g. from some REST APIs)
    *  2. ISO date-only: "yyyy-MM-dd"
    *  3. ISO datetime: "yyyy-MM-dd HH:mm:ss"
    *
    * Falls back to taking the first 10 characters of the raw string, which
    * covers the common "YYYY-MM-DD…" prefix regardless of what follows.
    */
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
                // This formatter didn't match — try the next one
            }
        }

        // Last resort: trim to the date portion if long enough, or return as-is
        return if (rawDate.length >= DATE_TEXT_LENGTH) {
            rawDate.take(DATE_TEXT_LENGTH)
        } else {
            rawDate
        }
    }

    /** Returns true when [sportName] equals "Gym" (case-insensitive). */
    private fun isGymSport(sportName: String): Boolean {
        return sportName.equals(GYM_SPORT, ignoreCase = true)
    }

    /** Convenience wrapper so every call site doesn't need the context argument. */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Constants
    companion object {
        private const val INVALID_WORKOUT_ID = -1
        private const val ZERO_INT = 0
        private const val ZERO_DOUBLE = 0.0

        // Indices into the "YYYY-MM-DD".split("-") array
        private const val YEAR_INDEX = 0
        private const val MONTH_INDEX = 1
        private const val DAY_INDEX = 2
        private const val MONTH_OFFSET = 1 // Calendar.MONTH is 0-indexed
        private const val DATE_TEXT_LENGTH = 10 // length of "YYYY-MM-DD"

        private const val WORKOUT_ID_EXTRA = "WORKOUT_ID"
        private const val DATE_SEPARATOR = "-"
        private const val DATE_REGEX = """\d{4}-\d{2}-\d{2}"""

        // Sport name constants
        private const val RUNNING_SPORT = "Running"
        private const val CYCLING_SPORT = "Cycling"
        private const val SWIMMING_SPORT = "Swimming"
        private const val GYM_SPORT = "Gym"

        // Format strings
        private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"
        private const val PACE_FORMAT = "%.2f min/km"

        // Date format patterns tried by formatWorkoutDate()
        private const val GMT_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
        private const val BASIC_DATE_PATTERN = "yyyy-MM-dd"
        private const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
        private const val UTC_TIMEZONE = "UTC"

        // Display strings
        private const val NOT_AVAILABLE_TEXT = "N/A"
        private const val AUTO_PACE_TEXT = "Will be calculated automatically"

        // User-facing error / success messages
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

/**
 * Snapshot of the raw text values read from the WorkoutDetail form.
 * Used internally to pass form state between validation helpers without
 * passing individual parameters for each field.
 */
private data class WorkoutFormValues(
    val sport: String,
    val workoutDate: String,
    val durationText: String,
    val distanceText: String,
    val avgHeartRateText: String,
    val notes: String
)

/**
 * The result returned by [WorkoutDetail.buildUpdateWorkoutRequest].
 *
 * Mirrors [WorkoutRequestResult] from WorkoutUtils but is typed for the update
 * path and also carries a pre-formatted [paceText] string ready to display.
 *
 * Exactly one of [request] or [errorMessage] will be non-null.
 */
private data class UpdateWorkoutRequestResult(
    val request: UpdateWorkoutRequest? = null,
    val paceText: String = "N/A", // formatted pace or "N/A" to show in the UI
    val errorMessage: String? = null
)
