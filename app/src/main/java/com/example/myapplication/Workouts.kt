@file:Suppress("ReturnCount", "LongParameterList")

package com.example.myapplication

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import java.util.Locale

// Shared data classes

/**
 * A plain container for the raw string values read from the workout form.
 * Passed into [createWorkoutRequest] so validation helpers don't need
 * direct access to the Activity's Views.
 */
data class WorkoutInput(
    val workoutDate: String,
    val durationText: String,
    val distanceText: String,
    val avgHeartRateText: String,
    val notes: String
)

/**
 * The result returned by [createWorkoutRequest].
 *
 * Exactly one of [request] or [errorMessage] will be non-null:
 *  - [request] is set when all validation passes and a [LogWorkoutRequest] was built.
 *  - [errorMessage] is set when any validation step failed; the caller should
 *    surface this string to the user (e.g. via a Toast).
 */
data class WorkoutRequestResult(
    val request: LogWorkoutRequest? = null,
    val errorMessage: String? = null
)

/**
 * A generic result wrapper used by every individual validation function.
 *
 * [T] is the type of the successfully parsed value (e.g. [Int] for duration,
 * [Double] for distance, [List<ExerciseEntry>] for gym exercises).
 * Again, exactly one of [value] or [errorMessage] will be non-null.
 */
data class ValidationResult<T>(
    val value: T? = null,
    val errorMessage: String? = null
)

// Top level entry point

/**
 * Orchestrates the full validation and construction of a [LogWorkoutRequest].
 *
 * Called by both [LogWorkout] (new workout) and conceptually mirrors the logic
 * in [WorkoutDetail] (edit workout). Keeping this logic here avoids duplication
 * between the two screens.
 *
 * Validation order:
 *  1. Required text fields (date, duration) must not be blank.
 *  2. Duration must parse to a positive integer.
 *  3. Distance must parse to a positive double (cardio only; optional for gym).
 *  4. Heart rate must parse to a positive integer (optional for all sports).
 *  5. Exercise rows must all be complete and valid (gym only).
 *
 * The first validation error encountered is returned immediately; the caller
 * receives only one error at a time so the user can fix them in sequence.
 *
 * @param userId                   the logged-in user's ID from [SessionManager]
 * @param selectedSportName        sport chosen on [ChooseSport] (e.g. "Running", "Gym")
 * @param input                    raw text values collected from the form fields
 * @param layoutExerciseContainer  the LinearLayout holding the gym exercise rows;
 *                                 ignored for non-gym sports
 * @return [WorkoutRequestResult] with either a ready-to-submit request or an error string
 */
fun createWorkoutRequest(
    userId: Int,
    selectedSportName: String,
    input: WorkoutInput,
    layoutExerciseContainer: LinearLayout
): WorkoutRequestResult {

    // Step 1: Quick-fail on blank required fields before doing any parsing
    val missingBasicFields = input.workoutDate.isBlank() ||
            input.durationText.isBlank()

    if (missingBasicFields) {
        return WorkoutRequestResult(
            errorMessage = MISSING_REQUIRED_FIELDS_MESSAGE
        )
    }

    // Step 2-5: Parse and validate each field independently
    val durationResult = parsePositiveInt(input.durationText)
    val distanceResult = parseDistanceForSport(
        selectedSportName,
        input.distanceText
    )
    val heartRateResult = parseOptionalHeartRate(input.avgHeartRateText)
    val exerciseResult = buildExerciseValidation(
        selectedSportName,
        layoutExerciseContainer
    )

    // Surface the first validation error, if any
    val firstError = listOfNotNull(
        durationResult.errorMessage,
        distanceResult.errorMessage,
        heartRateResult.errorMessage,
        exerciseResult.errorMessage
    ).firstOrNull()

    if (firstError != null) {
        return WorkoutRequestResult(errorMessage = firstError)
    }

    // All validation passed — build and return the final request object
    return createSuccessfulWorkoutRequest(
        userId = userId,
        selectedSportName = selectedSportName,
        input = input,
        duration = durationResult.value ?: ZERO_INT,
        distance = distanceResult.value,
        avgHeartRate = heartRateResult.value,
        exercises = exerciseResult.value
    )
}

