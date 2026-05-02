package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle

//checks whether user is already logged in
class Main : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //access saved user session data
        val sessionManager = SessionManager(this)

        //if user session exists
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getUserRole()
            val name = sessionManager.getUserName()
            val email = sessionManager.getUserEmail()
            val userId = sessionManager.getUserId()

            intent.putExtra("USER_ID", userId)
            intent.putExtra("USER_NAME", name)
            intent.putExtra("USER_EMAIL", email)
            intent.putExtra("USER_ROLE", role)

            //shows intial logged in screen
            startActivity(intent)
        } else {
            //if user is logged in, open home screen
            startActivity(Intent(this, Home::class.java))
        }

        finish()
    }
}
