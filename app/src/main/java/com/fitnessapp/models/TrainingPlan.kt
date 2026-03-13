package com.fitnessapp.models

class TrainingPlan(
    val planID: Int,
    var title: String,
    var startDate: String,
    var endDate: String
) {
    fun addWorkout() {
        /*
         Add a workout to the training plan.
         */
    }

    fun removeWorkout() {
        /*
         Remove a workout from the training plan.
         */
    }

    fun updatePlan() {
        /*
         Update the training plan details.
         */
    }
}