package se.anad19ps.student.turtle

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.pow


class JoystickView : SurfaceView, SurfaceHolder.Callback,
    View.OnTouchListener {

    private lateinit var joystickCallback : JoystickListener

    constructor(context: Context) : super(context) {
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoystickListener){
            joystickCallback = context
        }
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoystickListener){
            joystickCallback = context
        }
    }

    constructor(context: Context, attr: AttributeSet, int: Int) : super(context, attr, int) {
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoystickListener){
            joystickCallback = context
        }
    }

    private var originX: Float = 0.0f
    private var originY: Float = 0.0f
    private var radius: Float = 0.0f
    private var joyRadius: Float = 0.0f

    private fun setup() {
        originX = width.toFloat() / 2
        originY = height.toFloat() / 2
        radius = width.coerceAtMost(height).toFloat() / 3
        joyRadius = width.coerceAtMost(height).toFloat() / 5
    }

    private fun drawJoystick(newX: Float, newY: Float) {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas()
            val paint = Paint()

            //Background color
            canvas.drawColor(ContextCompat.getColor(this.context, R.color.PrimaryLight))
            //Background circle (big)
            paint.color = ContextCompat.getColor(this.context, R.color.PrimaryDark)
            canvas.drawCircle(originX, originY, radius, paint)
            //Joystick circle (small)
            paint.color = ContextCompat.getColor(this.context, R.color.PrimaryLight)
            canvas.drawCircle(newX, newY, joyRadius, paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        setup()
        drawJoystick(originX, originY)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == this) {
            if (event != null && event.action != MotionEvent.ACTION_UP) {
                val distanceToPressed = kotlin.math.sqrt(
                    (event.x - originX).toDouble().pow(2.0) + (event.y - originY).toDouble()
                        .pow(2.0)
                )
                if (distanceToPressed > radius) {
                    /*Get unit vector and multiply by radius. Relative to chosen origin*/
                    val xCoordinate = (radius * ((event.x - originX) / distanceToPressed) + originX).toFloat()
                    val yCoordinate = (radius * ((event.y - originY) / distanceToPressed) + originY).toFloat()
                    drawJoystick(
                        xCoordinate,
                        yCoordinate
                    )
                    /*This function will be implemented in an activity. It is then called with these parameters whenever joystick is moved*/
                    joystickCallback.onJoystickMoved(50+(50*(xCoordinate - originX)/radius).toInt(), 50+(50*(yCoordinate - originY)/radius).toInt())
                    return true
                }
                drawJoystick(event.x, event.y)
                joystickCallback.onJoystickMoved(50+(50*(event.x - originX)/radius).toInt(), 50+(50*(event.y - originY)/radius).toInt())
            } else {
                drawJoystick(originX, originY)  //Reset
                joystickCallback.onJoystickMoved(0, 0)
            }
        }
        return true
    }

    interface JoystickListener {
        fun onJoystickMoved(xPercentageMoved: Int, yPercentageMoved: Int)
    }
}