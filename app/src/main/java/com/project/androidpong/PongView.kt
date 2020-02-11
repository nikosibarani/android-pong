package com.project.androidpong

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Source from inspirecoding.app
 */

class PongView(context: Context, attr: AttributeSet?): View(context, attr) {
    private val paintBg: Paint = Paint()
    private val paintBlueFill: Paint = Paint()
    private val paintText: Paint = Paint()

    private val PLAYER_WIDTH: Int = 180
    private val PLAYER_HEIGHT: Int = 60
    private var playerX: Float = 0f

    private var touchX: Float = 0f

    private var GAME_ENABLED = false

    private var circleX = 200f
    private var circleY = 200f
    private var CIRCLE_RAD = 50f

    private var dX = 10
    private var dY = 10

    private var point = 0

    init
    {
        paintBg.color = Color.BLACK
        paintBg.style = Paint.Style.FILL

        paintBlueFill.color =  ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        paintBlueFill.style = Paint.Style.FILL

        paintText.color = Color.WHITE
        paintText.style = Paint.Style.STROKE
        paintText.textSize = 100f
    }

    //Android doesn't know the real size at start, it needs to calculate it. Once it's done, onSizeChanged() will notify you with the real size.
    //The main call to onSizeChanged() is done after the construction of your view but before the drawing. At this time the system will calculate the size of your view and notify you by calling onSizeChanged()
    //https://stackoverflow.com/questions/3526148/when-is-onsizechanged-called
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int)
    {
        super.onSizeChanged(width, height, oldwidth, oldheight)

        //Get the x position of the player
        playerX = (width / 2 - PLAYER_WIDTH / 2).toFloat()

        paintText.textSize = height / 20f
    }

    override fun onDraw(canvas: Canvas?)
    {
        super.onDraw(canvas)

        //The black background
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBg)

        canvas?.drawRect(
            playerX, // x position
            (height - PLAYER_HEIGHT).toFloat(), // y position has to be subtracted the height of the player
            playerX + PLAYER_WIDTH,
            height.toFloat(), paintBlueFill)

        canvas?.drawCircle(circleX, circleY, CIRCLE_RAD, paintBlueFill)

        canvas?.drawText(point.toString(),20f, height/ 20 - 10f, paintText)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        when (event?.action)
        {
            MotionEvent.ACTION_DOWN -> touchX = event.x
            MotionEvent.ACTION_MOVE ->
            {
                handleMove(event)
            }
        }

        return true
    }

    private fun handleMove(event: MotionEvent)
    {
        // playerX - old x position
        // touchX - the position where we have touched the screen at first time
        // event.x - the moved x position
        // touchX - event.x - the distance of the move of our touch
        playerX -= (touchX - event.x)
        touchX = event.x
        playerX = when {
            playerX < 0 -> 0f
            playerX > width - PLAYER_WIDTH -> {
                (width - PLAYER_WIDTH).toFloat()
            }
            else -> {
                playerX
            }
        }
        invalidate() // redraw the canvas, the onDraw method will be executed again
    }

    fun startGame()
    {
        GAME_ENABLED = true
        GameThread().start()
    }
    fun stopGame()
    {
        GAME_ENABLED = false
        resetGame()
        invalidate()
    }

    private fun resetGame()
    {
        circleX = 200f
        circleY = 200f
        dX = 10
        dY = 10

        point = 0
    }

    inner class GameThread : Thread() {
        override fun run() {
            while (GAME_ENABLED) {
                circleX += dX // x position of the center of the ball
                circleY += dY // y position of the center of the ball

                // Handle of the touch of the background
                if (circleX > width - CIRCLE_RAD) {
                    circleX = width - CIRCLE_RAD
                    dX *= -1
                } else if (circleX < CIRCLE_RAD) {
                    circleX = CIRCLE_RAD
                    dX *= -1
                }

                if (circleY >= height - CIRCLE_RAD - PLAYER_HEIGHT) {
                    if (circleX in playerX..playerX + PLAYER_WIDTH) {
                        circleY = height - CIRCLE_RAD - PLAYER_HEIGHT
                        dY *= -1
                        point++
                    } else {
                        resetGame()
                    }
                } else if (circleY < CIRCLE_RAD) {
                    circleY = CIRCLE_RAD
                    dY *= -1
                }

                postInvalidate()

                sleep(10)
            }
        }
    }
}