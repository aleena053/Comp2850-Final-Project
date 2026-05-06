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

class UpcomingCompetitions : Activity() {

    private lateinit var backUpcomingCompetitions: Button
    private lateinit var recyclerAllUpcomingCompetitions: RecyclerView
    private lateinit var competitionAdapter: Competition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upcoming_competitions)

        bindViews()
        setupRecyclerView()
        setupListeners()
        loadCompetitions()
    }

    override fun onResume() {
        super.onResume()
        loadCompetitions()
    }

    private fun bindViews() {
        backUpcomingCompetitions = findViewById(R.id.backUpcomingCompetitions)
        recyclerAllUpcomingCompetitions = findViewById(
            R.id.recyclerAllUpcomingCompetitions
        )
    }

    private fun setupRecyclerView() {
        competitionAdapter = Competition(this, mutableListOf()) {
            loadCompetitions()
        }

        recyclerAllUpcomingCompetitions.layoutManager = LinearLayoutManager(this)
        recyclerAllUpcomingCompetitions.adapter = competitionAdapter
    }

    private fun setupListeners() {
        backUpcomingCompetitions.setOnClickListener {
            finish()
        }
    }

    private fun loadCompetitions() {
        val userId = SessionManager(this).getUserId()

        RetrofitClient.apiService.getCompetitions(userId)
            .enqueue(object : Callback<CompetitionsResponse> {
                override fun onResponse(
                    call: Call<CompetitionsResponse>,
                    response: Response<CompetitionsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val competitions = response.body()?.competitions.orEmpty()
                        competitionAdapter.updateData(competitions)
                    } else {
                        showToast(LOAD_COMPETITIONS_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(
                    call: Call<CompetitionsResponse>,
                    t: Throwable
                ) {
                    showToast("Error: ${t.localizedMessage}", Toast.LENGTH_LONG)
                }
            })
    }

    private fun showToast(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(this, message, duration).show()
    }

    companion object {
        private const val LOAD_COMPETITIONS_FAILED_MESSAGE =
            "Failed to load competitions"
    }
}
