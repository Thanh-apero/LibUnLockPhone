package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.unLockPhone.R

@SuppressLint("ClickableViewAccessibility")
class UnlockActivity @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val maxPassword = 6
    private val numberButtons = mutableMapOf<ButtonPasswords, Rect>()
    private val passwords = mutableListOf<ButtonPasswords>()
    private var circleColor: Int
    private var borderColor: Int
    private var title: String
    private var textColor: Int = Color.WHITE
    private var textSizeNumber: Float
    private var textSizeTitle: Float
    private var fontNumberFamily: String? = null
    private var fontTitleFamily: String? = null
    private var circleRadius: Float
    private var circlePasswordWidth: Int
    private var circlePasswordHeight: Int
    private val strokeWidthNumber: Float
    private val distancePasswordHideWithKeyboard: Float
    private val circlePaint = Paint()
    private val borderPaint = Paint()
    private val textNumberPaint = Paint()
    private val textPaintTitle = Paint()
    private var vectorDelete: Drawable? = null
    private var vectorPinUnSelect: Drawable? = null
    private var vectorPinSelect: Drawable? = null
    private var frameKeyboardWidth = 0
    private var frameKeyboardHeight = 0
    private var framePasswordHideWidth = 0
    private var framePasswordHideHeight = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UnlockActivity)

        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleTouch(event.x, event.y)
            }
            true
        }
        title = typedArray.getString(R.styleable.UnlockActivity_title) ?: "Enter your pin"
        circleColor = typedArray.getColor(R.styleable.UnlockActivity_circleColor, Color.parseColor("#548EFF"))
        borderColor = typedArray.getColor(R.styleable.UnlockActivity_borderColor, Color.parseColor("#6FA0FF"))
        textColor = typedArray.getColor(R.styleable.UnlockActivity_textColor, Color.WHITE)
        textSizeNumber = typedArray.getDimension(R.styleable.UnlockActivity_textNumberSize, 100f)
        textSizeTitle = typedArray.getDimension(R.styleable.UnlockActivity_textTitleSize, 50f)
        circleRadius = typedArray.getDimension(R.styleable.UnlockActivity_circleRadius, 80f)
        circlePasswordWidth = typedArray.getDimension(R.styleable.UnlockActivity_circlePasswordWidth, 30f).toInt()
        circlePasswordHeight = typedArray.getDimension(R.styleable.UnlockActivity_circlePasswordHeight, 30f).toInt()
        val pinDrawableUnselectId = typedArray.getResourceId(R.styleable.UnlockActivity_pinDrawableUnselected, 0)
        vectorPinUnSelect = if (pinDrawableUnselectId != 0) ResourcesCompat.getDrawable(resources, pinDrawableUnselectId, null) else null
        val pinDrawableSelectedId = typedArray.getResourceId(R.styleable.UnlockActivity_pinDrawableSelected, 0)
        vectorPinSelect = if (pinDrawableSelectedId != 0) ResourcesCompat.getDrawable(resources, pinDrawableSelectedId, null) else null
        val iconDeleteId = typedArray.getResourceId(R.styleable.UnlockActivity_iconDelete, 0)
        vectorDelete = if (iconDeleteId != 0) ResourcesCompat.getDrawable(resources, iconDeleteId, null) else null
        strokeWidthNumber = typedArray.getDimension(R.styleable.UnlockActivity_strokeWidth, 10f)
        distancePasswordHideWithKeyboard = typedArray.getDimension(R.styleable.UnlockActivity_distancePasswordHideWithKeyBoard, 20f)
        fontNumberFamily = typedArray.getString(R.styleable.UnlockActivity_fontNumberFamily)
        fontTitleFamily = typedArray.getString(R.styleable.UnlockActivity_fontTitleFamily)
        typedArray.recycle()

        textPaintTitle.apply {
            color = textColor
            textSize = this@UnlockActivity.textSizeTitle
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            fontTitleFamily?.let {
                typeface = ResourcesCompat.getFont(context, it.toInt())
            }
        }
        circlePaint.apply {
            color = circleColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        borderPaint.apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthNumber
            isAntiAlias = true
        }

        textNumberPaint.apply {
            color = textColor
            textSize = textSizeNumber
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            fontNumberFamily?.let {
                typeface = ResourcesCompat.getFont(context, it.toInt())
            }
        }
    }

    private fun handleTouch(x: Float, y: Float) {
        Log.d("UnlockActivity", "Touch coordinates: x=$x, y=$y $numberButtons")
        for (i in numberButtons.keys) {
            if (numberButtons[i]?.contains(x.toInt(), y.toInt()) == true) {
                Log.d("UnlockActivity", "Button clicked: $i")
                onNumberButtonClick(i)
            }
        }
    }

    private fun onNumberButtonClick(button: ButtonPasswords) {
        when (button) {
            ButtonPasswords.NUMBER_0 -> addPassword(button)
            ButtonPasswords.NUMBER_1 -> addPassword(button)
            ButtonPasswords.NUMBER_2 -> addPassword(button)
            ButtonPasswords.NUMBER_3 -> addPassword(button)
            ButtonPasswords.NUMBER_4 -> addPassword(button)
            ButtonPasswords.NUMBER_5 -> addPassword(button)
            ButtonPasswords.NUMBER_6 -> addPassword(button)
            ButtonPasswords.NUMBER_7 -> addPassword(button)
            ButtonPasswords.NUMBER_8 -> addPassword(button)
            ButtonPasswords.NUMBER_9 -> addPassword(button)
            ButtonPasswords.DELETE -> {
                passwords.removeLastOrNull()
                invalidate()
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        frameKeyboardWidth = (width * 0.8).toInt()
        frameKeyboardHeight = (height * 0.8).toInt()
        framePasswordHideWidth = (width * (maxPassword / 10f)).toInt()
        framePasswordHideHeight = (height * 0.1).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTitle(canvas)
        drawPassword(canvas, passwords)
        drawNumbers(canvas)
    }

    private fun drawNumbers(canvas: Canvas) {
        val frameRect = Rect(
            (width - frameKeyboardWidth) / 2,
            (height - frameKeyboardHeight) / 2,
            (width + frameKeyboardWidth) / 2,
            (height + frameKeyboardHeight) / 2
        )

        val numberSize = 3
        val totalRows = 4
        val widthPerCircle = (frameKeyboardWidth) / (numberSize + 1)
        val heightPerRow = (frameKeyboardHeight) / (totalRows + 1)

        val centerX0 = frameRect.left + widthPerCircle * 2
        val centerY0 = frameRect.top + heightPerRow * totalRows
        canvas.drawCircle(centerX0, centerY0, circleRadius, circlePaint)
        canvas.drawCircle(centerX0, centerY0, circleRadius, borderPaint)
        drawCenteredText(canvas, "0", centerX0, centerY0, textNumberPaint)
        val rect0 = Rect(
            (centerX0 - circleRadius).toInt(),
            (centerY0 - circleRadius).toInt(),
            (centerX0 + circleRadius).toInt(),
            (centerY0 + circleRadius).toInt()
        )
        numberButtons[ButtonPasswords.NUMBER_0] = rect0
        val centerXBitmap = frameRect.left + widthPerCircle * 3
        val centerYBitmap = frameRect.top + heightPerRow * totalRows
        val rectDelete = Rect(
            (centerXBitmap - circleRadius).toInt(),
            (centerYBitmap - circleRadius).toInt(),
            (centerXBitmap + circleRadius).toInt(),
            (centerYBitmap + circleRadius).toInt()
        )
        numberButtons[ButtonPasswords.DELETE] = rectDelete
        vectorDelete?.let {
            val bitmapWidth = circleRadius
            val bitmapHeight = circleRadius
            it.setBounds(
                centerXBitmap - bitmapWidth / 2, centerYBitmap - bitmapHeight / 2,
                centerXBitmap + bitmapWidth / 2, centerYBitmap + bitmapHeight / 2
            )
            it.draw(canvas)
        }

        for (i in 1..9) {
            val row = (i - 1) / numberSize + 1
            val col = (i - 1) % numberSize + 1

            val centerX = frameRect.left + widthPerCircle * col
            val centerY = frameRect.top + heightPerRow * row
            val rectN = Rect(
                (centerX - circleRadius).toInt(),
                (centerY - circleRadius).toInt(),
                (centerX + circleRadius).toInt(),
                (centerY + circleRadius).toInt()
            )
            when (i) {
                1 -> numberButtons[ButtonPasswords.NUMBER_1] = rectN
                2 -> numberButtons[ButtonPasswords.NUMBER_2] = rectN
                3 -> numberButtons[ButtonPasswords.NUMBER_3] = rectN
                4 -> numberButtons[ButtonPasswords.NUMBER_4] = rectN
                5 -> numberButtons[ButtonPasswords.NUMBER_5] = rectN
                6 -> numberButtons[ButtonPasswords.NUMBER_6] = rectN
                7 -> numberButtons[ButtonPasswords.NUMBER_7] = rectN
                8 -> numberButtons[ButtonPasswords.NUMBER_8] = rectN
                9 -> numberButtons[ButtonPasswords.NUMBER_9] = rectN
            }
            canvas.drawCircle(centerX, centerY, circleRadius, circlePaint)
            canvas.drawCircle(centerX, centerY, circleRadius, borderPaint)
            drawCenteredText(canvas, i.toString(), centerX, centerY, textNumberPaint)
        }
    }

    private fun drawPassword(canvas: Canvas, passwords: List<ButtonPasswords>) {
        val widthPerCircle = framePasswordHideWidth / (maxPassword + 1)
        val centerY = (height - frameKeyboardHeight - distancePasswordHideWithKeyboard)
        val size = passwords.size
        for (i in 1..maxPassword) {
            val centerX =
                (width - framePasswordHideWidth) / 2 + widthPerCircle * i
            val vectorDrawable = if (i <= size) vectorPinSelect else vectorPinUnSelect
            vectorDrawable?.let {
                it.setBounds(
                    centerX - circlePasswordWidth / 2, centerY.toInt() - circlePasswordHeight / 2,
                    centerX + circlePasswordWidth / 2, centerY.toInt() + circlePasswordHeight / 2
                )
                it.draw(canvas)
            }
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Number, y: Number, paint: Paint) {
        val textHeight = paint.descent() - paint.ascent()
        val centerX = x.toFloat()
        val centerY = y.toFloat()

        val adjustedX = centerX
        val adjustedY = centerY + textHeight / 2 - paint.descent()

        canvas.drawText(text, adjustedX, adjustedY, paint)
    }

    private fun drawTitle(canvas: Canvas){
        val centerX = width / 2
        val centerY = (height - frameKeyboardHeight - distancePasswordHideWithKeyboard) - circlePasswordHeight - distancePasswordHideWithKeyboard
        canvas.drawText(title, centerX, centerY, textPaintTitle)
    }

    private fun addPassword(password: ButtonPasswords) {
        if (passwords.size >= maxPassword) {
            passwords.removeAt(0)
        }
        passwords.add(password)
        invalidate()
    }

    fun getPasswords(): List<ButtonPasswords> {
        return passwords
    }
}

fun Canvas.drawCircle(centerX: Number, centerY: Number, radius: Number, paint: Paint) {
    this.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), paint)
}

fun Canvas.drawText(text: String, x: Number, y: Number, paint: Paint) {
    this.drawText(text, x.toFloat(), y.toFloat(), paint)
}

fun Drawable.setBounds(left: Number, top: Number, right: Number, bottom: Number) {
    this.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}