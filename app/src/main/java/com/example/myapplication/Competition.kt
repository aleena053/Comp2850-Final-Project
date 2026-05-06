@file:Suppress("TooManyFunctions")

package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Competition(
    private val context: Context,
    private val competitions: MutableList<CompetitionItem>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<Competition.CompetitionViewHolder>() {

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.competition, parent, false)
        return CompetitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompetitionViewHolder, position: Int) {
        val competition = competitions[position]

        holder.competitionName.text = competition.name
        holder.competitionDate.text = context.getString(
            R.string.competition_date_label,
            formatCompetitionDate(competition.competitionDate)
        )

        holder.competitionLocation.text = context.getString(
            R.string.competition_location_label,
            competition.location ?: NOT_AVAILABLE_TEXT
        )

        holder.competitionSport.text = context.getString(
            R.string.competition_sport_label,
            competition.sportName
        )

        holder.competitionEventType.text = context.getString(
            R.string.competition_event_type_label,
            competition.eventType
        )

        holder.competitionDescription.text = context.getString(
            R.string.competition_description_label,
            competition.description ?: NONE_TEXT
        )

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

    fun updateData(newCompetitions: List<CompetitionItem>) {
        val oldSize = competitions.size
        competitions.clear()
        notifyItemRangeRemoved(0, oldSize)
        competitions.addAll(newCompetitions)
        notifyItemRangeInserted(0, competitions.size)
    }

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
                        onDataChanged()
                    } else {
                        val message = response.body()?.message
                            ?: DELETE_COMPETITION_FAILED_MESSAGE
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

    private fun submitResult(
        competition: CompetitionItem,
        fields: ResultDialogFields
    ) {
        val userId = SessionManager(context).getUserId()
        val finishTime = convertRaceTimeToSeconds(fields.finishTime.text.toString())
        val position = fields.position.text.toString().trim().toIntOrNull()
        val notes = fields.notes.text.toString().trim()

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
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

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

    private fun submitCompetitionUpdate(
        competition: CompetitionItem,
        fields: CompetitionDialogFields
    ) {
        val selectedSportId = getSportIdFromName(
            fields.sportSpinner.selectedItem.toString()
        )

        val request = UpdateCompetitionRequest(
            name = fields.name.text.toString().trim(),
            location = fields.location.text.toString().trim(),
            competitionDate = fields.date.text.toString().trim(),
            sportId = selectedSportId,
            eventType = fields.eventType.text.toString().trim(),
            description = fields.description.text.toString().trim()
        )

        RetrofitClient.apiService.updateCompetition(competition.competitionId, request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            context,
                            COMPETITION_UPDATED_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDataChanged()
                    } else {
                        val message = response.body()?.message
                            ?: UPDATE_COMPETITION_FAILED_MESSAGE
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

    private fun bindCompetitionFields(
        competition: CompetitionItem,
        fields: CompetitionDialogFields
    ) {
        fields.name.setText(competition.name)
        fields.location.setText(competition.location ?: EMPTY_TEXT)
        fields.date.setText(competition.competitionDate.take(DATE_TEXT_LENGTH))
        fields.eventType.setText(competition.eventType)
        fields.description.setText(competition.description ?: EMPTY_TEXT)
    }
}

private data class CompetitionDialogFields(
    val name: EditText,
    val location: EditText,
    val date: EditText,
    val sportSpinner: Spinner,
    val eventType: EditText,
    val description: EditText
)

private data class ResultDialogFields(
    val finishTime: EditText,
    val position: EditText,
    val notes: EditText
)

private fun createResultDialogLayout(context: Context): LinearLayout {
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

private fun createFinishTimeField(context: Context): EditText {
    return EditText(context).apply {
        hint = FINISH_TIME_HINT
        inputType = InputType.TYPE_CLASS_TEXT
    }
}

private fun createPositionField(context: Context): EditText {
    return EditText(context).apply {
        hint = POSITION_HINT
        inputType = InputType.TYPE_CLASS_NUMBER
    }
}

private fun createNotesField(context: Context): EditText {
    return EditText(context).apply {
        hint = NOTES_HINT
    }
}

private fun setupSportSpinner(
    context: Context,
    spinner: Spinner,
    sportName: String
) {
    val sports = listOf(RUNNING_TEXT, CYCLING_TEXT, SWIMMING_TEXT)
    val adapter = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_item,
        sports
    )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter

    val sportIndex = sports.indexOfFirst {
        it.equals(sportName, ignoreCase = true)
    }
    if (sportIndex >= ZERO_INT) {
        spinner.setSelection(sportIndex)
    }
}

private fun setupDatePicker(
    context: Context,
    etDate: EditText
) {
    etDate.setOnClickListener {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                etDate.setText(
                    String.format(
                        Locale.getDefault(),
                        DATE_PICKER_FORMAT,
                        year,
                        month + MONTH_OFFSET,
                        day
                    )
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

private fun getSportIdFromName(sportName: String): Int {
    return when (sportName) {
        RUNNING_TEXT -> RUNNING_ID
        CYCLING_TEXT -> CYCLING_ID
        SWIMMING_TEXT -> SWIMMING_ID
        else -> RUNNING_ID
    }
}

private fun convertRaceTimeToSeconds(input: String): Double? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
        return null
    }

    val parts = trimmed.split(TIME_SEPARATOR)
    return when (parts.size) {
        MINUTES_SECONDS_PARTS -> convertMinutesSeconds(parts)
        HOURS_MINUTES_SECONDS_PARTS -> convertHoursMinutesSeconds(parts)
        else -> null
    }
}

private fun convertMinutesSeconds(parts: List<String>): Double? {
    return try {
        val minutes = parts[FIRST_INDEX].toInt()
        val seconds = parts[SECOND_INDEX].toInt()
        val valid = minutes >= ZERO_INT && seconds in VALID_SECONDS_RANGE

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

        val valid = hours >= ZERO_INT &&
                minutes in VALID_SECONDS_RANGE &&
                seconds in VALID_SECONDS_RANGE

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

private fun formatCompetitionDate(rawDate: String?): String {
    val date = rawDate?.takeIf { it.isNotBlank() } ?: return EMPTY_TEXT
    val parsedDate = convertCompetitionDate(date)

    return if (parsedDate != null) {
        SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.ENGLISH).format(parsedDate)
    } else {
        cleanCompetitionDate(date)
    }
}

private fun convertCompetitionDate(rawDate: String): Date? {
    val inputFormats = listOf(
        GMT_DATE_PATTERN,
        BASIC_DATE_PATTERN,
        ISO_DATE_PATTERN,
        DATETIME_PATTERN
    ).map { pattern ->
        SimpleDateFormat(pattern, Locale.ENGLISH)
    }

    for (format in inputFormats) {
        try {
            val parsedDate = format.parse(rawDate)
            if (parsedDate != null) {
                return parsedDate
            }
        } catch (_: Exception) {
            // Try next format
        }
    }

    return null
}

private fun cleanCompetitionDate(rawDate: String): String {
    return rawDate
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

private const val MINUTES_SECONDS_PARTS = 2
private const val HOURS_MINUTES_SECONDS_PARTS = 3

private const val DATE_TEXT_LENGTH = 10
private const val MONTH_OFFSET = 1

private const val RUNNING_ID = 1
private const val CYCLING_ID = 2
private const val SWIMMING_ID = 3

private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3600
private val VALID_SECONDS_RANGE = ZERO_INT..MAX_SECONDS_VALUE

private const val DIALOG_PADDING_START = 40
private const val DIALOG_PADDING_TOP = 30
private const val DIALOG_PADDING_END = 40
private const val DIALOG_PADDING_BOTTOM = 10

private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"

private const val GMT_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
private const val BASIC_DATE_PATTERN = "yyyy-MM-dd"
private const val ISO_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
private const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"
private const val TIME_SUFFIX = " 00:00:00"
private const val GMT_SUFFIX = " GMT"
private const val TIME_SEPARATOR = ":"
private const val RUNNING_TEXT = "Running"
private const val CYCLING_TEXT = "Cycling"
private const val SWIMMING_TEXT = "Swimming"
private const val NOT_AVAILABLE_TEXT = "N/A"
private const val NONE_TEXT = "None"
private const val EMPTY_TEXT = ""
private const val ADD_RESULT_TITLE = "Add Result"
private const val EDIT_COMPETITION_TITLE = "Edit Competition"
private const val SAVE_TEXT = "Save"
private const val CANCEL_TEXT = "Cancel"
private const val FINISH_TIME_HINT = "Finish time (mm:ss or h:mm:ss)"
private const val POSITION_HINT = "Position (optional)"
private const val NOTES_HINT = "Notes (optional)"
private const val INVALID_TIME_MESSAGE =
    "Enter a valid time like 14:32 or 1:02:15"
private const val RESULT_ADDED_MESSAGE = "Result added"
private const val COMPETITION_UPDATED_MESSAGE = "Competition updated"
private const val ADD_RESULT_FAILED_MESSAGE = "Failed to add result"
private const val UPDATE_COMPETITION_FAILED_MESSAGE = "Failed to update competition"
private const val COMPETITION_DELETED_MESSAGE = "Competition deleted"
private const val DELETE_COMPETITION_FAILED_MESSAGE = "Failed to delete competition"
