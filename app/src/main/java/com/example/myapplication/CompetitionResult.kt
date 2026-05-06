@file:Suppress("TooManyFunctions")

package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class CompetitionResult(
    private val context: Context,
    private val results: MutableList<CompetitionResultItem>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<CompetitionResult.ResultViewHolder>() {

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultCompetitionName: TextView =
            itemView.findViewById(R.id.resultCompetitionName)
        val resultCompetitionDate: TextView =
            itemView.findViewById(R.id.resultCompetitionDate)
        val resultFinishTime: TextView =
            itemView.findViewById(R.id.resultFinishTime)
        val resultPosition: TextView =
            itemView.findViewById(R.id.resultPosition)
        val resultNotes: TextView =
            itemView.findViewById(R.id.resultNotes)
        val resultPbBadge: TextView =
            itemView.findViewById(R.id.resultPbBadge)
        val editResult: Button =
            itemView.findViewById(R.id.editResult)
        val deleteResult: Button =
            itemView.findViewById(R.id.deleteResult)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.competition_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]

        holder.resultCompetitionName.text = context.getString(
            R.string.result_competition_title,
            result.name,
            result.eventType
        )

        holder.resultCompetitionDate.text = context.getString(
            R.string.result_date_label,
            formatCompetitionDate(result.competitionDate)
        )

        holder.resultFinishTime.text = context.getString(
            R.string.result_finish_time_label,
            formatRaceTime(result.finishTime)
        )

        holder.resultPosition.text = context.getString(
            R.string.result_position_label,
            result.position ?: NOT_AVAILABLE_TEXT
        )

        holder.resultNotes.text = context.getString(
            R.string.result_notes_label,
            result.notes ?: NONE_TEXT
        )
        holder.resultPbBadge.visibility =
            if (result.isPersonalBest) View.VISIBLE else View.GONE

        holder.editResult.setOnClickListener {
            showEditResultDialog(result)
        }

        holder.deleteResult.setOnClickListener {
            deleteResult(result)
        }
    }

    override fun getItemCount(): Int = results.size

    fun updateData(newResults: List<CompetitionResultItem>) {
        val oldSize = results.size
        results.clear()
        notifyItemRangeRemoved(0, oldSize)
        results.addAll(newResults)
        notifyItemRangeInserted(0, results.size)
    }
    private fun deleteResult(result: CompetitionResultItem) {
        RetrofitClient.apiService.deleteCompetitionResult(result.resultId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            context,
                            RESULT_DELETED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDataChanged()
                    } else {
                        val message = response.body()?.message
                            ?: DELETE_FAILED_MESSAGE
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    private fun showEditResultDialog(result: CompetitionResultItem) {
        val layout = createEditDialogLayout(context)
        val etFinishTime = createFinishTimeField(context, result)
        val etPosition = createPositionField(context, result)
        val etNotes = createNotesField(context, result)

        layout.addView(etFinishTime)
        layout.addView(etPosition)
        layout.addView(etNotes)

        AlertDialog.Builder(context)
            .setTitle(EDIT_RESULT_TITLE)
            .setView(layout)
            .setPositiveButton(SAVE_TEXT) { _, _ ->
                submitEditedResult(result, etFinishTime, etPosition, etNotes)
            }
            .setNegativeButton(CANCEL_TEXT, null)
            .show()
    }
    private fun submitEditedResult(
        result: CompetitionResultItem,
        etFinishTime: EditText,
        etPosition: EditText,
        etNotes: EditText
    ) {
        val finishTime = convertRaceTimeToSeconds(etFinishTime.text.toString())
        val position = etPosition.text.toString().trim().toIntOrNull()
        val notes = etNotes.text.toString().trim()

        if (finishTime == null || finishTime <= ZERO_DOUBLE) {
            Toast.makeText(
                context,
                INVALID_TIME_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request = UpdateCompetitionResultRequest(
            finishTime = finishTime,
            position = position,
            notes = notes.ifEmpty { null }
        )

        RetrofitClient.apiService.updateCompetitionResult(result.resultId, request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            context,
                            RESULT_UPDATED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDataChanged()
                    } else {
                        val message = response.body()?.message
                            ?: UPDATE_FAILED_MESSAGE
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    private fun formatRaceTime(secondsValue: Double?): String {
        if (secondsValue == null) {
            return EMPTY_TEXT
        }

        val totalSeconds = secondsValue.toInt()
        val hours = totalSeconds / SECONDS_IN_HOUR
        val minutes = (totalSeconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
        val seconds = totalSeconds % SECONDS_IN_MINUTE

        return if (hours > ZERO_INT) {
            String.format(
                Locale.getDefault(),
                HOURS_TIME_FORMAT,
                hours,
                minutes,
                seconds
            )
        } else {
            String.format(
                Locale.getDefault(),
                MINUTES_TIME_FORMAT,
                minutes,
                seconds
            )
        }
    }
    private fun formatCompetitionDate(rawDate: String?): String {
        return if (rawDate.isNullOrBlank()) {
            EMPTY_TEXT
        } else {
            convertFormattedCompetitionDate(rawDate)
        }
    }
}
private fun createEditDialogLayout(context: Context): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(
            DIALOG_PADDING_START,
            DIALOG_PADDING_TOP,
            DIALOG_PADDING_END,
            DIALOG_PADDING_BOTTOM
        )
    }
}
private fun createFinishTimeField(
    context: Context,
    result: CompetitionResultItem
): EditText {
    return EditText(context).apply {
        hint = FINISH_TIME_HINT
        inputType = InputType.TYPE_CLASS_TEXT
        setText(formatInitialRaceTime(result.finishTime))
    }
}
private fun createPositionField(
    context: Context,
    result: CompetitionResultItem
): EditText {
    return EditText(context).apply {
        hint = POSITION_HINT
        inputType = InputType.TYPE_CLASS_NUMBER
        setText(result.position?.toString() ?: EMPTY_TEXT)
    }
}
private fun createNotesField(
    context: Context,
    result: CompetitionResultItem
): EditText {
    return EditText(context).apply {
        hint = NOTES_HINT
        setText(result.notes ?: EMPTY_TEXT)
    }
}
private fun formatInitialRaceTime(secondsValue: Double?): String {
    if (secondsValue == null) {
        return EMPTY_TEXT
    }

    val totalSeconds = secondsValue.toInt()
    val hours = totalSeconds / SECONDS_IN_HOUR
    val minutes = (totalSeconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
    val seconds = totalSeconds % SECONDS_IN_MINUTE

    return if (hours > ZERO_INT) {
        String.format(
            Locale.getDefault(),
            HOURS_TIME_FORMAT,
            hours,
            minutes,
            seconds
        )
    } else {
        String.format(
            Locale.getDefault(),
            MINUTES_TIME_FORMAT,
            minutes,
            seconds
        )
    }
}
private fun convertRaceTimeToSeconds(input: String): Double? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
        return null
    }

    val parts = trimmed.split(TIME_SEPARATOR)
    return when (parts.size) {
        TWO_PARTS -> convertMinutesSeconds(parts)
        THREE_PARTS -> convertHoursMinutesSeconds(parts)
        else -> null
    }
}
private fun convertMinutesSeconds(parts: List<String>): Double? {
    return try {
        val minutes = parts[FIRST_INDEX].toInt()
        val seconds = parts[SECOND_INDEX].toInt()
        val valid = seconds in VALID_SECONDS_RANGE && minutes >= ZERO_INT

        if (valid) {
            (minutes * SECONDS_IN_MINUTE + seconds).toDouble()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
private fun convertHoursMinutesSeconds(parts: List<String>): Double? {
    return try {
        val hours = parts[FIRST_INDEX].toInt()
        val minutes = parts[SECOND_INDEX].toInt()
        val seconds = parts[THIRD_INDEX].toInt()

        val valid = minutes in VALID_SECONDS_RANGE &&
                seconds in VALID_SECONDS_RANGE &&
                hours >= ZERO_INT

        if (valid) {
            (
                    hours * SECONDS_IN_HOUR +
                            minutes * SECONDS_IN_MINUTE +
                            seconds
                    ).toDouble()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun convertFormattedCompetitionDate(rawDate: String): String {
    val inputFormats = listOf(
        SimpleDateFormat(GMT_DATE_PATTERN, Locale.ENGLISH),
        SimpleDateFormat(BASIC_DATE_PATTERN, Locale.ENGLISH),
        SimpleDateFormat(ISO_DATE_PATTERN, Locale.ENGLISH),
        SimpleDateFormat(DATETIME_PATTERN, Locale.ENGLISH)
    )
    val outputFormat = SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.ENGLISH)

    var formattedDate: String? = null

    inputFormats.forEach { format ->
        try {
            val parsedDate = format.parse(rawDate)
            if (parsedDate != null && formattedDate == null) {
                formattedDate = outputFormat.format(parsedDate)
            }
        } catch (_: Exception) {
            // Try next format
        }
    }

    return formattedDate ?: rawDate
        .substringBefore(TIME_SUFFIX)
        .replace(GMT_SUFFIX, EMPTY_TEXT)
        .trim()
}

private const val ZERO_INT = 0
private const val ZERO_DOUBLE = 0.0
private const val MAX_SECONDS_VALUE = 59
private const val FIRST_INDEX = 0
private const val SECOND_INDEX = 1
private const val THIRD_INDEX = 2
private const val TWO_PARTS = 2
private const val THREE_PARTS = 3
private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3600
private val VALID_SECONDS_RANGE = ZERO_INT..MAX_SECONDS_VALUE
private const val DIALOG_PADDING_START = 40
private const val DIALOG_PADDING_TOP = 30
private const val DIALOG_PADDING_END = 40
private const val DIALOG_PADDING_BOTTOM = 10
private const val HOURS_TIME_FORMAT = "%d:%02d:%02d"
private const val MINUTES_TIME_FORMAT = "%d:%02d"
private const val GMT_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
private const val BASIC_DATE_PATTERN = "yyyy-MM-dd"
private const val ISO_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
private const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"
private const val TIME_SUFFIX = " 00:00:00"
private const val GMT_SUFFIX = " GMT"
private const val TIME_SEPARATOR = ":"
private const val NOT_AVAILABLE_TEXT = "N/A"
private const val NONE_TEXT = "None"
private const val EMPTY_TEXT = ""
private const val EDIT_RESULT_TITLE = "Edit Result"
private const val SAVE_TEXT = "Save"
private const val CANCEL_TEXT = "Cancel"
private const val FINISH_TIME_HINT = "Finish time (mm:ss or h:mm:ss)"
private const val POSITION_HINT = "Position (optional)"
private const val NOTES_HINT = "Notes (optional)"
private const val INVALID_TIME_MESSAGE =
    "Enter a valid time like 14:32 or 1:02:15"
private const val RESULT_UPDATED_MESSAGE = "Result updated"
private const val RESULT_DELETED_MESSAGE = "Result deleted"
private const val UPDATE_FAILED_MESSAGE = "Failed to update result"
private const val DELETE_FAILED_MESSAGE = "Failed to delete result"
