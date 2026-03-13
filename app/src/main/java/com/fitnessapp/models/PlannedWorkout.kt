package com.fitnessapp.models

class PlannedWorkout(
    val plannedWorkoutID: Int,
    var scheduledDate: String,
    var targetDistance: Float,
    var targetDuration: Int,
    var status: String
) {
    fun markCompleted() {
        /*
         Mark the planned workout as completed.
         */
    }

    fun updateTarget() {
        /*
         Update the target distance or duration for the planned workout.
         */
    }
}