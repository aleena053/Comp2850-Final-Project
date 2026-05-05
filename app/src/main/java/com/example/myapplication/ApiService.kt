package com.example.myapplication

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("signup")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>

    @POST("workouts")
    fun createWorkout(@Body request: LogWorkoutRequest): Call<BasicApiResponse>

    @GET("workouts/{user_id}")
    fun getWorkouts(@Path("user_id") userId: Int): Call<WorkoutsResponse>

    @GET("workouts/detail/{workout_id}")
    fun getWorkoutDetail(@Path("workout_id") workoutId: Int): Call<WorkoutDetailResponse>

    @PUT("workouts/{workout_id}")
    fun updateWorkout(
        @Path("workout_id") workoutId: Int,
        @Body request: UpdateWorkoutRequest
    ): Call<BasicApiResponse>

    @DELETE("workouts/{workout_id}")
    fun deleteWorkout(@Path("workout_id") workoutId: Int): Call<BasicApiResponse>

    @GET("dashboard-stats/{user_id}")
    fun getDashboardStats(@Path("user_id") userId: Int): Call<DashboardStatsResponse>

}
