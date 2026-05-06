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

    private lateinit var backCreateGroup: Button
    private lateinit var groupName: EditText
    private lateinit var groupUsernames: EditText
    private lateinit var createGroupbutton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        backCreateGroup = findViewById(R.id.backCreateGroup)
        groupName = findViewById(R.id.groupName)
        groupUsernames = findViewById(R.id.groupUsernames)
        createGroupbutton = findViewById(R.id.createGroup)

        backCreateGroup.setOnClickListener {
            finish()
        }

        createGroupbutton.setOnClickListener {
            createGroupClass()
        }
    }

    @Suppress("ReturnCount")
    private fun createGroupClass() {
        val currentUserId = SessionManager(this).getUserId()
        val title = groupName.text.toString().trim()
        val usernamesRaw = groupUsernames.text.toString().trim()

        if (currentUserId == INVALID_ID) {
            showToast(USER_SESSION_NOT_FOUND_MESSAGE)
            return
        }

        if (title.isEmpty()) {
            showToast(ENTER_GROUP_NAME_MESSAGE)
            return
        }

        if (usernamesRaw.isEmpty()) {
            showToast(ENTER_USERNAME_MESSAGE)
            return
        }

        val usernames = usernamesRaw
            .split(USERNAME_SEPARATOR)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

        if (usernames.isEmpty()) {
            showToast(ENTER_VALID_USERNAMES_MESSAGE)
            return
        }

        val request = CreateGroupByUsernamesRequest(
            createdByUserId = currentUserId,
            title = title,
            usernames = usernames
        )

        submitCreateGroupRequest(request, title)
    }

    private fun submitCreateGroupRequest(
        request: CreateGroupByUsernamesRequest,
        title: String
    ) {
        RetrofitClient.apiService.createGroupConversationByUsernames(request)
            .enqueue(object : Callback<ConversationResponse> {
                override fun onResponse(
                    call: Call<ConversationResponse>,
                    response: Response<ConversationResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        handleCreateGroupSuccess(response, title)
                    } else {
                        showToast(FAILED_TO_CREATE_GROUP_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<ConversationResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}", Toast.LENGTH_LONG)
                }
            })
    }
    private fun handleCreateGroupSuccess(
        response: Response<ConversationResponse>,
        title: String
    ) {
        val conversationId = response.body()?.conversationId ?: INVALID_ID

        if (conversationId == INVALID_ID) {
            showToast(INVALID_GROUP_CONVERSATION_MESSAGE)
            return
        }

        showToast(GROUP_CREATED_SUCCESSFULLY_MESSAGE)

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
