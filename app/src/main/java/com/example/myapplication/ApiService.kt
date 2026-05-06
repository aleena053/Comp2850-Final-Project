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


    @POST("trainer/add-client")
    fun addClient(@Body request: AddClientRequest): Call<ApiResponse>

    @GET("trainer/{trainer_id}/clients")
    fun getTrainerClients(@Path("trainer_id") trainerId: Int): Call<ClientListResponse>

    @DELETE("trainer/{trainer_id}/clients/{client_id}")
    fun removeClient(
        @Path("trainer_id") trainerId: Int,
        @Path("client_id") clientId: Int
    ): Call<BasicApiResponse>

    @POST("training-plans")
    fun createTrainingPlan(@Body request: CreateTrainingPlanRequest): Call<BasicApiResponse>

    @GET("training-plans/{user_id}")
    fun getTrainingPlans(@Path("user_id") userId: Int): Call<TrainingPlansResponse>

    @GET("training-plans/detail/{plan_id}")
    fun getTrainingPlanDetail(@Path("plan_id") planId: Int): Call<TrainingPlanDetailResponse>

    @PUT("training-plans/{plan_id}")
    fun updateTrainingPlan(
        @Path("plan_id") planId: Int,
        @Body request: UpdateTrainingPlanRequest
    ): Call<BasicApiResponse>

    @DELETE("training-plans/{plan_id}")
    fun deleteTrainingPlan(@Path("plan_id") planId: Int): Call<BasicApiResponse>

}
