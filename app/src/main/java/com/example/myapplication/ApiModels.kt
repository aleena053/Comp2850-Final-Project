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
