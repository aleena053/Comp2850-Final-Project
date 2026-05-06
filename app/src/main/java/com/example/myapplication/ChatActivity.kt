package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Screen used to view and send messages in a conversation
class ChatActivity : Activity() {

    // Stores the id of the current conversation
    private var conversationId: Int = -1

    // Stores the title shown at the top of the chat screen
    private var conversationTitle: String = "Chat"

    // Stores whether this is a direct chat or a group chat
    private var conversationType: String = "direct"

    // Back button to leave the chat screen
    private lateinit var backChat: Button

    // TextView that shows the chat title
    private lateinit var chatTitle: TextView

    // Layout that contains group-only actions
    private lateinit var layoutGroupActions: LinearLayout

    // Button to view group members
    private lateinit var viewMembers: Button

    // Button to add a new group member
    private lateinit var addMember: Button

    // Button to leave the group
    private lateinit var leaveGroup: Button

    // RecyclerView that displays the chat messages
    private lateinit var recyclerChatMessages: RecyclerView

    // Input box where the user types a message
    private lateinit var messageInput: EditText

    // Button used to send a message
    private lateinit var sendMessage: Button

    // Adapter used to display messages in the RecyclerView
    private lateinit var messageAdapter: Message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        // Gets conversation details sent from the previous screen
        conversationId = intent.getIntExtra("CONVERSATION_ID", -1)
        conversationTitle = intent.getStringExtra("CONVERSATION_TITLE") ?: "Chat"
        conversationType = intent.getStringExtra("CONVERSATION_TYPE") ?: "direct"

        // Connects XML views to Kotlin variables
        backChat = findViewById(R.id.backChat)
        chatTitle = findViewById(R.id.chatTitle)
        layoutGroupActions = findViewById(R.id.layoutGroupActions)
        viewMembers = findViewById(R.id.viewMembers)
        addMember = findViewById(R.id.addMember)
        leaveGroup = findViewById(R.id.leaveGroup)
        recyclerChatMessages = findViewById(R.id.recyclerChatMessages)
        messageInput = findViewById(R.id.messageInput)
        sendMessage = findViewById(R.id.sendMessage)

        // Stops the screen if the conversation id is missing
        if (conversationId == -1) {
            Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Shows the conversation title at the top of the screen
        chatTitle.text = conversationTitle

        // Gets the currently logged-in user's id from the saved session
        val currentUserId = SessionManager(this).getUserId()

        // Stops the screen if no valid user session is found
        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Creates the message adapter and gives it the current user's id
        messageAdapter = Message(currentUserId, mutableListOf())

        // Sets up the RecyclerView to display messages in a vertical list
        recyclerChatMessages.layoutManager = LinearLayoutManager(this)
        recyclerChatMessages.adapter = messageAdapter

        // Goes back to the previous screen
        backChat.setOnClickListener {
            finish()
        }

        // Sends the typed message when the button is pressed
        sendMessage.setOnClickListener {
            sendMessage()
        }

        // Shows extra buttons only if this is a group conversation
        if (conversationType.equals("group", ignoreCase = true)) {
            layoutGroupActions.visibility = View.VISIBLE

            // Opens a dialog showing all group members
            viewMembers.setOnClickListener {
                showGroupMembers()
            }

            // Opens a dialog to add a member by username
            addMember.setOnClickListener {
                showAddMemberDialog()
            }

            // Lets the current user leave the group
            leaveGroup.setOnClickListener {
                leaveGroup()
            }
        } else {
            // Hides group-only buttons for direct chats
            layoutGroupActions.visibility = View.GONE
        }

        // Loads the messages when the screen first opens
        loadMessages()
    }

    override fun onResume() {
        super.onResume()

        // Reloads messages when returning to this screen
        loadMessages()
    }

