package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TrainingPlan(
    private val plans: MutableList<TrainingPlanItem>,
    private val onDeleteClick: (TrainingPlanItem) -> Unit
) : RecyclerView.Adapter<TrainingPlan.TrainingPlanViewHolder>() {

    class TrainingPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planName: TextView = itemView.findViewById(R.id.planName)
        val planDescription: TextView = itemView.findViewById(R.id.planDescription)
        val planDates: TextView = itemView.findViewById(R.id.planDates)
        val deleteTrainingPlan: Button =
            itemView.findViewById(R.id.deleteTrainingPlan)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrainingPlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.training_plan, parent, false)
        return TrainingPlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainingPlanViewHolder, position: Int) {
        val plan = plans[position]

        holder.planName.text = plan.planName
        holder.planDescription.text = holder.itemView.context.getString(
            R.string.training_plan_description_label,
            plan.description ?: NONE_TEXT
        )

        holder.planDates.text = holder.itemView.context.getString(
            R.string.training_plan_dates_label,
            formatDate(plan.startDate),
            formatDate(plan.endDate)
        )

        holder.deleteTrainingPlan.setOnClickListener {
            onDeleteClick(plan)
        }
    }

    override fun getItemCount(): Int = plans.size

    fun updateData(newPlans: List<TrainingPlanItem>) {
        val oldSize = plans.size
        plans.clear()
        notifyItemRangeRemoved(0, oldSize)
        plans.addAll(newPlans)
        notifyItemRangeInserted(0, plans.size)
    }

    private fun formatDate(rawDate: String): String {
        val output = SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.UK)

        val possibleInputs = listOf(
            SimpleDateFormat(BASIC_DATE_PATTERN, Locale.getDefault()),
            SimpleDateFormat(DATETIME_PATTERN, Locale.getDefault()),
            SimpleDateFormat(GMT_DATE_PATTERN, Locale.ENGLISH)
        )

        for (input in possibleInputs) {
            try {
                input.timeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
                val parsed = input.parse(rawDate)
                if (parsed != null) {
                    return output.format(parsed)
                }
            } catch (_: Exception) {
                // Try next format
            }
        }

        return rawDate.take(DATE_TEXT_LENGTH)
    }

    companion object {
        private const val DATE_TEXT_LENGTH = 10
        private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"
        private const val BASIC_DATE_PATTERN = "yyyy-MM-dd"
        private const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
        private const val GMT_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
        private const val UTC_TIMEZONE = "UTC"
        private const val NONE_TEXT = "None"
    }
}
