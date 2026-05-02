package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : Activity() {

    private lateinit var accountType: Spinner
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signupbutton: Button
    private lateinit var goToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        bindViews()
        setupAccountTypeSpinner()
        setupListeners()
    }

    private fun bindViews() {
        accountType = findViewById(R.id.accountType)
        name = findViewById(R.id.name)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        signupbutton = findViewById(R.id.signup)
        goToLogin = findViewById(R.id.goToLogin)
    }

    private fun setupAccountTypeSpinner() {
        val accountTypes = arrayOf(
            SELECT_ACCOUNT_TYPE,
            CASUAL_RUNNER_ROLE,
            ATHLETE_ROLE,
            TRAINER_ROLE
        )
        val adapter = ArrayAdapter(this, R.layout.selected_item, accountTypes)
        adapter.setDropDownViewResource(R.layout.dropdown_item)
        accountType.adapter = adapter
    }

    private fun setupListeners() {
        signupbutton.setOnClickListener {
            signUpUser()
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun signUpUser() {
        val formData = readFormData()
        val validationMessage = validateForm(formData)

        if (validationMessage != null) {
            showToast(validationMessage)
        } else {
            submitSignup(formData)
        }
    }

    private fun readFormData(): SignUpFormData {
        return SignUpFormData(
            selectedAccountType = accountType.selectedItem.toString(),
            name = name.text.toString().trim(),
            username = username.text.toString().trim().lowercase(),
            email = email.text.toString().trim(),
            password = password.text.toString().trim(),
            confirmPassword = confirmPassword.text.toString().trim()
        )
    }

    private fun validateForm(formData: SignUpFormData): String? {
        val missingRequiredFields = formData.name.isEmpty() ||
                formData.username.isEmpty() ||
                formData.email.isEmpty() ||
                formData.password.isEmpty() ||
                formData.confirmPassword.isEmpty()

        return when {
            formData.selectedAccountType == SELECT_ACCOUNT_TYPE ->
                SELECT_ACCOUNT_TYPE_MESSAGE

            missingRequiredFields ->
                MISSING_FIELDS_MESSAGE

            formData.password != formData.confirmPassword ->
                PASSWORDS_DO_NOT_MATCH_MESSAGE

            else -> null
        }
    }

    private fun submitSignup(formData: SignUpFormData) {
        val request = SignUpRequest(
            name = formData.name,
            username = formData.username,
            email = formData.email,
            password = formData.password,
            role = formData.selectedAccountType.lowercase()
        )

        RetrofitClient.apiService.signUp(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast(ACCOUNT_CREATED_MESSAGE)
                    startActivity(Intent(this@SignUp, Login::class.java))
                    finish()
                } else {
                    showToast(parseSignupError(response))
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                showToast("Network error: ${t.message}")
            }
        })
    }

    private fun parseSignupError(response: Response<ApiResponse>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, ApiResponse::class.java)
            apiError.message
        } catch (_: Exception) {
            SIGNUP_FAILED_MESSAGE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val SELECT_ACCOUNT_TYPE = "Select Account Type"
        private const val CASUAL_RUNNER_ROLE = "Casual Runner"
        private const val ATHLETE_ROLE = "Athlete"
        private const val TRAINER_ROLE = "Trainer"
        private const val SELECT_ACCOUNT_TYPE_MESSAGE =
            "Please select an account type"
        private const val MISSING_FIELDS_MESSAGE =
            "Please fill in all required fields"
        private const val PASSWORDS_DO_NOT_MATCH_MESSAGE =
            "Passwords do not match"
        private const val ACCOUNT_CREATED_MESSAGE =
            "Account created successfully"
        private const val SIGNUP_FAILED_MESSAGE = "Signup failed"
    }
}

private data class SignUpFormData(
    val selectedAccountType: String,
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)
