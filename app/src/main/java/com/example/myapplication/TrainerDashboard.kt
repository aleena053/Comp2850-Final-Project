package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//New screen called TrainerDashboard
class TrainerDashboard : Activity() {

    private lateinit var trainerSettings: ImageButton
    private lateinit var messages: Button
    private lateinit var clientEmail: EditText
    private lateinit var addClient: Button
    private lateinit var recyclerClients: RecyclerView
    private lateinit var clientAdapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        //calls original ANDROID onCreate() function
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trainer_dashboard)

        trainerSettings = findViewById(R.id.trainerSettings)
        messages = findViewById(R.id.messages)
        clientEmail = findViewById(R.id.clientEmail)
        addClient = findViewById(R.id.addClient)
        recyclerClients = findViewById(R.id.recyclerClients)

        //Adapter controls the client list
        clientAdapter = ClientAdapter(
            mutableListOf(),
            //callback function, defines what happens when trainer clicks on a client row
            clientClick = { client ->
                //preparing to open ClientWorkoutHistory screen
                val intent = Intent(this, ClientWorkoutHistory::class.java)
                //send ID and Name to next screen
                intent.putExtra("CLIENT_ID", client.userId)
                intent.putExtra("CLIENT_NAME", client.name)
                //opens the new screen
                startActivity(intent)
            },
            removeClick = { client ->
                showRemoveClientDialog(client)
            },
            messageClick = { client ->
                openDirectChatWithClient(client)
            }
        )

        recyclerClients.layoutManager = LinearLayoutManager(this)
        //use ClientAdapter to display client inside the list
        recyclerClients.adapter = clientAdapter

        trainerSettings.setOnClickListener {
            showSettingsDialog()
        }

        messages.setOnClickListener {
            startActivity(Intent(this, Messages::class.java))
        }

        addClient.setOnClickListener {
            addClient()
        }

        loadClients()
    }

    override fun onResume() {
        super.onResume()
        loadClients()
    }

    private fun addClient() {
        val sessionManager = SessionManager(this)
        //gets logged-in trainer ID
        val trainerId = sessionManager.getUserId()
        val clientEmail = clientEmail.text.toString().trim()

        if (trainerId == -1) {
            Toast.makeText(this, "Trainer session not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (clientEmail.isEmpty()) {
            Toast.makeText(this, "Please enter client email", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AddClientRequest(
            trainerId = trainerId,
            clientEmail = clientEmail
        )
        //send trainer ID and client email to backend to add client
        RetrofitClient.apiService.addClient(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@TrainerDashboard, "Client added", Toast.LENGTH_SHORT).show()
                    this@TrainerDashboard.clientEmail.text.clear()
                    loadClients()
                } else {
                    val message = response.body()?.message ?: "Failed to add client"
                    Toast.makeText(this@TrainerDashboard, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(
                    this@TrainerDashboard,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    //load all clients linked to the trainer
    private fun loadClients() {
        val sessionManager = SessionManager(this)
        val trainerId = sessionManager.getUserId()

        if (trainerId == -1) {
            Toast.makeText(this, "Trainer session not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.getTrainerClients(trainerId)
            .enqueue(object : Callback<ClientListResponse> {
                override fun onResponse(
                    call: Call<ClientListResponse>,
                    response: Response<ClientListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val clients = response.body()?.clients ?: emptyList()
                        clientAdapter.updateData(clients)

                        if (clients.isEmpty()) {
                            Toast.makeText(
                                this@TrainerDashboard,
                                "No clients added yet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@TrainerDashboard,
                            "Failed to load clients",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                    Toast.makeText(
                        this@TrainerDashboard,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showRemoveClientDialog(client: ClientItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove Client")
            .setMessage("Remove ${client.name} from your client list?")
            .setPositiveButton("Remove") { _, _ ->
                removeClient(client)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //Removes a client
    private fun removeClient(client: ClientItem) {
        val sessionManager = SessionManager(this)
        val trainerId = sessionManager.getUserId()

        if (trainerId == -1) {
            Toast.makeText(this, "Trainer session not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.removeClient(trainerId, client.userId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@TrainerDashboard, "Client removed", Toast.LENGTH_SHORT).show()
                        loadClients()
                    } else {
                        Toast.makeText(this@TrainerDashboard, "Failed to remove client", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@TrainerDashboard,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    //1v1 chat with selected client
    private fun openDirectChatWithClient(client: ClientItem) {
        val trainerId = SessionManager(this).getUserId()

        if (trainerId == -1) {
            Toast.makeText(this, "Trainer session not found", Toast.LENGTH_SHORT).show()
            return
        }

        //Creates a request object for a direct conversation, sent to backend
        val request = CreateDirectConversationRequest(
            user1Id = trainerId,
            user2Id = client.userId
        )
        //Calls backend, avoid duplicate chats
        RetrofitClient.apiService.createOrGetDirectConversation(request)
            .enqueue(object : Callback<ConversationResponse> {
                override fun onResponse(
                    call: Call<ConversationResponse>,
                    response: Response<ConversationResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val conversationId = response.body()?.conversationId ?: -1

                        if (conversationId == -1) {
                            Toast.makeText(
                                this@TrainerDashboard,
                                "Invalid conversation",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        val intent = Intent(this@TrainerDashboard, ChatActivity::class.java)
                        intent.putExtra("CONVERSATION_ID", conversationId)
                        intent.putExtra("CONVERSATION_TITLE", client.name)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@TrainerDashboard,
                            "Failed to open chat",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ConversationResponse>, t: Throwable) {
                    Toast.makeText(
                        this@TrainerDashboard,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    //Shows setting pop-up
    private fun showSettingsDialog() {
        val options = arrayOf("Log Out")

        AlertDialog.Builder(this)
            .setTitle("Settings")
            //tells which option was clicked
            .setItems(options) { _, which ->
                //cleaner
                when (which) {
                    0 -> {
                        val sessionManager = SessionManager(this)
                        sessionManager.clearSession()

                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
}

