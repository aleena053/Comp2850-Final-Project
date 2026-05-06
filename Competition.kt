@file:Suppress("TooManyFunctions")

package com.example.myapplication

// ... imports ...

class Competition(
    private val context: Context,
    private val competitions: MutableList<CompetitionItem>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<Competition.CompetitionViewHolder>() {

    // finds the text views and buttons in the xml
    class CompetitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val competitionName: TextView = itemView.findViewById(R.id.competitionName)
        val competitionDate: TextView = itemView.findViewById(R.id.competitionDate)
        val competitionLocation: TextView = itemView.findViewById(R.id.competitionLocation)
        val competitionSport: TextView = itemView.findViewById(R.id.competitionSport)
        val competitionEventType: TextView = itemView.findViewById(R.id.competitionEventType)
        val competitionDescription: TextView =
            itemView.findViewById(R.id.competitionDescription)
        val editCompetition: Button = itemView.findViewById(R.id.editCompetition)
        val addResult: Button = itemView.findViewById(R.id.addResult)
        val deleteCompetition: Button = itemView.findViewById(R.id.deleteCompetition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompetitionViewHolder {
        // links the competition layout to the list
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.competition, parent, false)
        return CompetitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompetitionViewHolder, position: Int) {
        val competition = competitions[position]

        // sets text based on the database
        holder.competitionName.text = competition.name
        holder.competitionDate.text = context.getString(
            R.string.competition_date_label,
            formatCompetitionDate(competition.competitionDate)
        )

        holder.competitionLocation.text = context.getString(
            R.string.competition_location_label,
            competition.location ?: NOT_AVAILABLE_TEXT
        )

        // logic for button clicks on each card
        holder.editCompetition.setOnClickListener {
            showEditCompetitionDialog(competition)
        }

        holder.deleteCompetition.setOnClickListener {
            deleteCompetition(competition)
        }

        holder.addResult.setOnClickListener {
            showAddResultDialog(competition)
        }
    }

    override fun getItemCount(): Int = competitions.size

    // refreshes the list for new data or deletes
    fun updateData(newCompetitions: List<CompetitionItem>) {
        val oldSize = competitions.size
        competitions.clear()
        notifyItemRangeRemoved(0, oldSize)
        competitions.addAll(newCompetitions)
        notifyItemRangeInserted(0, competitions.size)
    }

    // sends api request to delete from db
    private fun deleteCompetition(competition: CompetitionItem) {
        RetrofitClient.apiService.deleteCompetition(competition.competitionId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            context,
                            COMPETITION_DELETED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDataChanged() // updates ui
                    } else {
                        val message = response.body()?.message
                            ?: DELETE_COMPETITION_FAILED_MESSAGE
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // opens pop up for results
    private fun showAddResultDialog(competition: CompetitionItem) {
        val layout = createResultDialogLayout(context)
        val etFinishTime = createFinishTimeField(context)
        val etPosition = createPositionField(context)
        val etNotes = createNotesField(context)

        layout.addView(etFinishTime)
        layout.addView(etPosition)
        layout.addView(etNotes)

        AlertDialog.Builder(context)
            .setTitle(ADD_RESULT_TITLE)
            .setView(layout)
            .setPositiveButton(SAVE_TEXT) { _, _ ->
                submitResult(
                    competition = competition,
                    fields = ResultDialogFields(
                        finishTime = etFinishTime,
                        position = etPosition,
                        notes = etNotes
                    )
                )
            }
            .setNegativeButton(CANCEL_TEXT, null)
            .show()
    }

    // sends pop up info to back-end
    private fun submitResult(
        competition: CompetitionItem,
        fields: ResultDialogFields
    ) {
        val userId = SessionManager(context).getUserId()
        val finishTime = convertRaceTimeToSeconds(fields.finishTime.text.toString())
        val position = fields.position.text.toString().trim().toIntOrNull()
        val notes = fields.notes.text.toString().trim()

        // check if time format is valid
        if (finishTime == null || finishTime <= ZERO_DOUBLE) {
            Toast.makeText(
                context,
                INVALID_TIME_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request = CreateCompetitionResultRequest(
            userId = userId,
            competitionId = competition.competitionId,
            finishTime = finishTime,
            position = position,
            notes = notes.ifEmpty { null }
        )

        RetrofitClient.apiService.addCompetitionResult(request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            context,
                            RESULT_ADDED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDataChanged()
                    } else {
                        val message = response.body()?.message ?: ADD_RESULT_FAILED_MESSAGE
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // edit pop up with existing data
    private fun showEditCompetitionDialog(competition: CompetitionItem) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.add_competition, null)

        val fields = CompetitionDialogFields(
            name = dialogView.findViewById(R.id.competitionName),
            location = dialogView.findViewById(R.id.competitionLocation),
            date = dialogView.findViewById(R.id.competitionDate),
            sportSpinner = dialogView.findViewById(R.id.competitionSport),
            eventType = dialogView.findViewById(R.id.competitionEventType),
            description = dialogView.findViewById(R.id.competitionDescription)
        )

        bindCompetitionFields(competition, fields)
        setupSportSpinner(context, fields.sportSpinner, competition.sportName)
        setupDatePicker(context, fields.date)

        AlertDialog.Builder(context)
            .setTitle(EDIT_COMPETITION_TITLE)
            .setView(dialogView)
            .setPositiveButton(SAVE_TEXT) { _, _ ->
                submitCompetitionUpdate(
                    competition = competition,
                    fields = fields
                )
            }
            .setNegativeButton(CANCEL_TEXT, null)
            .show()
    }
}
