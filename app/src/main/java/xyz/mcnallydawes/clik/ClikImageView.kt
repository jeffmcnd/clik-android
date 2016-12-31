package xyz.mcnallydawes.clik

import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.widget.ImageView

class ClikImageView : ImageView {

    val TAG = "ClikImageView"

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context): super(context)

    var activePointerId = INVALID_POINTER_ID
    var lastTouchX = 0f
    var lastTouchY = 0f
    var posX = 0f
    var posY = 0f

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if(ev == null) return super.onTouchEvent(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = ev.actionIndex
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                lastTouchX = x
                lastTouchY = y
                activePointerId = ev.getPointerId(0)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(activePointerId)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                val dx = x - lastTouchX
                val dy = y - lastTouchY

                posX += dx
                posY += dy

                alpha = 1.0f - Math.abs(posX) / 400

                invalidate()

                lastTouchX = x
                lastTouchY = y
                return true
            }
            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = ev.actionIndex
                val pointerId = ev.getPointerId(pointerIndex)

                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = ev.getX(newPointerIndex)
                    lastTouchY = ev.getY(newPointerIndex)
                    activePointerId = ev.getPointerId(newPointerIndex)
                    posX = 0f
                    posY = 0f
                    alpha = 1.0f
                }
                return true
            }
            else -> return super.onTouchEvent(ev)
        }
    }
}
