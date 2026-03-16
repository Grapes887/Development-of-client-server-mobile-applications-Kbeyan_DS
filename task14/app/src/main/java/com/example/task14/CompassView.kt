package com.example.task14

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var azimuth = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rectF = RectF()
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val northColor = Color.RED
    private val southColor = Color.GRAY

    init {
        textPaint.color = Color.WHITE
        textPaint.textSize = 60f
        textPaint.textAlign = Paint.Align.CENTER

        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = resources.getColor(R.color.dark_surface, null)
    }

    fun setAzimuth(azimuth: Float) {
        this.azimuth = azimuth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = min(width, height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        // Рисуем фон круга
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawOval(rectF, backgroundPaint)

        // Рисуем границу круга
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.GRAY
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Рисуем стрелку компаса
        canvas.save()
        canvas.rotate(-azimuth, centerX, centerY)

        val arrowLength = radius * 0.8f
        val arrowWidth = radius * 0.15f

        // Северная часть (красная)
        val northPath = Path().apply {
            moveTo(centerX, centerY - arrowLength)
            lineTo(centerX - arrowWidth, centerY)
            lineTo(centerX + arrowWidth, centerY)
            close()
        }
        paint.style = Paint.Style.FILL
        paint.color = northColor
        canvas.drawPath(northPath, paint)

        // Южная часть (серая)
        val southPath = Path().apply {
            moveTo(centerX, centerY + arrowLength)
            lineTo(centerX - arrowWidth, centerY)
            lineTo(centerX + arrowWidth, centerY)
            close()
        }
        paint.color = southColor
        canvas.drawPath(southPath, paint)

        canvas.restore()

        // Рисуем букву "N"
        canvas.drawText("N", centerX, centerY - radius * 0.6f, textPaint)
    }
}