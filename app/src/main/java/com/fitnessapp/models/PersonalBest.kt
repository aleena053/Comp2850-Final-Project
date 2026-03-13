package com.fitnessapp.models

class PersonalBest(
    val pbID: Int,
    var bestTime: Float,
    var dateAchieved: String
) {
    fun updatePersonalBest() {
        /*
         Update the athlete's personal best record.
         */
    }

    fun comparePerformance() {
        /*
         Compare current performance against the personal best.
         */
    }
}