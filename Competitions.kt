@file:Suppress("LongMethod")

package com.example.myapplication

// imports

class Competitions : Activity() {

    private lateinit var backCompetitions: Button
    private lateinit var addCompetition: Button
    private lateinit var viewCalendar: Button
    private lateinit var layoutUpcomingHeader: LinearLayout
    private lateinit var layoutPastResultsHeader: LinearLayout
    private lateinit var recyclerUpcomingCompetitions: RecyclerView
    private lateinit var recyclerCompetitionResults: RecyclerView
    private lateinit var competitionAdapter: Competition
    private lateinit var resultAdapter: CompetitionResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.competitions)

        // inits ui elements
        backCompetitions = findViewById(R.id.backCompetitions)
        addCompetition = findViewById(R.id.addCompetition)
        viewCalendar = findViewById(R.id.viewCalendar)
        layoutUpcomingHeader = findViewById(R.id.layoutUpcomingHeader)
        layoutPastResultsHeader = findViewById(R.id.layoutPastResultsHeader)
        recyclerUpcomingCompetitions = findViewById(R.id.recyclerUpcomingCompetitions)
        recyclerCompetitionResults = findViewById(R.id.recyclerCompetitionResults)

        // sets up upcoming list
        competitionAdapter = Competition(this, mutableListOf()) {
            loadCompetitions()
            loadResults()
        }

        // sets up results list
        resultAdapter = CompetitionResult(this, mutableListOf()) {
            loadCompetitions()
            loadResults()
        }
        
        recyclerUpcomingCompetitions.layoutManager = LinearLayoutManager(this)
        recyclerUpcomingCompetitions.adapter = competitionAdapter
        recyclerUpcomingCompetitions.isNestedScrollingEnabled = false

        recyclerCompetitionResults.layoutManager = LinearLayoutManager(this)
        recyclerCompetitionResults.adapter = resultAdapter
        recyclerCompetitionResults.isNestedScrollingEnabled = false

        // navigation click logic
        backCompetitions.setOnClickListener { finish() }
        addCompetition.setOnClickListener { showAddCompetitionDialog() }
        viewCalendar.setOnClickListener {
            startActivity(Intent(this, CompetitionCalendar::class.java))
        }
        layoutUpcomingHeader.setOnClickListener {
            startActivity(Intent(this, UpcomingCompetitions::class.java))
        }
        layoutPastResultsHeader.setOnClickListener {
            startActivity(Intent(this, PastResults::class.java))
        }

        loadCompetitions()
        loadResults()
    }

    override fun onResume() {
        super.onResume()
        loadCompetitions()
        loadResults()
    }

    // fetches upcoming data summary
    private fun loadCompetitions() {
        val userId = SessionManager(this).getUserId()

        RetrofitClient.apiService.getCompetitions(userId)
            .enqueue(object : Callback<CompetitionsResponse> {
                override fun onResponse(
                    call: Call<CompetitionsResponse>,
                    response: Response<CompetitionsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val summaryList = response.body()!!.competitions.take(SUMMARY_LIMIT)
                        competitionAdapter.updateData(summaryList)
                    } else {
                        Toast.makeText(this@Competitions, LOAD_COMPETITIONS_FAILED_MESSAGE, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CompetitionsResponse>, t: Throwable) {
                    Toast.makeText(this@Competitions, "error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
