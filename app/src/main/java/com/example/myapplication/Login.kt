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
        //sets layout file for the login screen
        setContentView(R.layout.login)

        //links xml to this file
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginbutton = findViewById(R.id.login)
        goToSignup = findViewById(R.id.goToSignup)

        //when login is pressed it called login function
        loginbutton.setOnClickListener {
            loginUser()
        }

        //opens Sign Up screen if Sign up is pressed
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

        //sends login request to backend using Retrofit
        RetrofitClient.apiService.login(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                val body = response.body()

                //checks if login was successful
                if (response.isSuccessful && body?.success == true && body.user != null) {
                    Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()

                    val user = body.user

                    val sessionManager = SessionManager(this@Login)
                    sessionManager.saveUserSession(user)

                    val intent = when (user.role.lowercase()) {
                        "trainer" -> Intent(this@Login, TrainerDashboard::class.java)
                        "athlete", "casual runner" -> Intent(this@Login, AthleteDashboard::class.java)
                        else -> Intent(this@Login, AthleteDashboard::class.java)
                    }

                    intent.putExtra("USER_ID", user.userId)
                    intent.putExtra("USER_NAME", user.name)
                    intent.putExtra("USERNAME", user.username)
                    intent.putExtra("USER_EMAIL", user.email)
                    intent.putExtra("USER_ROLE", user.role)

                    startActivity(intent)
                    //close login screen if login was successfull
                    finish()
                } else {
                    //if login fails
                    val message = body?.message ?: "Invalid email or password"
                    Toast.makeText(this@Login, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                //show error if cannot connect to the backend api
                Toast.makeText(this@Login, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
