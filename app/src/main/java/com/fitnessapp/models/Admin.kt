package com.fitnessapp.models

class Admin(
    userID: Int,
    name: String,
    email: String,
    password: String,
    dateOfBirth: String,
    fitnessLevel: String,
    role: String,
    var adminLevel: String
) : User(
    userID = userID,
    name = name,
    email = email,
    password = password,
    dateOfBirth = dateOfBirth,
    fitnessLevel = fitnessLevel,
    role = role
) {
    fun reviewReports() {
        /*
         Allow the admin to review submitted reports.
         */
    }

    fun warnUser() {
        /*
         Allow the admin to issue a warning to a user.
         */
    }

    fun muteUser() {
        /*
         Allow the admin to mute a user.
         */
    }

    fun suspendUser() {
        /*
         Allow the admin to suspend a user account.
         */
    }
}