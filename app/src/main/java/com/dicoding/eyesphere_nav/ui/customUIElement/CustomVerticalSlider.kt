package com.dicoding.eyesphere_nav.ui.customUIElement

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import kotlin.math.*

// Base Widget class
abstract class Widget(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) : View(context, attrs) {
    abstract fun setupWidget()
}

class CustomVerticalSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Widget(context, attrs, defStyleAttr) {

    // Properties
    var value: Float = 50f
        set(newValue) {
            field = newValue.coerceIn(minValue, maxValue)
            invalidate()
            onValueChangeListener?.invoke(field)
        }

    var minValue: Float = 0f
    var maxValue: Float = 100f
    var stepSize: Float = 1f

    // Colors
    var trackColor: Int = "#E5E7EB".toColorInt()
    var fillColor: Int = "#3B82F6".toColorInt()
    var thumbColor: Int = "#FFFFFF".toColorInt()
    var thumbStrokeColor: Int = "#3B82F6".toColorInt()

    // Dimensions
    private var trackWidth = 12f
    private var thumbRadius = 24f
    private var thumbStrokeWidth = 4f

    // Touch handling
    private var isDragging = false
    private var touchStartY = 0f

    // Paint objects
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Listener
    var onValueChangeListener: ((Float) -> Unit)? = null

    init {
        setupWidget()
        setupPaints()
    }

    override fun setupWidget() {
        // Setup default properties
        isClickable = true
    }

    private fun setupPaints() {
        trackPaint.color = trackColor
        fillPaint.color = fillColor
        thumbPaint.color = thumbColor
        thumbStrokePaint.apply {
            color = thumbStrokeColor
            style = Paint.Style.STROKE
            strokeWidth = thumbStrokeWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val sliderHeight = height - thumbRadius * 2
        val sliderTop = thumbRadius
        val sliderBottom = height - thumbRadius

        // Reuse RectF objects
        val trackRect = RectF(centerX - trackWidth / 2, sliderTop, centerX + trackWidth / 2, sliderBottom)
        canvas.drawRoundRect(trackRect, trackWidth / 2, trackWidth / 2, trackPaint)

        val fillHeight = (value / maxValue) * sliderHeight
        val fillTop = sliderBottom - fillHeight
        val fillRect = RectF(centerX - trackWidth / 2, fillTop, centerX + trackWidth / 2, sliderBottom)
        canvas.drawRoundRect(fillRect, trackWidth / 2, trackWidth / 2, fillPaint)

        // Draw thumb
        val thumbY = sliderBottom - fillHeight
        canvas.drawCircle(centerX, thumbY, thumbRadius, thumbPaint)
        canvas.drawCircle(centerX, thumbY, thumbRadius, thumbStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                touchStartY = event.y
                updateValueFromTouch(event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    updateValueFromTouch(event.y)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateValueFromTouch(touchY: Float) {
        val sliderHeight = height - thumbRadius * 2
        val sliderTop = thumbRadius
        val sliderBottom = height - thumbRadius

        val percentage = 1f - (touchY - sliderTop) / sliderHeight
        val newValue = (percentage * maxValue).coerceIn(minValue, maxValue)

        // Apply step size
        val steppedValue = round(newValue / stepSize) * stepSize
        value = steppedValue
    }

    fun incrementValue() {
        value += stepSize
    }

    fun decrementValue() {
        value -= stepSize
    }
}

class VerticalSliderWithIcons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val topIcon: ImageView
    private val slider: CustomVerticalSlider
    private val bottomIcon: ImageView

    var onValueChangeListener: ((Float) -> Unit)? = null
        set(value) {
            field = value
            slider.onValueChangeListener = value
        }

    var sliderValue: Float
        get() = slider.value
        set(value) {
            slider.value = value
        }

    init {
        orientation = VERTICAL
        gravity = android.view.Gravity.CENTER_HORIZONTAL

        // Create top icon
        topIcon = ImageView(context).apply {
            layoutParams = LayoutParams(
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size),
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = getSelectableItemBackground()
            setOnClickListener { slider.incrementValue() }
        }

        // Create slider
        slider = CustomVerticalSlider(context, attrs, defStyleAttr).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                0,
                1f
            )
        }

        // Create bottom icon
        bottomIcon = ImageView(context).apply {
            layoutParams = LayoutParams(
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size),
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
            ).apply {
                setMargins(0, 16, 0, 0)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = getSelectableItemBackground()
            setOnClickListener { slider.decrementValue() }
        }

        // Add views to layout
        addView(topIcon)
        addView(slider)
        addView(bottomIcon)
    }

    private fun getSelectableItemBackground() =
        ContextCompat.getDrawable(context, android.R.drawable.btn_default)

    fun setTopIcon(resId: Int) {
        topIcon.setImageResource(resId)
    }

    fun setBottomIcon(resId: Int) {
        bottomIcon.setImageResource(resId)
    }

    fun setSliderColors(trackColor: Int, fillColor: Int, thumbColor: Int) {
        slider.trackColor = trackColor
        slider.fillColor = fillColor
        slider.thumbColor = thumbColor
        slider.thumbStrokeColor = fillColor
        slider.invalidate()
    }

    fun setValueRange(min: Float, max: Float, step: Float = 1f) {
        slider.minValue = min
        slider.maxValue = max
        slider.stepSize = step
    }
}

