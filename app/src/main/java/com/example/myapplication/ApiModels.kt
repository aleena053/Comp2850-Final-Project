package com.example.myapplication

data class SignUpRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val userId: Int,
    val name: String,
    val username: String? = null,
    val email: String,
    val role: String,
    val dateOfBirth: String? = null,
    val fitnessLevel: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse? = null
)

data class LogWorkoutRequest(
    val userId: Int,
    val sport: String,
    val workoutDate: String,
    val duration: Int,
    val distanceKm: Double?,
    val avgPace: Double?,
    val avgHeartRate: Int?,
    val notes: String?,
    val exercises: List<ExerciseEntry>? = null
)

data class ExerciseEntry(
    val exerciseName: String,
    val setsCount: Int,
    val repsCount: Int,
    val weightKg: Double
)

data class BasicApiResponse(
    val success: Boolean,
    val message: String
)

data class WorkoutItem(
    val workoutId: Int,
    val userId: Int? = null,
    val sportName: String,
    val workoutDate: String,
    val duration: Int,
    val distanceKm: Double?,
    val avgPace: Double?,
    val avgHeartRate: Int?,
    val notes: String?,
    val exerciseSummaries: List<String>? = null,
    val exercises: List<ExerciseEntry>? = null
)

data class WorkoutsResponse(
    val success: Boolean,
    val workouts: List<WorkoutItem>
)

data class WorkoutDetailResponse(
    val success: Boolean,
    val workout: WorkoutItem?,
    val message: String? = null
)

data class UpdateWorkoutRequest(
    val sport: String,
    val workoutDate: String,
    val duration: Int,
    val distanceKm: Double?,
    val avgPace: Double?,
    val avgHeartRate: Int?,
    val notes: String?,
    val exercises: List<ExerciseEntry>? = null
)


data class DailyDistanceItem(
    val label: String,
    val distance: Double
)

data class DashboardStats(
    val thisWeekDistance: Double,
    val thisWeekDuration: Int,
    val thisWeekAvgPace: Double,
    val thisWeekAvgHeartRate: Double,
    val dailyDistance: List<DailyDistanceItem>
)

data class DashboardStatsResponse(
    val success: Boolean,
    val stats: DashboardStats
)
