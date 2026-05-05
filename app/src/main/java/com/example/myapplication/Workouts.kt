@file:Suppress("ReturnCount", "LongParameterList")

package com.example.myapplication

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import java.util.Locale

data class WorkoutInput(
    val workoutDate: String,
    val durationText: String,
    val distanceText: String,
    val avgHeartRateText: String,
    val notes: String
)

data class WorkoutRequestResult(
    val request: LogWorkoutRequest? = null,
    val errorMessage: String? = null
)

data class ValidationResult<T>(
    val value: T? = null,
    val errorMessage: String? = null
)

fun createWorkoutRequest(
    userId: Int,
    selectedSportName: String,
    input: WorkoutInput,
    layoutExerciseContainer: LinearLayout
): WorkoutRequestResult {
    val missingBasicFields = input.workoutDate.isBlank() ||
            input.durationText.isBlank()

    if (missingBasicFields) {
        return WorkoutRequestResult(
            errorMessage = MISSING_REQUIRED_FIELDS_MESSAGE
        )
    }

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

    val firstError = listOfNotNull(
        durationResult.errorMessage,
        distanceResult.errorMessage,
        heartRateResult.errorMessage,
        exerciseResult.errorMessage
    ).firstOrNull()

    if (firstError != null) {
        return WorkoutRequestResult(errorMessage = firstError)
    }

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

private fun createSuccessfulWorkoutRequest(
    userId: Int,
    selectedSportName: String,
    input: WorkoutInput,
    duration: Int,
    distance: Double?,
    avgHeartRate: Int?,
    exercises: List<ExerciseEntry>?
): WorkoutRequestResult {
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
            notes = input.notes.ifBlank { null },
            exercises = exercises
        )
    )
}

fun parsePositiveInt(value: String): ValidationResult<Int> {
    val parsed = value.toIntOrNull()

    return if (parsed != null && parsed > ZERO_INT) {
        ValidationResult(value = parsed)
    } else {
        ValidationResult(errorMessage = INVALID_DURATION_MESSAGE)
    }
}

fun parseDistanceForSport(
    selectedSportName: String,
    distanceText: String
): ValidationResult<Double?> {
    return when {
        isGymSport(selectedSportName) || distanceText.isBlank() -> {
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

fun buildExerciseValidation(
    selectedSportName: String,
    layoutExerciseContainer: LinearLayout
): ValidationResult<List<ExerciseEntry>?> {
    if (!isGymSport(selectedSportName)) {
        return ValidationResult(value = null)
    }

    val exerciseList = mutableListOf<ExerciseEntry>()

    for (index in ZERO_INT until layoutExerciseContainer.childCount) {
        val row = layoutExerciseContainer.getChildAt(index)
        val rowResult = parseExerciseRow(row)

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

@Suppress("CyclomaticComplexMethod")
fun parseExerciseRow(row: View): ValidationResult<ExerciseEntry> {
    val exerciseNameField = row.findViewById<EditText>(R.id.exerciseNameItem)
    val setsField = row.findViewById<EditText>(R.id.setsItem)
    val repsField = row.findViewById<EditText>(R.id.repsItem)
    val weightField = row.findViewById<EditText>(R.id.weightKgItem)

    val exerciseName = exerciseNameField.text.toString().trim()
    val sets = setsField.text.toString().trim().toIntOrNull()
    val reps = repsField.text.toString().trim().toIntOrNull()
    val weight = weightField.text.toString().trim().toDoubleOrNull()

    val incompleteFields = exerciseName.isBlank() ||
            sets == null ||
            reps == null ||
            weight == null

    if (incompleteFields) {
        return ValidationResult(errorMessage = COMPLETE_EXERCISE_FIELDS_MESSAGE)
    }

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

fun isGymSport(selectedSportName: String): Boolean {
    return selectedSportName.equals(GYM_SPORT, ignoreCase = true)
}

fun calculatePace(durationMinutes: Int, distanceKm: Double?): Double? {
    return if (distanceKm == null || distanceKm <= ZERO_DOUBLE) {
        null
    } else {
        durationMinutes / distanceKm
    }
}

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

private const val ZERO_INT = 0
private const val ZERO_DOUBLE = 0.0
private const val GYM_SPORT = "Gym"
private const val PACE_FORMAT = "%.2f min/km"
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
