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

class WeeklyDistanceChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var items: List<DailyDistanceItem> = emptyList()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = GRID_STROKE_WIDTH
        style = Paint.Style.STROKE
    }

    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = AXIS_TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }

    private val rightTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = AXIS_TEXT_SIZE
        textAlign = Paint.Align.LEFT
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = LINE_COLOR_HEX.toColorInt()
        strokeWidth = LINE_STROKE_WIDTH
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = LINE_COLOR_HEX.toColorInt()
        style = Paint.Style.FILL
    }

    fun setData(newItems: List<DailyDistanceItem>) {
        items = newItems
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (items.isEmpty()) {
            return
        }

        val chartBounds = createChartBounds()
        val maxDistanceValue = calculateMaxDistanceValue()
        val yStepValue = maxDistanceValue / Y_STEPS

        drawGridLines(canvas, chartBounds, yStepValue)

        if (items.size == SINGLE_POINT_COUNT) {
            drawSinglePoint(canvas, chartBounds, maxDistanceValue)
            return
        }

        drawLineSegments(canvas, chartBounds, maxDistanceValue)
        drawPointsAndLabels(canvas, chartBounds, maxDistanceValue)
    }

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

    private fun calculateMaxDistanceValue(): Float {
        val highestDistance = items.maxOfOrNull { it.distance } ?: ZERO_DISTANCE
        val roundedDistance =
            ceil(highestDistance / DISTANCE_STEP).toFloat() * DISTANCE_STEP_FLOAT
        return max(MIN_DISTANCE_DISPLAY, roundedDistance)
    }

    private fun drawGridLines(
        canvas: Canvas,
        chartBounds: ChartBounds,
        yStepValue: Float
    ) {
        for (i in ZERO_INDEX..Y_STEPS) {
            val y = chartBounds.bottom - (i * chartBounds.height / Y_STEPS)
            canvas.drawLine(chartBounds.left, y, chartBounds.right, y, gridPaint)

            val label = (i * yStepValue).toInt().toString()
            canvas.drawText(
                label,
                chartBounds.right + RIGHT_LABEL_X_OFFSET,
                y + RIGHT_LABEL_Y_OFFSET,
                rightTextPaint
            )
        }
    }

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

    private fun calculatePointY(
        distance: Double,
        chartBounds: ChartBounds,
        maxDistanceValue: Float
    ): Float {
        return (
                chartBounds.bottom - ((distance / maxDistanceValue) * chartBounds.height)
                ).toFloat()
    }

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
        private const val GRID_STROKE_WIDTH = 2f
        private const val AXIS_TEXT_SIZE = 22f
        private const val LINE_STROKE_WIDTH = 5f
        private const val POINT_RADIUS = 8f
        private const val LEFT_PADDING = 35f
        private const val TOP_PADDING = 25f
        private const val RIGHT_PADDING = 75f
        private const val BOTTOM_PADDING = 50f
        private const val RIGHT_LABEL_X_OFFSET = 8f
        private const val RIGHT_LABEL_Y_OFFSET = 8f
        private const val BOTTOM_LABEL_OFFSET = 10f
        private const val LINE_COLOR_HEX = "#E67E3C"
        private const val MIN_DISTANCE_DISPLAY = 5f
        private const val DISTANCE_STEP = 5.0
        private const val DISTANCE_STEP_FLOAT = 5f
        private const val Y_STEPS = 4
        private const val SINGLE_POINT_COUNT = 1
        private const val STEP_REDUCTION = 1
        private const val HALF_DIVISOR = 2f
        private const val ZERO_INDEX = 0
        private const val ZERO_DISTANCE = 0.0
    }
}
