package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class Home : Activity() {

    private lateinit var signUp: Button
    private lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        signUp = findViewById(R.id.signUp)
        login = findViewById(R.id.login)

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        login.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
}
