package com.fitnessapp.models

class Group(
    val groupID: Int,
    var name: String,
    var description: String,
    var createdDate: String
) {
    fun addMember() {
        /*
         Add a user to the group.
         */
    }

    fun removeMember() {
        /*
         Remove a user from the group.
         */
    }

    fun deleteGroup() {
        /*
         Delete the group.
         */
    }
}