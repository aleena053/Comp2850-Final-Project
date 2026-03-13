package com.fitnessapp.models

class CompetitionResult(
    val resultID: Int,
    var finishTime: Float,
    var position: Int,
    var notes: String
) {
    fun recordResult() {
        /*
         Save the result of a competition for an athlete.
         */
    }

    fun compareResults() {
        /*
         Compare this competition result with other results.
         */
    }

    fun updateResultNotes() {
        /*
         Update notes attached to the competition result.
         */
    }
}