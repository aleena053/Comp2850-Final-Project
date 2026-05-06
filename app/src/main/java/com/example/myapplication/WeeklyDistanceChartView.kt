package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.ceil
import kotlin.math.max

/**
 * WeeklyDistanceChartView is a custom [View] that draws a simple line chart
 * showing daily distances for the current week.
 *
 * It is used exclusively by [AthleteDashboard], which calls [setData] after
 * receiving the API response. The view then calls [invalidate] to schedule a
 * redraw via [onDraw].
 *
 * Drawing approach:
 *  1. Compute chart bounds (the drawable rectangle inside the padding margins).
 *  2. Determine the y-axis scale from the highest distance in the data.
 *  3. Draw horizontal grid lines at equal intervals, with numeric labels on the right.
 *  4. Connect adjacent data points with straight line segments.
 *  5. Draw a filled circle on each data point, with the day label below it.
 *
 * All drawing is done directly on [Canvas] with [Paint] objects initialised
 * once at field-declaration time to avoid allocations inside onDraw.
 */

class WeeklyDistanceChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /** The data currently being displayed. Empty list = nothing drawn. */
    private var items: List<DailyDistanceItem> = emptyList()

    // Paint objects (created once, reused every frame)

    /** Thin grey lines drawn at each y-axis interval across the full chart width. */
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = GRID_STROKE_WIDTH
        style = Paint.Style.STROKE
    }

    /** Small dark-grey text for x-axis day labels, centred under each data point */
    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = AXIS_TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }

    /** Small dark-grey text for y-axis distance labels, left-aligned to the right of the chart. */
    private val rightTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = AXIS_TEXT_SIZE
        textAlign = Paint.Align.LEFT
    }

    /** Orange line connecting consecutive data points. */
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = LINE_COLOR_HEX.toColorInt()
        strokeWidth = LINE_STROKE_WIDTH
        style = Paint.Style.STROKE
    }

    /** Filled orange circle drawn at each data point. */
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = LINE_COLOR_HEX.toColorInt()
        style = Paint.Style.FILL
    }

    /**
     * Updates the chart with a new dataset and triggers a redraw.
     *
     * @param newItems list of [DailyDistanceItem] objects, one per day of the week.
     *                 Each item holds a day label (e.g. "Mon") and a distance in km.
     */
    fun setData(newItems: List<DailyDistanceItem>) {
        items = newItems
        invalidate()
    }

    /**
     * Called by Android whenever this view needs to draw itself.
     *
     * Steps:
     *  1. Guard against empty data.
     *  2. Build [ChartBounds] from the view's pixel dimensions and padding constants.
     *  3. Calculate the y-axis ceiling (rounded up to the nearest 5 km, minimum 5).
     *  4. Draw grid lines.
     *  5. Special-case a single data point (no line segment to draw).
     *  6. Otherwise draw line segments first, then circles + labels on top.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (items.isEmpty()) {
            return
        }

        val chartBounds = createChartBounds()
        val maxDistanceValue = calculateMaxDistanceValue()
        val yStepValue = maxDistanceValue / Y_STEPS // distance represented by each grid band

        drawGridLines(canvas, chartBounds, yStepValue)

        if (items.size == SINGLE_POINT_COUNT) {
            // Only one day has data: draw a lone circle in the horizontal centre
            drawSinglePoint(canvas, chartBounds, maxDistanceValue)
            return
        }

        // General case: line segments then points/labels so circles render on top of lines
        drawLineSegments(canvas, chartBounds, maxDistanceValue)
        drawPointsAndLabels(canvas, chartBounds, maxDistanceValue)
    }

    /**
     * Builds a [ChartBounds] value that describes the rectangle available for
     * chart content, accounting for the padding reserved for labels.
     *
     * LEFT_PADDING  — space for leftmost point circle to not clip
     * RIGHT_PADDING — space for y-axis labels (e.g. "10", "20")
     * BOTTOM_PADDING — space for x-axis day labels (e.g. "Mon")
     * TOP_PADDING   — breathing room above the top grid line
     */
    private fun createChartBounds(): ChartBounds {
        val chartLeft = LEFT_PADDING
        val chartTop = TOP_PADDING
        val chartRight = width.toFloat() - RIGHT_PADDING
        val chartBottom = height.toFloat() - BOTTOM_PADDING

        return ChartBounds(
            left = chartLeft,
            top = chartTop,
            right = chartRight,
            bottom = chartBottom
        )
    }

    /**
     * Determines the y-axis ceiling.
     *
     * Takes the highest distance in the dataset, rounds it up to the nearest
     * [DISTANCE_STEP] (5 km), and ensures it is at least [MIN_DISTANCE_DISPLAY] (5 km)
     * so the chart is never flat when all values are zero.
     */
    private fun calculateMaxDistanceValue(): Float {
        val highestDistance = items.maxOfOrNull { it.distance } ?: ZERO_DISTANCE
        val roundedDistance =
            ceil(highestDistance / DISTANCE_STEP).toFloat() * DISTANCE_STEP_FLOAT
        return max(MIN_DISTANCE_DISPLAY, roundedDistance)
    }

    /**
     * Draws [Y_STEPS] + 1 horizontal grid lines evenly spaced between the
     * top and bottom of the chart area, and prints the corresponding distance
     * value to the right of each line.
     */
    private fun drawGridLines(
        canvas: Canvas,
        chartBounds: ChartBounds,
        yStepValue: Float
    ) {
        for (i in ZERO_INDEX..Y_STEPS) {
            // y decreases as i increses because canvas y=0 is at the top
            val y = chartBounds.bottom - (i * chartBounds.height / Y_STEPS)
            canvas.drawLine(chartBounds.left, y, chartBounds.right, y, gridPaint)

            // Label for this grid line (e.g. "0", "5", "10", "15", "20")
            val label = (i * yStepValue).toInt().toString()
            canvas.drawText(
                label,
                chartBounds.right + RIGHT_LABEL_X_OFFSET,
                y + RIGHT_LABEL_Y_OFFSET,
                rightTextPaint
            )
        }
    }

    /**
     * Draws a single circle in the horizontal centre of the chart for datasets
     * that contain exactly one data point. Also draws the day label below it.
     */
    private fun drawSinglePoint(
        canvas: Canvas,
        chartBounds: ChartBounds,
        maxDistanceValue: Float
    ) {
        val item = items.first()
        val x = chartBounds.left + (chartBounds.width / HALF_DIVISOR)
        val y = calculatePointY(item.distance, chartBounds, maxDistanceValue)

        canvas.drawCircle(x, y, POINT_RADIUS, pointPaint)
        canvas.drawText(
            item.label,
            x,
            height.toFloat() - BOTTOM_LABEL_OFFSET,
            axisTextPaint
        )
    }

    /**
     * Draws straight line segments connecting consecutive data points.
     * Points are evenly distributed across the chart width.
     */
    private fun drawLineSegments(
        canvas: Canvas,
        chartBounds: ChartBounds,
        maxDistanceValue: Float
    ) {
        val stepX = chartBounds.width / (items.size - STEP_REDUCTION)

        for (i in ZERO_INDEX until items.size - STEP_REDUCTION) {
            val startX = chartBounds.left + (i * stepX)
            val startY = calculatePointY(items[i].distance, chartBounds, maxDistanceValue)

            val stopX = chartBounds.left + ((i + STEP_REDUCTION) * stepX)
            val stopY = calculatePointY(
                items[i + STEP_REDUCTION].distance,
                chartBounds,
                maxDistanceValue
            )

            canvas.drawLine(startX, startY, stopX, stopY, linePaint)
        }
    }

    /**
     * Draws a filled circle and a day label for every data point.
     * Called after [drawLineSegments] so the circles appear on top of lines.
     */
    private fun drawPointsAndLabels(
        canvas: Canvas,
        chartBounds: ChartBounds,
        maxDistanceValue: Float
    ) {
        val stepX = chartBounds.width / (items.size - STEP_REDUCTION)

        items.forEachIndexed { index, item ->
            val x = chartBounds.left + (index * stepX)
            val y = calculatePointY(item.distance, chartBounds, maxDistanceValue)

            canvas.drawCircle(x, y, POINT_RADIUS, pointPaint)
            canvas.drawText(
                item.label,
                x,
                height.toFloat() - BOTTOM_LABEL_OFFSET,
                axisTextPaint
            )
        }
    }

    /**
     * Converts a [distance] value into a canvas y-coordinate.
     *
     * A distance of 0 maps to [ChartBounds.bottom]; [maxDistanceValue] maps to
     * [ChartBounds.top]. Values in between are linearly interpolated.
     */
    private fun calculatePointY(
        distance: Double,
        chartBounds: ChartBounds,
        maxDistanceValue: Float
    ): Float {
        return (
                chartBounds.bottom - ((distance / maxDistanceValue) * chartBounds.height)
                ).toFloat()
    }

    /**
     * Holds the pixel coordinates of the drawable chart area after padding is applied.
     * [width] and [height] are computed properties for convenience.
     */
    private data class ChartBounds(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    ) {
        val width: Float
            get() = right - left

        val height: Float
            get() = bottom - top
    }

    companion object {
        // Paint dimensions
        private const val GRID_STROKE_WIDTH = 2f
        private const val AXIS_TEXT_SIZE = 22f
        private const val LINE_STROKE_WIDTH = 5f
        private const val POINT_RADIUS = 8f

        // Chart padding (pixels reserved outside the plot area)
        private const val LEFT_PADDING = 35f
        private const val TOP_PADDING = 25f
        private const val RIGHT_PADDING = 75f
        private const val BOTTOM_PADDING = 50f

        // Offsets for the y-axis labels drawn to the right of the chart
        private const val RIGHT_LABEL_X_OFFSET = 8f
        private const val RIGHT_LABEL_Y_OFFSET = 8f

        // Offset from the very bottom of the view for day labels
        private const val BOTTOM_LABEL_OFFSET = 10f
        private const val LINE_COLOR_HEX = "#E67E3C" // Orange accent colour

        // Y-axis scale constants
        private const val MIN_DISTANCE_DISPLAY = 5f // minimum y-axis ceiling (km)
        private const val DISTANCE_STEP = 5.0 // round up to nearest 5 km
        private const val DISTANCE_STEP_FLOAT = 5f
        private const val Y_STEPS = 4 // number of grid bands (0, 5, 10, 15, 20...)

        // Index / loop helpers
        private const val SINGLE_POINT_COUNT = 1
        private const val STEP_REDUCTION = 1 // (size-1) segments for (size) points
        private const val HALF_DIVISOR = 2f
        private const val ZERO_INDEX = 0
        private const val ZERO_DISTANCE = 0.0
    }
}
