package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Messages : Activity() {

    // Back button to return to the previous screen
    private lateinit var backMessages: Button

    // Button used to open the create group screen
    private lateinit var createGroup: Button

    // RecyclerView that shows all conversations
    private lateinit var recyclerMessages: RecyclerView

    // Adapter used to show the list of conversations
    private lateinit var conversationAdapter: Conversation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages)

        // Connects the XML views to the Kotlin variables
        backMessages = findViewById(R.id.backMessages)
        createGroup = findViewById(R.id.createGroup)
        recyclerMessages = findViewById(R.id.recyclerMessages)

        // Creates the adapter and sets what happens when a conversation is clicked
        conversationAdapter = Conversation(mutableListOf()) { conversation ->
            val intent = Intent(this, ChatActivity::class.java)

            // Sends the conversation id, title, and type to the chat screen
            intent.putExtra("CONVERSATION_ID", conversation.conversationId)
            intent.putExtra(
                "CONVERSATION_TITLE",
                conversation.title ?: defaultConversationTitle(conversation)
            )
            intent.putExtra("CONVERSATION_TYPE", conversation.conversationType)

            // Opens the chat screen
            startActivity(intent)
        }

        // Sets up the RecyclerView to show conversations in a vertical list
        recyclerMessages.layoutManager = LinearLayoutManager(this)
        recyclerMessages.adapter = conversationAdapter

        // Goes back to the previous screen
        backMessages.setOnClickListener {
            finish()
        }

        // Opens the create group screen
        createGroup.setOnClickListener {
            startActivity(Intent(this, CreateGroup::class.java))
        }

        // Loads the conversations when the screen first opens
        loadConversations()
    }

    override fun onResume() {
        super.onResume()

        // Reloads the conversations when returning to this screen
        loadConversations()
    }

    private fun loadConversations() {
        // Gets the current logged-in user's id
        val userId = SessionManager(this).getUserId()

        // Stops if the user session cannot be found
        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Sends a request to the backend to get this user's conversations
        RetrofitClient.apiService.getConversations(userId)
            .enqueue(object : Callback<ConversationsResponse> {
                override fun onResponse(
                    call: Call<ConversationsResponse>,
                    response: Response<ConversationsResponse>
                ) {
                    // If the request worked, update the list of conversations
                    if (response.isSuccessful && response.body()?.success == true) {
                        val conversations = response.body()?.conversations ?: emptyList()
                        conversationAdapter.updateData(conversations)
                    } else {
                        // Shows an error if the conversations could not be loaded
                        Toast.makeText(
                            this@Messages,
                            "Failed to load conversations",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ConversationsResponse>, t: Throwable) {
                    // Shows an error if the network request fails
                    Toast.makeText(
                        this@Messages,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun defaultConversationTitle(conversation: ConversationItem): String {
        // Returns a default title if the conversation title is missing
        return if (conversation.conversationType.equals("group", ignoreCase = true)) {
            "Training Group"
        } else {
            "Direct Chat"
        }
    }
}
