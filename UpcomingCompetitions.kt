package com.example.myapplication

// imports

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

    // links xml views to code
    private fun bindViews() {
        backUpcomingCompetitions = findViewById(R.id.backUpcomingCompetitions)
        recyclerAllUpcomingCompetitions = findViewById(
            R.id.recyclerAllUpcomingCompetitions
        )
    }

    // sets up list and adapter
    private fun setupRecyclerView() {
        competitionAdapter = Competition(this, mutableListOf()) {
            loadCompetitions()
        }

        recyclerAllUpcomingCompetitions.layoutManager = LinearLayoutManager(this)
        recyclerAllUpcomingCompetitions.adapter = competitionAdapter
    }

    // handles back button click
    private fun setupListeners() {
        backUpcomingCompetitions.setOnClickListener {
            finish()
        }
    }

    // gets competition data from api
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
                    showToast("error: ${t.localizedMessage}", Toast.LENGTH_LONG)
                }
            })
    }

    // helper for alerts
    private fun showToast(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(this, message, duration).show()
    }

    companion object {
        private const val LOAD_COMPETITIONS_FAILED_MESSAGE =
            "failed to load competitions"
    }
}
