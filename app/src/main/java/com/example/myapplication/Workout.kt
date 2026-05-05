package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Workout(
    private val workouts: MutableList<WorkoutItem>,
    private val onItemClick: ((WorkoutItem) -> Unit)? = null
) : RecyclerView.Adapter<Workout.WorkoutViewHolder>() {

    private val allWorkouts = mutableListOf<WorkoutItem>().apply { addAll(workouts) }

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutSport: TextView = itemView.findViewById(R.id.workoutSport)
        val workoutDate: TextView = itemView.findViewById(R.id.workoutDate)
        val workoutDuration: TextView = itemView.findViewById(R.id.workoutDuration)
        val workoutDistance: TextView = itemView.findViewById(R.id.workoutDistance)
        val workoutPace: TextView = itemView.findViewById(R.id.workoutPace)
        val workoutHeartRate: TextView = itemView.findViewById(R.id.workoutHeartRate)
        val workoutNotes: TextView = itemView.findViewById(R.id.workoutNotes)
        val workoutExercises: TextView = itemView.findViewById(R.id.workoutExercises)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        val isGym = workout.sportName.equals(GYM_SPORT, ignoreCase = true)

        holder.workoutSport.text = workout.sportName
        holder.workoutDate.text = holder.itemView.context.getString(
            R.string.workout_date_label,
            formatWorkoutDate(workout.workoutDate)
        )

        holder.workoutDuration.text = holder.itemView.context.getString(
            R.string.workout_duration_label,
            workout.duration
        )

        holder.workoutHeartRate.text = holder.itemView.context.getString(
            R.string.workout_heart_rate_label,
            workout.avgHeartRate?.toString() ?: NOT_AVAILABLE_TEXT
        )

        holder.workoutNotes.text = holder.itemView.context.getString(
            R.string.workout_notes_label,
            workout.notes ?: NONE_TEXT
        )
        if (isGym) {
            bindGymWorkout(holder, workout)
        } else {
            bindCardioWorkout(holder, workout)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(workout)
        }
    }

    override fun getItemCount(): Int = workouts.size

    fun filter(query: String) {
        val searchText = query.trim().lowercase()
        val oldSize = workouts.size

        val filteredWorkouts = if (searchText.isEmpty()) {
            allWorkouts
        } else {
            allWorkouts.filter { workout ->
                workout.sportName.lowercase().contains(searchText) ||
                        formatWorkoutDate(workout.workoutDate).lowercase().contains(searchText) ||
                        workout.notes?.lowercase()?.contains(searchText) == true ||
                        workout.exerciseSummaries?.any {
                            it.lowercase().contains(searchText)
                        } == true
            }
        }

        workouts.clear()
        notifyItemRangeRemoved(0, oldSize)
        workouts.addAll(filteredWorkouts)
        notifyItemRangeInserted(0, workouts.size)
    }

    fun updateData(newWorkouts: List<WorkoutItem>) {
        val oldSize = workouts.size
        allWorkouts.clear()
        allWorkouts.addAll(newWorkouts)
        workouts.clear()
        notifyItemRangeRemoved(0, oldSize)
        workouts.addAll(newWorkouts)
        notifyItemRangeInserted(0, workouts.size)
    }

    private fun bindGymWorkout(holder: WorkoutViewHolder, workout: WorkoutItem) {
        holder.workoutDistance.visibility = View.GONE
        holder.workoutPace.visibility = View.GONE
        holder.workoutExercises.visibility = View.VISIBLE

        val summaries = workout.exerciseSummaries ?: emptyList()
        holder.workoutExercises.text = if (summaries.isNotEmpty()) {
            "Exercises:\n${summaries.joinToString(LINE_BREAK)}"
        } else {
            EXERCISES_NONE_TEXT
        }
    }

    private fun bindCardioWorkout(holder: WorkoutViewHolder, workout: WorkoutItem) {
        holder.workoutDistance.visibility = View.VISIBLE
        holder.workoutPace.visibility = View.VISIBLE
        holder.workoutExercises.visibility = View.GONE

        holder.workoutDistance.text = holder.itemView.context.getString(
            R.string.workout_distance_label,
            workout.distanceKm?.toString() ?: NOT_AVAILABLE_TEXT
        )

        holder.workoutPace.text = holder.itemView.context.getString(
            R.string.workout_pace_label,
            workout.avgPace?.toString() ?: NOT_AVAILABLE_TEXT
        )
    }

    private fun formatWorkoutDate(rawDate: String): String {
        return when {
            rawDate.contains(GMT_TEXT) -> {
                rawDate.substringBefore(TIME_SUFFIX)
                    .replace(SUN_PREFIX, EMPTY_TEXT)
                    .replace(MON_PREFIX, EMPTY_TEXT)
                    .replace(TUE_PREFIX, EMPTY_TEXT)
                    .replace(WED_PREFIX, EMPTY_TEXT)
                    .replace(THU_PREFIX, EMPTY_TEXT)
                    .replace(FRI_PREFIX, EMPTY_TEXT)
                    .replace(SAT_PREFIX, EMPTY_TEXT)
            }
            rawDate.length >= ISO_DATE_LENGTH -> {
                rawDate.take(ISO_DATE_LENGTH)
            }
            else -> {
                rawDate
            }
        }
    }

    companion object {
        private const val GYM_SPORT = "Gym"
        private const val NOT_AVAILABLE_TEXT = "N/A"
        private const val NONE_TEXT = "None"
        private const val EXERCISES_NONE_TEXT = "Exercises: None"
        private const val GMT_TEXT = "GMT"
        private const val TIME_SUFFIX = " 00:00:00"
        private const val LINE_BREAK = "\n"
        private const val EMPTY_TEXT = ""
        private const val ISO_DATE_LENGTH = 10
        private const val SUN_PREFIX = "Sun, "
        private const val MON_PREFIX = "Mon, "
        private const val TUE_PREFIX = "Tue, "
        private const val WED_PREFIX = "Wed, "
        private const val THU_PREFIX = "Thu, "
        private const val FRI_PREFIX = "Fri, "
        private const val SAT_PREFIX = "Sat, "
    }
}