/**
 * Assembles the final [LogWorkoutRequest] once all fields have been validated.
 *
 * Pace is calculated here (duration / distance) and set to null for gym
 * workouts where it is not meaningful. Empty notes are stored as null rather
 * than an empty string to avoid noise in the database.
 */
private fun createSuccessfulWorkoutRequest(
    userId: Int,
    selectedSportName: String,
    input: WorkoutInput,
    duration: Int,
    distance: Double?,
    avgHeartRate: Int?,
    exercises: List<ExerciseEntry>?
): WorkoutRequestResult {

    // Pace only makes sense for cardio sports
    val avgPace = if (isGymSport(selectedSportName)) {
        null
    } else {
        calculatePace(duration, distance)
    }

    return WorkoutRequestResult(
        request = LogWorkoutRequest(
            userId = userId,
            sport = selectedSportName,
            workoutDate = input.workoutDate,
            duration = duration,
            distanceKm = distance,
            avgPace = avgPace,
            avgHeartRate = avgHeartRate,
            notes = input.notes.ifBlank { null }, // convert empty string to null
            exercises = exercises
        )
    )
}

// Individual field validators

/**
 * Parses [value] as a positive integer (duration in minutes).
 * Returns an error if the string is non-numeric or <= 0.
 */
fun parsePositiveInt(value: String): ValidationResult<Int> {
    val parsed = value.toIntOrNull()

    return if (parsed != null && parsed > ZERO_INT) {
        ValidationResult(value = parsed)
    } else {
        ValidationResult(errorMessage = INVALID_DURATION_MESSAGE)
    }
}

/**
 * Parses the distance field according to the sport type:
 *  - Gym sport or blank input → distance is not required; returns null (valid).
 *  - Non-blank input that parses to a positive double → returns the distance.
 *  - Non-blank input that is not a valid positive double → returns an error.
 */
