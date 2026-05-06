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

class ChatActivity : Activity() {

    private var conversationId: Int = -1
    private var conversationTitle: String = "Chat"
    private var conversationType: String = "direct"

    private lateinit var backChat: Button
    private lateinit var chatTitle: TextView
    private lateinit var layoutGroupActions: LinearLayout
    private lateinit var viewMembers: Button
    private lateinit var addMember: Button
    private lateinit var leaveGroup: Button
    private lateinit var recyclerChatMessages: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendMessage: Button
    private lateinit var messageAdapter: Message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        conversationId = intent.getIntExtra("CONVERSATION_ID", -1)
        conversationTitle = intent.getStringExtra("CONVERSATION_TITLE") ?: "Chat"
        conversationType = intent.getStringExtra("CONVERSATION_TYPE") ?: "direct"

        backChat = findViewById(R.id.backChat)
        chatTitle = findViewById(R.id.chatTitle)
        layoutGroupActions = findViewById(R.id.layoutGroupActions)
        viewMembers = findViewById(R.id.viewMembers)
        addMember = findViewById(R.id.addMember)
        leaveGroup = findViewById(R.id.leaveGroup)
        recyclerChatMessages = findViewById(R.id.recyclerChatMessages)
        messageInput = findViewById(R.id.messageInput)
        sendMessage = findViewById(R.id.sendMessage)

        if (conversationId == -1) {
            Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        chatTitle.text = conversationTitle

        val currentUserId = SessionManager(this).getUserId()
        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        messageAdapter = Message(currentUserId, mutableListOf())

        recyclerChatMessages.layoutManager = LinearLayoutManager(this)
        recyclerChatMessages.adapter = messageAdapter

        backChat.setOnClickListener {
            finish()
        }

        sendMessage.setOnClickListener {
            sendMessage()
        }

        if (conversationType.equals("group", ignoreCase = true)) {
            layoutGroupActions.visibility = View.VISIBLE

            viewMembers.setOnClickListener {
                showGroupMembers()
            }

            addMember.setOnClickListener {
                showAddMemberDialog()
            }

            leaveGroup.setOnClickListener {
                leaveGroup()
            }
        } else {
            layoutGroupActions.visibility = View.GONE
        }

        loadMessages()
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
    }

    private fun loadMessages() {
        RetrofitClient.apiService.getMessages(conversationId)
            .enqueue(object : Callback<MessagesResponse> {
                override fun onResponse(
                    call: Call<MessagesResponse>,
                    response: Response<MessagesResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val messages = response.body()?.messages ?: emptyList()
                        messageAdapter.updateData(messages)

                        if (messageAdapter.itemCount > 0) {
                            recyclerChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
                        }
                    } else {
                        Toast.makeText(
                            this@ChatActivity,
                            "Failed to load messages",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MessagesResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun sendMessage() {
        val currentUserId = SessionManager(this).getUserId()
        val messageText = messageInput.text.toString().trim()

        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (messageText.isEmpty()) {
            return
        }

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
                    if (response.isSuccessful && response.body()?.success == true) {
                        messageInput.setText("")
                        loadMessages()
                    } else {
                        val message = response.body()?.message ?: "Failed to send message"
                        Toast.makeText(
                            this@ChatActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showAddMemberDialog() {
        val input = EditText(this)
        input.hint = "Enter username"

        AlertDialog.Builder(this)
            .setTitle("Add Member")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val username = input.text.toString().trim().lowercase()
                if (username.isNotEmpty()) {
                    addMember(username)
                } else {
                    Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addMember(username: String) {
        val request = AddMemberByUsernameRequest(username = username)

        RetrofitClient.apiService.addGroupMemberByUsername(conversationId, request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@ChatActivity,
                            "Member added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val message = response.body()?.message ?: "Failed to add member"
                        Toast.makeText(
                            this@ChatActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun leaveGroup() {
        val currentUserId = SessionManager(this).getUserId()

        if (currentUserId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

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
                                val message = response.body()?.message ?: "Failed to leave group"
                                Toast.makeText(
                                    this@ChatActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
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

    private fun showGroupMembers() {
        RetrofitClient.apiService.getGroupMembers(conversationId)
            .enqueue(object : Callback<GroupMembersResponse> {
                override fun onResponse(
                    call: Call<GroupMembersResponse>,
                    response: Response<GroupMembersResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val members = response.body()?.members ?: emptyList()

                        if (members.isEmpty()) {
                            AlertDialog.Builder(this@ChatActivity)
                                .setTitle("Group Members")
                                .setMessage("No members found.")
                                .setPositiveButton("OK", null)
                                .show()
                            return
                        }

                        val memberLines = members.joinToString("\n\n") { member ->
                            val usernameText = if (!member.username.isNullOrBlank()) {
                                "@${member.username}"
                            } else {
                                "@unknown"
                            }

                            "${member.name}\n$usernameText"
                        }

                        AlertDialog.Builder(this@ChatActivity)
                            .setTitle("Group Members")
                            .setMessage(memberLines)
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        val message = response.body()?.message ?: "Failed to load group members"
                        Toast.makeText(
                            this@ChatActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GroupMembersResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
