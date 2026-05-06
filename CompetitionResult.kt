@file:Suppress("TooManyFunctions")

package com.example.myapplication

// imports

class CompetitionResult(
    private val context: Context,
    private val results: MutableList<CompetitionResultItem>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<CompetitionResult.ResultViewHolder>() {

    // finds text and buttons in xml
    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultCompetitionName: TextView = itemView.findViewById(R.id.resultCompetitionName)
        val resultCompetitionDate: TextView = itemView.findViewById(R.id.resultCompetitionDate)
        val resultFinishTime: TextView = itemView.findViewById(R.id.resultFinishTime)
        val resultPosition: TextView = itemView.findViewById(R.id.resultPosition)
        val resultNotes: TextView = itemView.findViewById(R.id.resultNotes)
        val resultPbBadge: TextView = itemView.findViewById(R.id.resultPbBadge)
        val editResult: Button = itemView.findViewById(R.id.editResult)
        val deleteResult: Button = itemView.findViewById(R.id.deleteResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        // links the result layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.competition_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]

        // sets text from the db
        holder.resultCompetitionName.text = context.getString(R.string.result_competition_title, result.name, result.eventType)
        holder.resultCompetitionDate.text = context.getString(R.string.result_date_label, formatCompetitionDate(result.competitionDate))
        holder.resultFinishTime.text = context.getString(R.string.result_finish_time_label, formatRaceTime(result.finishTime))
        holder.resultPosition.text = context.getString(R.string.result_position_label, result.position ?: NOT_AVAILABLE_TEXT)
        holder.resultNotes.text = context.getString(R.string.result_notes_label, result.notes ?: NONE_TEXT)
        
        // personal best badge logic
        holder.resultPbBadge.visibility = if (result.isPersonalBest) View.VISIBLE else View.GONE

        // button click listeners
        holder.editResult.setOnClickListener { showEditResultDialog(result) }
        holder.deleteResult.setOnClickListener { deleteResult(result) }
    }

    override fun getItemCount(): Int = results.size

    // refreshes results data
    fun updateData(newResults: List<CompetitionResultItem>) {
        val oldSize = results.size
        results.clear()
        notifyItemRangeRemoved(0, oldSize)
        results.addAll(newResults)
        notifyItemRangeInserted(0, results.size)
    }

    // sends api request to delete
    private fun deleteResult(result: CompetitionResultItem) {
        RetrofitClient.apiService.deleteCompetitionResult(result.resultId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(call: Call<BasicApiResponse>, response: Response<BasicApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, RESULT_DELETED_MESSAGE, Toast.LENGTH_SHORT).show()
                        onDataChanged()
                    } else {
                        val message = response.body()?.message ?: DELETE_FAILED_MESSAGE
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(context, "error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
