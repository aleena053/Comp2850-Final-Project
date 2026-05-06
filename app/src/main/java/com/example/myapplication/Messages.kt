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

    private lateinit var backMessages: Button
    private lateinit var createGroup: Button
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var conversationAdapter: Conversation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages)

        backMessages = findViewById(R.id.backMessages)
        createGroup = findViewById(R.id.createGroup)
        recyclerMessages = findViewById(R.id.recyclerMessages)

        conversationAdapter = Conversation(mutableListOf()) { conversation ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("CONVERSATION_ID", conversation.conversationId)
            intent.putExtra("CONVERSATION_TITLE", conversation.title ?: defaultConversationTitle(conversation))
            intent.putExtra("CONVERSATION_TYPE", conversation.conversationType)
            startActivity(intent)
        }

        recyclerMessages.layoutManager = LinearLayoutManager(this)
        recyclerMessages.adapter = conversationAdapter

        backMessages.setOnClickListener {
            finish()
        }

        createGroup.setOnClickListener {
            startActivity(Intent(this, CreateGroup::class.java))
        }

        loadConversations()
    }

    override fun onResume() {
        super.onResume()
        loadConversations()
    }

    private fun loadConversations() {
        val userId = SessionManager(this).getUserId()

        if (userId == -1) {
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitClient.apiService.getConversations(userId)
            .enqueue(object : Callback<ConversationsResponse> {
                override fun onResponse(
                    call: Call<ConversationsResponse>,
                    response: Response<ConversationsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val conversations = response.body()?.conversations ?: emptyList()
                        conversationAdapter.updateData(conversations)
                    } else {
                        Toast.makeText(
                            this@Messages,
                            "Failed to load conversations",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ConversationsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@Messages,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun defaultConversationTitle(conversation: ConversationItem): String {
        return if (conversation.conversationType.equals("group", ignoreCase = true)) {
            "Training Group"
        } else {
            "Direct Chat"
        }
    }
}
