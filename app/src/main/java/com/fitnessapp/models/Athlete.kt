package com.fitnessapp.models

class Athlete(
    userID: Int,
    name: String,
    email: String,
    password: String,
    dateOfBirth: String,
    fitnessLevel: String,
    role: String
) : User(
    userID = userID,
    name = name,
    email = email,
    password = password,
    dateOfBirth = dateOfBirth,
    fitnessLevel = fitnessLevel,
    role = role
) {
    fun recordWorkout() {
        /*
         Allow the athlete to record a completed workout.
         */
    }

    fun viewStatistics() {
        /*
         Show the athlete's statistics and performance insights.
         */
    }

    fun joinCompetition() {
        /*
         Allow the athlete to join a competition.
         */
    }
}