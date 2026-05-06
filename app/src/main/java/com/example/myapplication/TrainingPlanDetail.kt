package com.example.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("TooManyFunctions")
class TrainingPlanDetail : Activity() {

    private var planId: Int = INVALID_PLAN_ID
    private lateinit var backTrainingPlanDetail: Button
    private lateinit var planNameDetail: EditText
    private lateinit var planDescriptionDetail: EditText
    private lateinit var startDateDetail: EditText
    private lateinit var endDateDetail: EditText
    private lateinit var updateTrainingPlan: Button
    private lateinit var deleteTrainingPlan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.training_plan_detail)

        planId = intent.getIntExtra(PLAN_ID_EXTRA, INVALID_PLAN_ID)

        bindViews()
        setupListeners()
        loadTrainingPlanDetail()
    }

    private fun bindViews() {
        backTrainingPlanDetail = findViewById(R.id.backTrainingPlanDetail)
        planNameDetail = findViewById(R.id.planNameDetail)
        planDescriptionDetail = findViewById(R.id.planDescriptionDetail)
        startDateDetail = findViewById(R.id.startDateDetail)
        endDateDetail = findViewById(R.id.endDateDetail)
        updateTrainingPlan = findViewById(R.id.updateTrainingPlan)
        deleteTrainingPlan = findViewById(R.id.deleteTrainingPlan)
    }

    private fun setupListeners() {
        backTrainingPlanDetail.setOnClickListener {
            finish()
        }

        startDateDetail.setOnClickListener {
            showDatePicker(startDateDetail)
        }

        endDateDetail.setOnClickListener {
            showDatePicker(endDateDetail)
        }

        updateTrainingPlan.setOnClickListener {
            updateTrainingPlan()
        }

        deleteTrainingPlan.setOnClickListener {
            deleteTrainingPlan()
        }
    }

    private fun showDatePicker(targetField: EditText) {
        val calendar = Calendar.getInstance()
        val currentText = targetField.text.toString().trim()

        if (currentText.matches(ISO_DATE_REGEX.toRegex())) {
            try {
                val parts = currentText.split(DATE_SEPARATOR)
                calendar.set(
                    parts[YEAR_INDEX].toInt(),
                    parts[MONTH_INDEX].toInt() - MONTH_OFFSET,
                    parts[DAY_INDEX].toInt()
                )
            } catch (_: Exception) {
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = String.format(
                    Locale.getDefault(),
                    DATE_PICKER_FORMAT,
                    year,
                    month + MONTH_OFFSET,
                    day
                )
                targetField.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun loadTrainingPlanDetail() {
        if (planId == INVALID_PLAN_ID) {
            showToast(INVALID_PLAN_MESSAGE)
            finish()
            return
        }

        RetrofitClient.apiService.getTrainingPlanDetail(planId)
            .enqueue(object : Callback<TrainingPlanDetailResponse> {
                override fun onResponse(
                    call: Call<TrainingPlanDetailResponse>,
                    response: Response<TrainingPlanDetailResponse>
                ) {
                    val plan = response.body()?.plan

                    if (response.isSuccessful && response.body()?.success == true && plan != null) {
                        planNameDetail.setText(plan.planName)
                        planDescriptionDetail.setText(plan.description ?: EMPTY_TEXT)
                        startDateDetail.setText(toIsoDate(plan.startDate))
                        endDateDetail.setText(toIsoDate(plan.endDate))
                    } else {
                        showToast(LOAD_PLAN_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<TrainingPlanDetailResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun updateTrainingPlan() {
        val planName = planNameDetail.text.toString().trim()
        val description = planDescriptionDetail.text.toString().trim()
        val startDate = startDateDetail.text.toString().trim()
        val endDate = endDateDetail.text.toString().trim()

        if (planName.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            showToast(MISSING_REQUIRED_FIELDS_MESSAGE)
            return
        }

        if (!isIsoDate(startDate) || !isIsoDate(endDate)) {
            showToast(INVALID_DATE_FORMAT_MESSAGE)
            return
        }

        val request = UpdateTrainingPlanRequest(
            planName = planName,
            description = description,
            startDate = startDate,
            endDate = endDate
        )

        RetrofitClient.apiService.updateTrainingPlan(planId, request)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        showToast(TRAINING_PLAN_UPDATED_MESSAGE)
                        finish()
                    } else {
                        showToast(parseUpdateErrorMessage(response))
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun parseUpdateErrorMessage(response: Response<BasicApiResponse>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, BasicApiResponse::class.java)
            apiError.message
        } catch (_: Exception) {
            UPDATE_PLAN_FAILED_MESSAGE
        }
    }

    private fun deleteTrainingPlan() {
        RetrofitClient.apiService.deleteTrainingPlan(planId)
            .enqueue(object : Callback<BasicApiResponse> {
                override fun onResponse(
                    call: Call<BasicApiResponse>,
                    response: Response<BasicApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        showToast(TRAINING_PLAN_DELETED_MESSAGE)
                        finish()
                    } else {
                        showToast(DELETE_PLAN_FAILED_MESSAGE)
                    }
                }

                override fun onFailure(call: Call<BasicApiResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            })
    }

    private fun isIsoDate(value: String): Boolean {
        return value.matches(ISO_DATE_REGEX.toRegex())
    }

    private fun toIsoDate(rawDate: String): String {
        val trimmed = rawDate.trim()
        val isoDate = extractIsoDatePrefix(trimmed)
        val gmtDate = convertGmtDateToIso(trimmed)

        return when {
            isoDate != null -> isoDate
            gmtDate != null -> gmtDate
            else -> trimmed
        }
    }

    private fun extractIsoDatePrefix(value: String): String? {
        return if (value.matches(ISO_DATE_PREFIX_REGEX.toRegex())) {
            value.take(ISO_DATE_LENGTH)
        } else {
            null
        }
    }

    private fun convertGmtDateToIso(value: String): String? {
        if (!value.contains(GMT_TEXT)) {
            return null
        }

        return try {
            val inputFormat = SimpleDateFormat(GMT_INPUT_PATTERN, Locale.ENGLISH)
            val outputFormat = SimpleDateFormat(ISO_OUTPUT_PATTERN, Locale.getDefault())
            val parsedDate = inputFormat.parse(value)
            parsedDate?.let { outputFormat.format(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val INVALID_PLAN_ID = -1
        private const val PLAN_ID_EXTRA = "PLAN_ID"
        private const val DATE_SEPARATOR = "-"
        private const val YEAR_INDEX = 0
        private const val MONTH_INDEX = 1
        private const val DAY_INDEX = 2
        private const val MONTH_OFFSET = 1
        private const val ISO_DATE_LENGTH = 10
        private const val ISO_DATE_REGEX = """\d{4}-\d{2}-\d{2}"""
        private const val ISO_DATE_PREFIX_REGEX = """\d{4}-\d{2}-\d{2}.*"""
        private const val DATE_PICKER_FORMAT = "%04d-%02d-%02d"
        private const val GMT_INPUT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
        private const val ISO_OUTPUT_PATTERN = "yyyy-MM-dd"
        private const val GMT_TEXT = "GMT"
        private const val EMPTY_TEXT = ""
        private const val INVALID_PLAN_MESSAGE = "Invalid training plan"
        private const val LOAD_PLAN_FAILED_MESSAGE = "Failed to load training plan"
        private const val MISSING_REQUIRED_FIELDS_MESSAGE =
            "Please fill in all required fields"
        private const val INVALID_DATE_FORMAT_MESSAGE =
            "Dates must be in YYYY-MM-DD format"
        private const val TRAINING_PLAN_UPDATED_MESSAGE = "Training plan updated"
        private const val UPDATE_PLAN_FAILED_MESSAGE = "Failed to update training plan"
        private const val TRAINING_PLAN_DELETED_MESSAGE = "Training plan deleted"
        private const val DELETE_PLAN_FAILED_MESSAGE = "Failed to delete training plan"
    }
}
