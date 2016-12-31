package xyz.mcnallydawes.clik

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
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

    interface UserChoiceListener {
        fun onYay()
        fun onNay()
    }

    var choiceListener: UserChoiceListener? = null

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

                if(posX > 400) posX = 400f
                else if(posX < -400) posX = -400f

                var imageAlpha = 0.0f + Math.abs(posX) / 400

                imageAlpha *= 255
                val intAlpha = Math.round(imageAlpha)
                val strAlpha = String.format("%02X", intAlpha)
                if(posX == 0f) {
                    clearColorFilter()
                } else if(posX < 0) {
                    setColorFilter(Color.parseColor("#${strAlpha}F44336"))
                } else {
                    setColorFilter(Color.parseColor("#${strAlpha}4CAF50"))
                }

                invalidate()

                lastTouchX = x
                lastTouchY = y
                return true
            }
            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID

                if(choiceListener != null) {
                    if(posX >= 350) {
                        choiceListener!!.onYay()
                        return true
                    } else if(posX <= -350) {
                        choiceListener!!.onNay()
                        return true
                    }
                }

                posX = 0f
                posY = 0f
                clearColorFilter()
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
                }
                return true
            }
            else -> return super.onTouchEvent(ev)
        }
    }

    fun setUserChoiceListener(activity: Activity) {
        try {
            choiceListener = activity as UserChoiceListener
        } catch(e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnUserChoiceListener")
        }
    }

}
