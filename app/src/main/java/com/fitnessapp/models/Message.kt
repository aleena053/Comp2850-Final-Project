package com.fitnessapp.models

class Message(
    val messageID: Int,
    var content: String,
    var timestamp: String
) {
    fun sendMessage() {
        /*
         Send the message to its intended recipient or group.
         */
    }

    fun editMessage() {
        /*
         Edit the contents of an existing message.
         */
    }

    fun deleteMessage() {
        /*
         Delete the message.
         */
    }
}