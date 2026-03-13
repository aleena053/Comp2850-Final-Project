package com.fitnessapp.models

open class User(
    val userID: Int,
    var name: String,
    var email: String,
    var password: String,
    var dateOfBirth: String,
    var fitnessLevel: String,
    var role: String
) {

    open fun updateProfile() {
        /*
         Update the user's profile details such as name, email,
         date of birth, fitness level, or role.
         */
    }

    open fun changePassword() {
        /*
         Allow the user to change their account password.
         */
    }

    open fun viewWorkouts() {
        /*
         Retrieve and display the user's workouts.
         */
    }

    open fun viewTrainingPlan() {
        /*
         Retrieve and display the user's training plan.
         */
    }

    open fun joinGroup() {
        /*
         Allow the user to join a group.
         */
    }

    open fun createGroup() {
        /*
         Allow the user to create a new group.
         */
    }

    open fun leaveGroup() {
        /*
         Allow the user to leave a group they are a member of.
         */
    }

    open fun sendGroupMessage() {
        /*
         Allow the user to send a message to a group.
         */
    }
}