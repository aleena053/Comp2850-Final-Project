package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PastResults : Activity() {

    private lateinit var backPastResults: Button
    private lateinit var recyclerAllPastResults: RecyclerView
    private lateinit var resultAdapter: CompetitionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.past_results)

        // links views and sets up list
        backPastResults = findViewById(R.id.backPastResults)
        recyclerAllPastResults = findViewById(R.id.recyclerAllPastResults)

        resultAdapter = CompetitionResult(this, mutableListOf()) {
            loadResults()
        }

        recyclerAllPastResults.layoutManager = LinearLayoutManager(this)
        recyclerAllPastResults.adapter = resultAdapter

        // handles back click
        backPastResults.setOnClickListener {
            finish()
        }

        loadResults()
    }

    override fun onResume() {
        super.onResume()
        loadResults()
    }

    // gets results from back-end
    private fun loadResults() {
        val userId = SessionManager(this).getUserId()

        RetrofitClient.apiService.getCompetitionResults(userId)
            .enqueue(object : Callback<CompetitionResultsResponse> {
                override fun onResponse(
                    call: Call<CompetitionResultsResponse>,
                    response: Response<CompetitionResultsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        resultAdapter.updateData(response.body()!!.results)
                    } else {
                        Toast.makeText(this@PastResults, "Failed to load results", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CompetitionResultsResponse>, t: Throwable) {
                    Toast.makeText(this@PastResults, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
