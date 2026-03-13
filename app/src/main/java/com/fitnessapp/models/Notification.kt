package com.fitnessapp.models

class Notification(
    val notificationID: Int,
    var message: String,
    var type: String,
    var timestamp: String
) {
    fun sendNotification() {
        /*
         Send a notification to a user.
         */
    }

    fun markAsRead() {
        /*
         Mark the notification as read.
         */
    }
}