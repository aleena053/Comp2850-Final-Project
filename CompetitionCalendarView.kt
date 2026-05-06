package com.example.myapplication

// imports

class CompetitionCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentMonth: YearMonth = YearMonth.now()
    private var highlightedDays: Set<Int> = emptySet()
    
    // set up paint for the monday-friday headers
    private val dayHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = HEADER_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    // paint for the actual day numbers
    private val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = DAY_TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }
    
    // paint for the blue circles on event days
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HIGHLIGHT_COLOR.toColorInt()
        style = Paint.Style.FILL
    }
    private val rowSpacing = ROW_SPACING

    // updates the month and redraws
    fun setMonth(month: YearMonth) {
        currentMonth = month
        invalidate()
    }

    // sets which days get circles and redraws
    fun setCompetitionDates(days: Set<Int>) {
        highlightedDays = days
        invalidate()
    }

    // main draw logic for the calendar grid
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

    // draws the day headers
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

    // draws the grid of day numbers
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

    // draws the highlight circle if day has even
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

    // companion object stays same
}
