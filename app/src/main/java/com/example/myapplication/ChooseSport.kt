package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class ChooseSport : Activity() {

    private lateinit var back: Button
    private lateinit var running: Button
    private lateinit var cycling: Button
    private lateinit var weightLifting: Button
    private lateinit var swimming: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_sport)

        back = findViewById(R.id.back)
        running = findViewById(R.id.running)
        cycling = findViewById(R.id.cycling)
        weightLifting = findViewById(R.id.weightLifting)
        swimming = findViewById(R.id.swimming)

        back.setOnClickListener {
            finish()
        }

        running.setOnClickListener {
            openLogWorkout(RUNNING_SPORT)
        }

        cycling.setOnClickListener {
            openLogWorkout(CYCLING_SPORT)
        }

        weightLifting.setOnClickListener {
            openLogWorkout(GYM_SPORT)
        }

        swimming.setOnClickListener {
            openLogWorkout(SWIMMING_SPORT)
        }
    }

    private fun openLogWorkout(sportName: String) {
        val intent = Intent(this, LogWorkout::class.java)
        intent.putExtra(SPORT_NAME, sportName)
        startActivity(intent)
    }

    companion object {
        private const val SPORT_NAME = "sport_name"
        private const val RUNNING_SPORT = "Running"
        private const val CYCLING_SPORT = "Cycling"
        private const val GYM_SPORT = "Gym"
        private const val SWIMMING_SPORT = "Swimming"
    }
}
