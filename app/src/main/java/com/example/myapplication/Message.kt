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
        return if (messages[position].senderUserId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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
        val message = messages[position]

        if (holder is SentMessageViewHolder) {
            holder.messageTextSent.text = message.messageText
            holder.messageTimeSent.text = formatMessageTime(message.sentAt)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.senderNameReceived.text = message.senderName
            holder.messageTextReceived.text = message.messageText
            holder.messageTimeReceived.text = formatMessageTime(message.sentAt)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateData(newMessages: List<MessageItem>) {
        val oldSize = messages.size
        messages.clear()
        notifyItemRangeRemoved(0, oldSize)
        messages.addAll(newMessages)
        notifyItemRangeInserted(0, messages.size)
    }

    private fun formatMessageTime(raw: String): String {
        return if (raw.length >= MIN_TIME_STRING_LENGTH) {
            raw.replace(T_SEPARATOR, SPACE_SEPARATOR)
                .substring(TIME_START_INDEX, TIME_END_INDEX)
        } else {
            raw
        }
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextSent: TextView = itemView.findViewById(R.id.messageTextSent)
        val messageTimeSent: TextView = itemView.findViewById(R.id.messageTimeSent)
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderNameReceived: TextView =
            itemView.findViewById(R.id.senderNameReceived)
        val messageTextReceived: TextView =
            itemView.findViewById(R.id.messageTextReceived)
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
