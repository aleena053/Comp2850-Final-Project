package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Message(
    private val currentUserId: Int,
    private val messages: MutableList<MessageItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        // Checks if the message was sent by the current user
        return if (messages[position].senderUserId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Creates a different message layout depending on who sent the message
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Gets the message for the current position
        val message = messages[position]

        // Fills the sent message layout
        if (holder is SentMessageViewHolder) {
            holder.messageTextSent.text = message.messageText
            holder.messageTimeSent.text = formatMessageTime(message.sentAt)

            // Fills the received message layout
        } else if (holder is ReceivedMessageViewHolder) {
            holder.senderNameReceived.text = message.senderName
            holder.messageTextReceived.text = message.messageText
            holder.messageTimeReceived.text = formatMessageTime(message.sentAt)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateData(newMessages: List<MessageItem>) {
        // Stores the old number of messages
        val oldSize = messages.size

        // Removes the old messages from the list
        messages.clear()
        notifyItemRangeRemoved(0, oldSize)

        // Adds the new messages to the list
        messages.addAll(newMessages)
        notifyItemRangeInserted(0, messages.size)
    }

    private fun formatMessageTime(raw: String): String {
        // If the time string is long enough, extract only the time part
        return if (raw.length >= MIN_TIME_STRING_LENGTH) {
            raw.replace(T_SEPARATOR, SPACE_SEPARATOR)
                .substring(TIME_START_INDEX, TIME_END_INDEX)
        } else {
            // If not, return the original value
            raw
        }
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Shows the text of a message sent by the current user
        val messageTextSent: TextView = itemView.findViewById(R.id.messageTextSent)

        // Shows the time of a message sent by the current user
        val messageTimeSent: TextView = itemView.findViewById(R.id.messageTimeSent)
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Shows the name of the sender for a received message
        val senderNameReceived: TextView =
            itemView.findViewById(R.id.senderNameReceived)

        // Shows the text of a received message
        val messageTextReceived: TextView =
            itemView.findViewById(R.id.messageTextReceived)

        // Shows the time of a received message
        val messageTimeReceived: TextView =
            itemView.findViewById(R.id.messageTimeReceived)
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val MIN_TIME_STRING_LENGTH = 16
        private const val TIME_START_INDEX = 11
        private const val TIME_END_INDEX = 16
        private const val T_SEPARATOR = "T"
        private const val SPACE_SEPARATOR = " "
    }
}
