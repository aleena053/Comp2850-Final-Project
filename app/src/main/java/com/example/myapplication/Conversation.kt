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
        val conversationTitle: TextView = itemView.findViewById(R.id.conversationTitle)
        val conversationUsername: TextView = itemView.findViewById(R.id.conversationUsername)
        val conversationLastMessage: TextView = itemView.findViewById(R.id.conversationLastMessage)
        val conversationTime: TextView = itemView.findViewById(R.id.conversationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]

        holder.conversationTitle.text = when {
            !conversation.title.isNullOrBlank() -> conversation.title
            conversation.conversationType.equals("group", ignoreCase = true) -> "Training Group"
            else -> "Direct Chat"
        }

        if (!conversation.username.isNullOrBlank()) {
            holder.conversationUsername.visibility = View.VISIBLE
            holder.conversationUsername.text = holder.itemView.context.getString(
                R.string.username_with_at,
                conversation.username
            )
        } else {
            holder.conversationUsername.visibility = View.GONE
        }

        holder.conversationLastMessage.text =
            conversation.lastMessage ?: "No messages yet"

        holder.conversationTime.text =
            formatConversationTime(conversation.lastMessageTime ?: conversation.createdAt ?: "")

        holder.itemView.setOnClickListener {
            onItemClick(conversation)
        }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateData(newConversations: List<ConversationItem>) {
        val oldSize = conversations.size
        conversations.clear()
        notifyItemRangeRemoved(0, oldSize)
        conversations.addAll(newConversations)
        notifyItemRangeInserted(0, conversations.size)
    }

    private fun formatConversationTime(raw: String): String {
        val outputs = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK)

        val possibleInputs = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        )

        for (input in possibleInputs) {
            try {
                input.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = input.parse(raw)
                if (parsed != null) {
                    return outputs.format(parsed)
                }
            } catch (_: Exception) {
            }
        }

        return raw
    }
}
