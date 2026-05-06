package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class Conversation(
    private val conversations: MutableList<ConversationItem>,
    private val onItemClick: (ConversationItem) -> Unit
) : RecyclerView.Adapter<Conversation.ConversationViewHolder>() {

    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Shows the conversation title
        val conversationTitle: TextView = itemView.findViewById(R.id.conversationTitle)

        // Shows the username if there is one
        val conversationUsername: TextView = itemView.findViewById(R.id.conversationUsername)

        // Shows the latest message in the conversation
        val conversationLastMessage: TextView = itemView.findViewById(R.id.conversationLastMessage)

        // Shows the time of the last message
        val conversationTime: TextView = itemView.findViewById(R.id.conversationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        // Creates the layout for one conversation item in the RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation, parent, false)

        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        // Gets the conversation for the current position
        val conversation = conversations[position]

        // Sets the title shown for the conversation
        holder.conversationTitle.text = when {
            !conversation.title.isNullOrBlank() -> conversation.title
            conversation.conversationType.equals("group", ignoreCase = true) -> "Training Group"
            else -> "Direct Chat"
        }

        // Shows the username if it exists, otherwise hides that TextView
        if (!conversation.username.isNullOrBlank()) {
            holder.conversationUsername.visibility = View.VISIBLE
            holder.conversationUsername.text = holder.itemView.context.getString(
                R.string.username_with_at,
                conversation.username
            )
        } else {
            holder.conversationUsername.visibility = View.GONE
        }

        // Shows the last message, or default text if there are no messages yet
        holder.conversationLastMessage.text =
            conversation.lastMessage ?: "No messages yet"

        // Formats and shows the message time
        holder.conversationTime.text =
            formatConversationTime(conversation.lastMessageTime ?: conversation.createdAt ?: "")

        // Runs the click action when the user taps on a conversation
        holder.itemView.setOnClickListener {
            onItemClick(conversation)
        }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateData(newConversations: List<ConversationItem>) {
        // Stores the old list size before clearing it
        val oldSize = conversations.size

        // Removes the old conversations
        conversations.clear()
        notifyItemRangeRemoved(0, oldSize)

        // Adds the new conversations
        conversations.addAll(newConversations)
        notifyItemRangeInserted(0, conversations.size)
    }

    private fun formatConversationTime(raw: String): String {
        // This is the format we want to show in the app
        val outputs = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK)

        // These are the possible date formats we might receive from the backend
        val possibleInputs = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        )

        // Tries each possible date format until one works
        for (input in possibleInputs) {
            try {
                input.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = input.parse(raw)

                // If the date was parsed successfully, return it in the app format
                if (parsed != null) {
                    return outputs.format(parsed)
                }
            } catch (_: Exception) {
            }
        }

        // If no date format worked, return the original value
        return raw
    }
}
