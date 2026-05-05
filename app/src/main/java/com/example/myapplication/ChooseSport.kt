package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

/**
 * ChooseSport is a simple routing screen that sits between the dashboard and
 * the workout-logging form.
 *
 * The user taps one of four sport buttons and this screen creates an Intent
 * for [LogWorkout], passing the chosen sport name as an extra. LogWorkout then
 * uses that name to decide which form fields to show (cardio vs gym).
 *
 * There is no data fetching here — this screen is purely navigational.
 */

class ChooseSport : Activity() {

    // UI references
    private lateinit var back: Button            // returns to AthleteDashboard
    private lateinit var running: Button         // selects the Running sport
    private lateinit var cycling: Button         // selects the Cycling sport
    private lateinit var weightLifting: Button   // selects the Gym (weight lifting) sport
    private lateinit var swimming: Button        // selects the Swimming sport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_sport)

        // Wire up all buttons from the layout
        back = findViewById(R.id.back)
        running = findViewById(R.id.running)
        cycling = findViewById(R.id.cycling)
        weightLifting = findViewById(R.id.weightLifting)
        swimming = findViewById(R.id.swimming)

        // Back arrow just closes this screen, returning to the dashboard
        back.setOnClickListener {
            finish()
        }

        // Each sport button calls the same helper with a different sport constant
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

    /**
     * Launches [LogWorkout] with the selected [sportName] attached as an Intent
     * extra. LogWorkout reads this extra in its own onCreate to configure the
     * correct form layout (cardio fields vs gym exercise rows).
     *
     * @param sportName one of the sport constants defined in this file's companion object
     */
    private fun openLogWorkout(sportName: String) {
        val intent = Intent(this, LogWorkout::class.java)
        intent.putExtra(SPORT_NAME, sportName)
        startActivity(intent)
    }

    companion object {
        // Key used to pass the sport name through the Intent extra
        private const val SPORT_NAME = "sport_name"

        // Sport name constants — these must match the string "Gym" checked in WorkoutUtils.isGymSport()
        private const val RUNNING_SPORT = "Running"
        private const val CYCLING_SPORT = "Cycling"
        private const val GYM_SPORT = "Gym"
        private const val SWIMMING_SPORT = "Swimming"
    }
}
