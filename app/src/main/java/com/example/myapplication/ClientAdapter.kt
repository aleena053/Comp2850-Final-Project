package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClientAdapter(
    private val clients: MutableList<ClientItem>,
    private val clientClick: (ClientItem) -> Unit,
    private val removeClick: (ClientItem) -> Unit,
    private val messageClick: (ClientItem) -> Unit
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clientName: TextView = itemView.findViewById(R.id.client_name)
        val clientEmail: TextView = itemView.findViewById(R.id.client_email)
        val removeClient: Button = itemView.findViewById(R.id.remove_client)
        val messageClient: Button = itemView.findViewById(R.id.messageClient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]

        holder.clientName.text = client.name
        holder.clientEmail.text = client.email

        holder.itemView.setOnClickListener {
            clientClick(client)
        }

        holder.removeClient.setOnClickListener {
            removeClick(client)
        }

        holder.messageClient.setOnClickListener {
            messageClick(client)
        }
    }

    override fun getItemCount(): Int = clients.size

    fun updateData(newClients: List<ClientItem>) {
        val oldSize = clients.size
        clients.clear()
        notifyItemRangeRemoved(0, oldSize)
        clients.addAll(newClients)
        notifyItemRangeInserted(0, clients.size)
    }
}
