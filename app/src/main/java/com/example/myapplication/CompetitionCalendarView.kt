package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.time.YearMonth
import androidx.core.graphics.toColorInt

class CompetitionCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentMonth: YearMonth = YearMonth.now()
    private var highlightedDays: Set<Int> = emptySet()
    private val dayHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = HEADER_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = DAY_TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HIGHLIGHT_COLOR.toColorInt()
        style = Paint.Style.FILL
    }
    private val rowSpacing = ROW_SPACING

    fun setMonth(month: YearMonth) {
        currentMonth = month
        invalidate()
    }

    fun setCompetitionDates(days: Set<Int>) {
        highlightedDays = days
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthF = width.toFloat()
        val heightF = height.toFloat()

        val daysOfWeek = listOf(
            MONDAY_LABEL,
            TUESDAY_LABEL,
            WEDNESDAY_LABEL,
            THURSDAY_LABEL,
            FRIDAY_LABEL,
            SATURDAY_LABEL,
            SUNDAY_LABEL
        )

        val columnWidth = widthF / COLUMN_COUNT

        val weekHeaderY = paddingTop + HEADER_TOP_OFFSET
        val gridTop = weekHeaderY + GRID_TOP_OFFSET

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDay = currentMonth.atDay(FIRST_DAY_OF_MONTH).dayOfWeek.value
        val startOffset = firstDay - START_OFFSET_ADJUSTMENT

        val usableHeight = heightF - gridTop - paddingBottom
        val cellHeight = usableHeight / ROW_COUNT

        drawWeekHeaders(canvas, daysOfWeek, columnWidth, weekHeaderY)
        drawDayCells(
            canvas = canvas,
            daysInMonth = daysInMonth,
            startOffset = startOffset,
            columnWidth = columnWidth,
            gridTop = gridTop,
            cellHeight = cellHeight
        )
    }
    private fun drawWeekHeaders(
        canvas: Canvas,
        daysOfWeek: List<String>,
        columnWidth: Float,
        weekHeaderY: Float
    ) {
        for (i in ZERO_INDEX until COLUMN_COUNT) {
            val cx = (i * columnWidth) + columnWidth / HALF_DIVISOR
            val textY = weekHeaderY -
                    ((dayHeaderPaint.descent() + dayHeaderPaint.ascent()) / HALF_DIVISOR)

            canvas.drawText(
                daysOfWeek[i],
                cx,
                textY,
                dayHeaderPaint
            )
        }
    }

    @Suppress("LongParameterList")
    private fun drawDayCells(
        canvas: Canvas,
        daysInMonth: Int,
        startOffset: Int,
        columnWidth: Float,
        gridTop: Float,
        cellHeight: Float
    ) {
        for (day in FIRST_DAY_OF_MONTH..daysInMonth) {
            val index = startOffset + (day - FIRST_DAY_OF_MONTH)
            val row = index / COLUMN_COUNT
            val col = index % COLUMN_COUNT

            val left = col * columnWidth
            val top = gridTop + row * cellHeight + row * rowSpacing

            val cx = left + columnWidth / HALF_DIVISOR
            val cy = top + cellHeight / HALF_DIVISOR

            drawHighlightedCircleIfNeeded(canvas, day, cx, cy, columnWidth, cellHeight)

            val textY = cy - ((dayTextPaint.descent() + dayTextPaint.ascent()) / HALF_DIVISOR)
            canvas.drawText(day.toString(), cx, textY, dayTextPaint)
        }
    }

    @Suppress("LongParameterList")
    private fun drawHighlightedCircleIfNeeded(
        canvas: Canvas,
        day: Int,
        cx: Float,
        cy: Float,
        columnWidth: Float,
        cellHeight: Float
    ) {
        if (highlightedDays.contains(day)) {
            val radius = minOf(columnWidth, cellHeight) * HIGHLIGHT_RADIUS_MULTIPLIER
            canvas.drawCircle(cx, cy, radius, highlightPaint)
        }
    }

    companion object {
        private const val HEADER_TEXT_SIZE = 26f
        private const val DAY_TEXT_SIZE = 34f
        private const val ROW_SPACING = 36f
        private const val HEADER_TOP_OFFSET = 26f
        private const val GRID_TOP_OFFSET = 28f
        private const val HIGHLIGHT_RADIUS_MULTIPLIER = 0.28f
        private const val HALF_DIVISOR = 2f
        private const val COLUMN_COUNT = 7
        private const val ROW_COUNT = 6
        private const val ZERO_INDEX = 0
        private const val FIRST_DAY_OF_MONTH = 1
        private const val START_OFFSET_ADJUSTMENT = 1
        private const val HIGHLIGHT_COLOR = "#3F51B5"
        private const val MONDAY_LABEL = "M"
        private const val TUESDAY_LABEL = "T"
        private const val WEDNESDAY_LABEL = "W"
        private const val THURSDAY_LABEL = "T"
        private const val FRIDAY_LABEL = "F"
        private const val SATURDAY_LABEL = "S"
        private const val SUNDAY_LABEL = "S"
    }
}

