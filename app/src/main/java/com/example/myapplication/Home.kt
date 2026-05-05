package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

//home screen where the user has the option to sign up or login
class Home : Activity() {

    private lateinit var signUp: Button
    private lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //sets layout file for the home page
        setContentView(R.layout.home)

        //connects buttons in xml file to this file
        signUp = findViewById(R.id.signUp)
        login = findViewById(R.id.login)

        //opens SignUp file when sign up button is pressed
        signUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        //opens Login file when login button is pressed
        login.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
}
