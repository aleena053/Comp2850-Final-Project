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

class TrainerDashboard : Activity() {

    private lateinit var trainerSettings: ImageButton
    private lateinit var messages: Button
    private lateinit var clientEmail: EditText
    private lateinit var addClient: Button
    private lateinit var recyclerClients: RecyclerView
    private lateinit var clientAdapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trainer_dashboard)

        trainerSettings = findViewById(R.id.trainerSettings)
        messages = findViewById(R.id.messages)
        clientEmail = findViewById(R.id.clientEmail)
        addClient = findViewById(R.id.addClient)
        recyclerClients = findViewById(R.id.recyclerClients)

        clientAdapter = ClientAdapter(
            mutableListOf(),
            clientClick = { client ->
                val intent = Intent(this, ClientWorkoutHistory::class.java)
                intent.putExtra("CLIENT_ID", client.userId)
                intent.putExtra("CLIENT_NAME", client.name)
                startActivity(intent)
            },
            removeClick = { client ->
                showRemoveClientDialog(client)
            }
        )

        recyclerClients.layoutManager = LinearLayoutManager(this)
        recyclerClients.adapter = clientAdapter

        trainerSettings.setOnClickListener {
            showSettingsDialog()
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

    private fun showSettingsDialog() {
        val options = arrayOf("Log Out")

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
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