    // Loads all messages for this conversation from the backend
    private fun loadMessages() {
        RetrofitClient.apiService.getMessages(conversationId)
            .enqueue(object : Callback<MessagesResponse> {
                override fun onResponse(
                    call: Call<MessagesResponse>,
                    response: Response<MessagesResponse>
                ) {
                    // If the request worked, update the RecyclerView with the messages
                    if (response.isSuccessful && response.body()?.success == true) {
                        val messages = response.body()?.messages ?: emptyList()
                        messageAdapter.updateData(messages)

                        // Scrolls to the last message so the newest message is visible
                        if (messageAdapter.itemCount > 0) {
                            recyclerChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
                        }
                    } else {
                        // Shows an error if messages could not be loaded
                        Toast.makeText(
                            this@ChatActivity,
                            "Failed to load messages",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MessagesResponse>, t: Throwable) {
                    // Shows an error if the network request fails
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // Sends the message typed by the user to the backend
    private fun sendMessage() {
        val currentUserId = SessionManager(this).getUserId()
        val messageText = messageInput.text.toString().trim()

        // Stops if the user session is missing
        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Does nothing if the user tries to send an empty message
        if (messageText.isEmpty()) {
            return
        }

        // Creates the request object that will be sent to the backend
        val request = SendMessageRequest(
            conversationId = conversationId,
            senderUserId = currentUserId,
            messageText = messageText
        )

        RetrofitClient.apiService.sendMessage(request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    // If the message was sent successfully, clear the input and reload messages
                    if (response.isSuccessful && response.body()?.success == true) {
                        messageInput.setText("")
                        loadMessages()
                    } else {
                        // Shows an error message if sending failed
                        val message = response.body()?.message ?: "Failed to send message"
                        Toast.makeText(
                            this@ChatActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    // Shows an error if the network request fails
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // Opens a dialog where the user can type a username to add to the group
    private fun showAddMemberDialog() {
        val input = EditText(this)
        input.hint = "Enter username"

        AlertDialog.Builder(this)
            .setTitle("Add Member")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val username = input.text.toString().trim().lowercase()

                // If a username was entered, try to add that user to the group
                if (username.isNotEmpty()) {
                    addMember(username)
                } else {
                    // Shows an error if the input box is empty
                    Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Sends a request to add a member to the group by username
    private fun addMember(username: String) {
        val request = AddMemberByUsernameRequest(username = username)

        RetrofitClient.apiService.addGroupMemberByUsername(conversationId, request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    // Shows success message if the member was added
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@ChatActivity,
                            "Member added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Shows backend error if the member could not be added
                        val message = response.body()?.message ?: "Failed to add member"
                        Toast.makeText(
                            this@ChatActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    // Shows an error if the network request fails
                    Toast.makeText(
                        this@ChatActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // Lets the current user leave the group conversation
    private fun leaveGroup() {
        val currentUserId = SessionManager(this).getUserId()

        // Stops if the user session is missing
        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Shows a confirmation dialog before leaving the group
        AlertDialog.Builder(this)
            .setTitle("Leave Group")
            .setMessage("Are you sure you want to leave this group?")
            .setPositiveButton("Leave") { _, _ ->
                val request = LeaveGroupRequest(userId = currentUserId)

                RetrofitClient.apiService.leaveGroup(conversationId, request)
                    .enqueue(object : Callback<BasicApiResponse> {
                        override fun onResponse(
                            call: Call<BasicApiResponse>,
                            response: Response<BasicApiResponse>
                        ) {
                            // If leaving worked, return to the messages screen
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(
                                    this@ChatActivity,
                                    "You left the group",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this@ChatActivity, Messages::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                // Shows backend error if leaving failed
                                val message = response.body()?.message ?: "Failed to leave group"
                                Toast.makeText(
                                    this@ChatActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                            // Shows an error if the network request fails
                            Toast.makeText(
                                this@ChatActivity,
                                "Network error: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Loads and shows the members of the group conversation
    private fun showGroupMembers() {
        RetrofitClient.apiService.getGroupMembers(conversationId)
            .enqueue(object : Callback<GroupMembersResponse> {
                override fun onResponse(
                    call: Call<GroupMembersResponse>,
                    response: Response<GroupMembersResponse>
                ) {
                    // If the request worked, get the members list
                    if (response.isSuccessful && response.body()?.success == true) {
                        val members = response.body()?.members ?: emptyList()

                        // Shows a simple dialog if no members were found
                        if (members.isEmpty()) {
                            AlertDialog.Builder(this@ChatActivity)
                                .setTitle("Group Members")
                                .setMessage("No members found.")
                                .setPositiveButton("OK", null)
                                .show()
                            return
                        }

                        // Builds the text that will be shown in the dialog
                        val memberLines = members.joinToString("\n\n") { member ->
                            val usernameText = if (!member.username.isNullOrBlank()) {
                                "@${member.username}"
                            } else {
                                "@unknown"
                            }

                            "${member.name}\n$usernameText"
                        }

                        // Shows the list of members in a dialog box
                        AlertDialog.Builder(this@ChatActivity)
                            .setTitle("Group Members")
                            .setMessage(memberLines)
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        // Shows backend error if members could not be loaded
                        val message = response.body()?.message ?: "Failed to load group members"
                        Toast.makeText(
                            this@ChatActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GroupMembersResponse>, t: Throwable) {
                    // Shows an error if the network request fails
                    Toast.makeText(
                        this@ChatActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
