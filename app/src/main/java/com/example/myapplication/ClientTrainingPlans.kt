package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("TooManyFunctions")
class ClientTrainingPlans : Activity() {

    private var clientId: Int = INVALID_CLIENT_ID
    private var clientName: String = DEFAULT_CLIENT_NAME

    private lateinit var backClientPlans: Button
    private lateinit var createTrainingPlan: Button
    private lateinit var clientPlansTitle: TextView
    private lateinit var layoutPlansContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.client_training_plans)

        clientId = intent.getIntExtra(CLIENT_ID_EXTRA, INVALID_CLIENT_ID)
        clientName = intent.getStringExtra(CLIENT_NAME_EXTRA) ?: DEFAULT_CLIENT_NAME

        bindViews()
        setupScreen()
        loadPlans()
    }

    override fun onResume() {
        super.onResume()
        loadPlans()
    }

    private fun bindViews() {
        backClientPlans = findViewById(R.id.backClientPlans)
        createTrainingPlan = findViewById(R.id.createTrainingPlan)
        clientPlansTitle = findViewById(R.id.clientPlansTitle)
        layoutPlansContainer = findViewById(R.id.layoutPlansContainer)
    }

    private fun setupScreen() {
        clientPlansTitle.text = getString(R.string.client_training_plans_title, clientName)

        backClientPlans.setOnClickListener {
            finish()
        }

        createTrainingPlan.setOnClickListener {
            openCreateTrainingPlanScreen()
        }
    }

    private fun openCreateTrainingPlanScreen() {
        val intent = Intent(this, CreateTrainingPlan::class.java).apply {
            putExtra(CLIENT_ID_EXTRA, clientId)
        }
        startActivity(intent)
    }

    private fun loadPlans() {
        if (clientId == INVALID_CLIENT_ID) {
            Toast.makeText(this, INVALID_CLIENT_MESSAGE, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitClient.apiService.getTrainingPlans(clientId)
            .enqueue(object : Callback<TrainingPlansResponse> {
                override fun onResponse(
                    call: Call<TrainingPlansResponse>,
                    response: Response<TrainingPlansResponse>
                ) {
                    handlePlansResponse(response)
                }

                override fun onFailure(call: Call<TrainingPlansResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ClientTrainingPlans,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun handlePlansResponse(response: Response<TrainingPlansResponse>) {
        if (response.isSuccessful && response.body()?.success == true) {
            val plans = response.body()?.plans ?: emptyList()
            showPlans(plans)
            return
        }

        Toast.makeText(
            this@ClientTrainingPlans,
            FAILED_TO_LOAD_MESSAGE,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPlans(plans: List<TrainingPlanItem>) {
        layoutPlansContainer.removeAllViews()

        if (plans.isEmpty()) {
            layoutPlansContainer.addView(createEmptyPlansTextView())
            return
        }

        plans.forEach { plan ->
            layoutPlansContainer.addView(createPlanCard(plan))
        }
    }

    private fun createEmptyPlansTextView(): TextView {
        return TextView(this).apply {
            text = NO_PLANS_MESSAGE
            setTextColor(getColor(android.R.color.white))
            textSize = BODY_TEXT_SIZE
        }
    }

    private fun createPlanCard(plan: TrainingPlanItem): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(
                this@ClientTrainingPlans,
                R.drawable.workout_card
            )
            setPadding(
                CARD_PADDING,
                CARD_PADDING,
                CARD_PADDING,
                CARD_PADDING
            )
            layoutParams = createCardLayoutParams()

            setOnClickListener {
                openTrainingPlanDetail(plan.planId)
            }

            addView(createTitleTextView(plan.planName))
            addView(createDescriptionTextView(plan.description))
            addView(createDatesTextView(plan.startDate, plan.endDate))
        }
    }

    private fun createCardLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = CARD_MARGIN_BOTTOM
        }
    }

    private fun openTrainingPlanDetail(planId: Int) {
        val intent = Intent(this, TrainingPlanDetail::class.java).apply {
            putExtra(PLAN_ID_EXTRA, planId)
        }
        startActivity(intent)
    }

    private fun createTitleTextView(planName: String): TextView {
        return TextView(this).apply {
            text = planName
            setTextColor(getColor(android.R.color.white))
            textSize = TITLE_TEXT_SIZE
        }
    }

    private fun createDescriptionTextView(description: String?): TextView {
        val descriptionText = description ?: NO_DESCRIPTION_TEXT

        return TextView(this).apply {
            text = getString(R.string.training_plan_description_label, descriptionText)
            setTextColor(getColor(android.R.color.white))
            textSize = BODY_TEXT_SIZE
        }
    }

    private fun createDatesTextView(startDateRaw: String, endDateRaw: String): TextView {
        val startDate = formatPlanDate(startDateRaw)
        val endDate = formatPlanDate(endDateRaw)

        return TextView(this).apply {
            text = getString(R.string.training_plan_dates_label, startDate, endDate)
            setTextColor(getColor(android.R.color.white))
            textSize = BODY_TEXT_SIZE
        }
    }

    private fun formatPlanDate(rawDate: String): String {
        return if (rawDate.contains(GMT_TEXT)) {
            rawDate
                .substringBefore(TIME_SUFFIX)
                .replace(SUN_PREFIX, "")
                .replace(MON_PREFIX, "")
                .replace(TUE_PREFIX, "")
                .replace(WED_PREFIX, "")
                .replace(THU_PREFIX, "")
                .replace(FRI_PREFIX, "")
                .replace(SAT_PREFIX, "")
        } else if (rawDate.length >= DATE_TEXT_LENGTH) {
            rawDate.take(DATE_TEXT_LENGTH)
        } else {
            rawDate
        }
    }

    companion object {
        private const val INVALID_CLIENT_ID = -1
        private const val DEFAULT_CLIENT_NAME = "Client"

        private const val CARD_PADDING = 16
        private const val CARD_MARGIN_BOTTOM = 16
        private const val TITLE_TEXT_SIZE = 20f
        private const val BODY_TEXT_SIZE = 16f
        private const val DATE_TEXT_LENGTH = 10

        private const val CLIENT_ID_EXTRA = "CLIENT_ID"
        private const val CLIENT_NAME_EXTRA = "CLIENT_NAME"
        private const val PLAN_ID_EXTRA = "PLAN_ID"

        private const val INVALID_CLIENT_MESSAGE = "Invalid client"
        private const val FAILED_TO_LOAD_MESSAGE = "Failed to load training plans"
        private const val NO_PLANS_MESSAGE = "No training plans yet."
        private const val NO_DESCRIPTION_TEXT = "None"

        private const val GMT_TEXT = "GMT"
        private const val TIME_SUFFIX = " 00:00:00"
        private const val SUN_PREFIX = "Sun, "
        private const val MON_PREFIX = "Mon, "
        private const val TUE_PREFIX = "Tue, "
        private const val WED_PREFIX = "Wed, "
        private const val THU_PREFIX = "Thu, "
        private const val FRI_PREFIX = "Fri, "
        private const val SAT_PREFIX = "Sat, "
    }
}