fun parseDistanceForSport(
    selectedSportName: String,
    distanceText: String
): ValidationResult<Double?> {
    return when {
        isGymSport(selectedSportName) || distanceText.isBlank() -> {
            // Distance is either not applicable (gym) or intentionally left blank
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

/**
 * Parses the heart rate field. Heart rate is always optional:
 *  - Blank → returns null (valid; the athlete may not own an HR monitor).
 *  - Positive integer → returns the parsed value.
 *  - Anything else → returns an error.
 */
fun parseOptionalHeartRate(avgHeartRateText: String): ValidationResult<Int?> {
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
 * Validates the gym exercise rows when [selectedSportName] is "Gym".
 * For non-gym sports, immediately returns null (no exercises needed).
 *
 * Iterates every child view in [layoutExerciseContainer], delegating each row
 * to [parseExerciseRow]. Returns the first row-level error encountered, or an
 * error if the container has no valid exercises at all.
 */
fun buildExerciseValidation(
    selectedSportName: String,
    layoutExerciseContainer: LinearLayout
): ValidationResult<List<ExerciseEntry>?> {
    if (!isGymSport(selectedSportName)) {
        return ValidationResult(value = null) // not a gym workout — skip exercise validation
    }

    val exerciseList = mutableListOf<ExerciseEntry>()

    for (index in ZERO_INT until layoutExerciseContainer.childCount) {
        val row = layoutExerciseContainer.getChildAt(index)
        val rowResult = parseExerciseRow(row)

        // Fail fast: return the first row error rather than collecting all errors
        if (rowResult.errorMessage != null) {
            return ValidationResult(errorMessage = rowResult.errorMessage)
        }

        rowResult.value?.let { exerciseList.add(it) }
    }

    return if (exerciseList.isEmpty()) {
        ValidationResult(errorMessage = ADD_EXERCISE_MESSAGE)
    } else {
        ValidationResult(value = exerciseList)
    }
}

/**
 * Reads and validates a single exercise row inflated from exercise_item.xml.
 *
 * Each row contains four fields: exercise name, sets, reps, and weight (kg).
 * Validation checks:
 *  1. No field may be blank or fail to parse to its expected type.
 *  2. Sets and reps must be positive integers; weight must be a positive double.
 *
 * @param row the root View of one exercise_item.xml row
 * @return [ValidationResult] containing an [ExerciseEntry] on success, or an error string
 */
@Suppress("CyclomaticComplexMethod")
fun parseExerciseRow(row: View): ValidationResult<ExerciseEntry> {
    // Read the four EditText values from this row's view hierarchy
    val exerciseNameField = row.findViewById<EditText>(R.id.exerciseNameItem)
    val setsField = row.findViewById<EditText>(R.id.setsItem)
    val repsField = row.findViewById<EditText>(R.id.repsItem)
    val weightField = row.findViewById<EditText>(R.id.weightKgItem)

    val exerciseName = exerciseNameField.text.toString().trim()
    val sets = setsField.text.toString().trim().toIntOrNull()
    val reps = repsField.text.toString().trim().toIntOrNull()
    val weight = weightField.text.toString().trim().toDoubleOrNull()

    // Check 1: all fields must be present and parse successfully
    val incompleteFields = exerciseName.isBlank() ||
            sets == null ||
            reps == null ||
            weight == null

    if (incompleteFields) {
        return ValidationResult(errorMessage = COMPLETE_EXERCISE_FIELDS_MESSAGE)
    }

    // Check 2: numeric values must be greater than zero
    // (sets/reps/weight are non-null here because incompleteFields was false)
    val invalidValues = sets <= ZERO_INT ||
            reps <= ZERO_INT ||
            weight <= ZERO_DOUBLE

    return if (invalidValues) {
        ValidationResult(errorMessage = INVALID_EXERCISE_VALUES_MESSAGE)
    } else {
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

// Shared utility functions

/**
 * Returns true when [selectedSportName] is "Gym" (case-insensitive).
 * Used throughout the codebase to branch between gym and cardio logic.
 */
fun isGymSport(selectedSportName: String): Boolean {
    return selectedSportName.equals(GYM_SPORT, ignoreCase = true)
}

/**
 * Calculates average pace in minutes per kilometre.
 *
 * Returns null if [distanceKm] is null or zero (avoids division by zero and
 * covers the case where distance was not recorded).
 *
 * @param durationMinutes total workout duration in whole minutes
 * @param distanceKm      total distance covered in kilometres
 */
fun calculatePace(durationMinutes: Int, distanceKm: Double?): Double? {
    return if (distanceKm == null || distanceKm <= ZERO_DOUBLE) {
        null
    } else {
        durationMinutes / distanceKm
    }
}

/**
 * Formats [avgPace] into a display string for the "Calculated Pace" label.
 *
 * Rules:
 *  - Gym sport → always "N/A" (pace is meaningless for weight training).
 *  - Non-null pace → formatted as "X.XX min/km".
 *  - Null pace (no distance recorded) → "N/A".
 *
 * @param activity         used to look up string resources (pace_na)
 * @param avgPace          the computed pace value, or null if unavailable
 * @param selectedSportName used to detect gym workouts
 */
fun formatCalculatedPace(
    activity: Activity,
    avgPace: Double?,
    selectedSportName: String
): String {
    return when {
        isGymSport(selectedSportName) -> {
            activity.getString(R.string.pace_na)
        }

        avgPace != null -> {
            String.format(Locale.getDefault(), PACE_FORMAT, avgPace)
        }

        else -> {
            activity.getString(R.string.pace_na)
        }
    }
}

// File-level constants
private const val ZERO_INT = 0
private const val ZERO_DOUBLE = 0.0
private const val GYM_SPORT = "Gym"
private const val PACE_FORMAT = "%.2f min/km" // e.g. "7.35 min/km"

// User-facing validation error messages (shown as Toast text)
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
