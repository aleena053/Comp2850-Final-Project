package com.fitnessapp.models

class Trainer(
    userID: Int,
    name: String,
    email: String,
    password: String,
    dateOfBirth: String,
    fitnessLevel: String,
    role: String,
    var certification: String
) : User(
    userID = userID,
    name = name,
    email = email,
    password = password,
    dateOfBirth = dateOfBirth,
    fitnessLevel = fitnessLevel,
    role = role
) {
    fun createTrainingPlan() {
        /*
         Allow the trainer to create a training plan.
         */
    }

    fun assignPlan() {
        /*
         Allow the trainer to assign a training plan to an athlete.
         */
    }

    fun viewClientProgress() {
        /*
         Allow the trainer to view an athlete's progress.
         */
    }
}