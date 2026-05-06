package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Workout is the [RecyclerView.Adapter] used by [WorkoutHistory] to display
 * a scrollable list of logged workouts.
 *
 * It maintains two lists internally:
 *  - [workouts] — the currently visible subset (may be filtered by a search query).
 *  - [allWorkouts] — a copy of the full dataset used to restore results when
 *    the search query is cleared.
 *
 * Each row is inflated from workout.xml. The adapter switches between two
 * display modes based on sport type:
 *  - Cardio (Running, Cycling, Swimming): shows distance and average pace.
 *  - Gym: hides distance/pace and shows a summary of exercises instead.
 *
 * @param workouts      the initial (mutable) list of workouts to display.
 * @param onItemClick   optional lambda invoked when the user taps a row;
 *                      receives the tapped [WorkoutItem] so the caller can
 *                      navigate to [WorkoutDetail].
 */
class Workout(
    private val workouts: MutableList<WorkoutItem>,
    private val onItemClick: ((WorkoutItem) -> Unit)? = null
) : RecyclerView.Adapter<Workout.WorkoutViewHolder>() {

    /**
     * Full unfiltered copy of the data. [filter] searches against this list
     * and writes matching items back to [workouts].
     */
    private val allWorkouts = mutableListOf<WorkoutItem>().apply { addAll(workouts) }

    /**
     * Holds references to the TextViews in a single workout row (workout.xml).
     * RecyclerView recycles these holders so we don't inflate new views for
     * every item.
     */
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutSport: TextView = itemView.findViewById(R.id.workoutSport)
        val workoutDate: TextView = itemView.findViewById(R.id.workoutDate)
        val workoutDuration: TextView = itemView.findViewById(R.id.workoutDuration)
        val workoutDistance: TextView = itemView.findViewById(R.id.workoutDistance) // cardio only
        val workoutPace: TextView = itemView.findViewById(R.id.workoutPace) // cardio only
        val workoutHeartRate: TextView = itemView.findViewById(R.id.workoutHeartRate)
        val workoutNotes: TextView = itemView.findViewById(R.id.workoutNotes)
        val workoutExercises: TextView = itemView.findViewById(R.id.workoutExercises)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout, parent, false)
        return WorkoutViewHolder(view)
    }

    /**
     * Populates a recycled [holder] with data from [workouts][position].
     *
     * Fields common to all sports (sport name, date, duration, heart rate,
     * notes) are bound first. Then either [bindGymWorkout] or
     * [bindCardioWorkout] is called to handle the sport-specific fields.
     */
    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        val isGym = workout.sportName.equals(GYM_SPORT, ignoreCase = true)

        // Common fields
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
        // Sport-specific fields
        if (isGym) {
            bindGymWorkout(holder, workout)
        } else {
            bindCardioWorkout(holder, workout)
        }

        // Row tap → invoke the callback (WorkoutHistory uses it to open WorkoutDetail)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(workout)
        }
    }

    override fun getItemCount(): Int = workouts.size

    // Public API

    /**
     * Filters the visible list to items whose sport name, date, notes, or
     * exercise summaries contain [query] (case-insensitive).
     *
     * Passing an empty string restores the full [allWorkouts] list.
     * Uses paired notifyItemRangeRemoved / notifyItemRangeInserted so
     * RecyclerView can animate the diff.
     */
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

    /**
     * Replaces the full dataset with [newWorkouts] and refreshes both the
     * visible list and the backing [allWorkouts] copy.
     * Called by [WorkoutHistory.loadWorkouts] after each API response.
     */
    fun updateData(newWorkouts: List<WorkoutItem>) {
        val oldSize = workouts.size
        allWorkouts.clear()
        allWorkouts.addAll(newWorkouts)
        workouts.clear()
        notifyItemRangeRemoved(0, oldSize)
        workouts.addAll(newWorkouts)
        notifyItemRangeInserted(0, workouts.size)
    }

    // Private Helpers

    /**
     * Configures a [holder] for a gym workout:
     *  - Hides the distance and pace views (not applicable to gym).
     *  - Shows the exercise summaries TextView, listing each exercise on its own line.
     */
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

    /**
     * Configures a [holder] for a cardio workout:
     *  - Shows distance and pace views.
     *  - Hides the exercise summary view (not applicable to cardio).
     */
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

    /**
     * Normalises [rawDate] into a plain "YYYY-MM-DD" string.
     *
     * The API can return dates in several formats depending on the backend:
     *  1. RFC-822 / HTTP date: "Mon, 06 Jan 2025 00:00:00 GMT" — strip the
     *     day-of-week prefix and the trailing time+timezone.
     *  2. ISO datetime with a time portion: take the first 10 characters.
     *  3. Anything else: return as-is.
     */
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

        // Date parsing helpers
        private const val GMT_TEXT = "GMT"
        private const val TIME_SUFFIX = " 00:00:00"
        private const val LINE_BREAK = "\n"
        private const val EMPTY_TEXT = ""
        private const val ISO_DATE_LENGTH = 10 // length of "YYYY-MM-DD"

        // Day-of-week prefixes present in RFC-822 dates
        private const val SUN_PREFIX = "Sun, "
        private const val MON_PREFIX = "Mon, "
        private const val TUE_PREFIX = "Tue, "
        private const val WED_PREFIX = "Wed, "
        private const val THU_PREFIX = "Thu, "
        private const val FRI_PREFIX = "Fri, "
        private const val SAT_PREFIX = "Sat, "
    }
}
