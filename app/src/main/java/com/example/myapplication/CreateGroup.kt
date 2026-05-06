package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateGroup : Activity() {

    // Back button to return to the previous screen
    private lateinit var backCreateGroup: Button

    // Input field for the group name
    private lateinit var groupName: EditText

    // Input field for the usernames to add to the group
    private lateinit var groupUsernames: EditText

    // Button used to create the group
    private lateinit var createGroupbutton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        // Connects the XML views to the Kotlin variables
        backCreateGroup = findViewById(R.id.backCreateGroup)
        groupName = findViewById(R.id.groupName)
        groupUsernames = findViewById(R.id.groupUsernames)
        createGroupbutton = findViewById(R.id.createGroup)

        // Goes back to the previous screen
        backCreateGroup.setOnClickListener {
            finish()
        }

        // Starts the group creation process
        createGroupbutton.setOnClickListener {
            createGroupClass()
        }
    }

    @Suppress("ReturnCount")
    private fun createGroupClass() {
        // Gets the current logged-in user's id
        val currentUserId = SessionManager(this).getUserId()

        // Gets the group name typed by the user
        val title = groupName.text.toString().trim()

        // Gets the usernames typed by the user
        val usernamesRaw = groupUsernames.text.toString().trim()

        // Stops if the user session is missing
        if (currentUserId == INVALID_ID) {
            showToast(USER_SESSION_NOT_FOUND_MESSAGE)
            return
        }

        // Stops if the group name is empty
        if (title.isEmpty()) {
            showToast(ENTER_GROUP_NAME_MESSAGE)
            return
        }

        // Stops if no usernames were entered
        if (usernamesRaw.isEmpty()) {
            showToast(ENTER_USERNAME_MESSAGE)
            return
        }

        // Splits the usernames by comma, removes spaces, changes them to lowercase,
        // removes blank entries, and removes duplicates
        val usernames = usernamesRaw
            .split(USERNAME_SEPARATOR)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

        // Stops if the final usernames list is empty
        if (usernames.isEmpty()) {
            showToast(ENTER_VALID_USERNAMES_MESSAGE)
            return
        }

        // Creates the request object to send to the backend
        val request = CreateGroupByUsernamesRequest(
            createdByUserId = currentUserId,
            title = title,
            usernames = usernames
        )

        // Sends the request to create the group
        submitCreateGroupRequest(request, title)
    }

    private fun submitCreateGroupRequest(
        request: CreateGroupByUsernamesRequest,
        title: String
    ) {
        // Sends the create-group request to the backend
        RetrofitClient.apiService.createGroupConversationByUsernames(request)
            .enqueue(object : Callback<ConversationResponse> {
                override fun onResponse(
                    call: Call<ConversationResponse>,
                    response: Response<ConversationResponse>
                ) {
                    // If the request worked, continue to the success step
                    if (response.isSuccessful && response.body()?.success == true) {
                        handleCreateGroupSuccess(response, title)
                    } else {
                        // Shows an error if the backend could not create the group
                        showToast(FAILED_TO_CREATE_GROUP_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<ConversationResponse>, t: Throwable) {
                    // Shows an error if the network request fails
                    showToast("Network error: ${t.message}", Toast.LENGTH_LONG)
                }
            })
    }

    private fun handleCreateGroupSuccess(
        response: Response<ConversationResponse>,
        title: String
    ) {
        // Gets the new conversation id from the backend response
        val conversationId = response.body()?.conversationId ?: INVALID_ID

        // Stops if the returned conversation id is invalid
        if (conversationId == INVALID_ID) {
            showToast(INVALID_GROUP_CONVERSATION_MESSAGE)
            return
        }

        // Shows a success message
        showToast(GROUP_CREATED_SUCCESSFULLY_MESSAGE)

        // Opens the new group chat screen
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(CONVERSATION_ID_EXTRA, conversationId)
        intent.putExtra(CONVERSATION_TITLE_EXTRA, title)
        startActivity(intent)
        finish()
    }

    private fun showToast(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        // Shows a short message on the screen
        Toast.makeText(this, message, duration).show()
    }

    companion object {
        private const val INVALID_ID = -1
        private const val USERNAME_SEPARATOR = ","
        private const val CONVERSATION_ID_EXTRA = "CONVERSATION_ID"
        private const val CONVERSATION_TITLE_EXTRA = "CONVERSATION_TITLE"
        private const val USER_SESSION_NOT_FOUND_MESSAGE =
            "User session not found"
        private const val ENTER_GROUP_NAME_MESSAGE =
            "Please enter a group name"
        private const val ENTER_USERNAME_MESSAGE =
            "Please enter at least one username"
        private const val ENTER_VALID_USERNAMES_MESSAGE =
            "Please enter valid usernames"
        private const val INVALID_GROUP_CONVERSATION_MESSAGE =
            "Invalid group conversation"
        private const val GROUP_CREATED_SUCCESSFULLY_MESSAGE =
            "Group created successfully"
        private const val FAILED_TO_CREATE_GROUP_MESSAGE =
            "Failed to create group"
    }
}
