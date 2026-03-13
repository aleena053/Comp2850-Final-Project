package com.fitnessapp.models

class Competition(
    val competitionID: Int,
    var name: String,
    var location: String,
    var competitionDate: String,
    var description: String
) {
    fun viewCompetition() {
        /*
         Display the details of the competition.
         */
    }

    fun registerAthlete() {
        /*
         Register an athlete for the competition.
         */
    }

    fun cancelRegistration() {
        /*
         Cancel an athlete's registration for the competition.
         */
    }
}