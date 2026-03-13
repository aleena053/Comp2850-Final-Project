package com.fitnessapp.models

class Workout(
    val workoutID: Int,
    var workoutDate: String,
    var duration: Float,
    var distance: Float,
    var heartRate: Int,
    var pace: Float,
    var calories: Int,
    var notes: String,
    var status: String
) {
    fun recordDetails() {
        /*
         Save the details of the workout.
         */
    }

    fun updateNotes() {
        /*
         Update notes related to the workout.
         */
    }

    fun markCompleted() {
        /*
         Mark the workout as completed.
         */
    }
}