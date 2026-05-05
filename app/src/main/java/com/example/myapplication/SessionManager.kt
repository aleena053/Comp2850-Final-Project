package com.example.myapplication

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("fitness_app_session", Context.MODE_PRIVATE)

    fun saveUserSession(user: UserResponse) {
        prefs.edit {
            putBoolean("is_logged_in", true)
            putInt("user_id", user.userId)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_role", user.role)
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun getUserRole(): String {
        return prefs.getString("user_role", "") ?: ""
    }

    fun clearSession() {
        prefs.edit {
            clear()
        }
    }
}
