package com.fitnessapp.models

class Report(
    val reportID: Int,
    var reason: String,
    var status: String
) {
    fun submitReport() {
        /*
         Submit a new report for admin review.
         */
    }

    fun reviewReport() {
        /*
         Review a submitted report.
         */
    }

    fun resolveReport() {
        /*
         Mark a report as resolved.
         */
    }
}