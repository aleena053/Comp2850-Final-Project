package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : Activity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginbutton: Button
    private lateinit var goToSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginbutton = findViewById(R.id.login)
        goToSignup = findViewById(R.id.goToSignup)

        loginbutton.setOnClickListener {
            loginUser()
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    private fun loginUser() {
        val email = email.text.toString().trim()
        val password = password.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LoginRequest(email, password)

        RetrofitClient.apiService.login(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.user != null) {
                    Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()

                    val user = body.user

                    val sessionManager = SessionManager(this@Login)
                    sessionManager.saveUserSession(user)

                    finish()
                } else {
                    val message = body?.message ?: "Invalid email or password"
                    Toast.makeText(this@Login, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@Login, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
