package com.example.myapplication

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("signup")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>
}
