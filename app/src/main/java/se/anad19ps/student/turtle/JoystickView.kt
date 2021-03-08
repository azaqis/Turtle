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

    private lateinit var joystickCallback: JoystickListener

    private var originX: Float = 0.0f               //X coordinate for center origin of joystick bounded area
    private var originY: Float = 0.0f               //Y coordinate for center origin of joystick bounded area
    private var radiusBoundedArea: Float = 0.0f     //Radius for bounded area
    private var joystickRadius: Float = 0.0f        //Radius of the joystick

    /*Standard constructor for creating a JoystickView object*/
    constructor(context: Context) : super(context) {
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoystickListener) {
            joystickCallback = context
        }
    }

    /*This constructor lets us add JoystickView in the xml layout. (Minimum requirements for this is Context and AttributeSet)*/
    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoystickListener) {
            joystickCallback = context
        }
    }

    private fun drawJoystick(joystickX: Float, joystickY: Float) {
        if (holder.surface.isValid) {
            /*Creates a surface area. Locks the canvas so no other code can write to the surface*/
            val canvas = holder.lockCanvas()

            /*Holds style and color for drawing shapes*/
            val paint = Paint()

            /*Color of the background area behind the joystick and joysticks bounded area*/
            canvas.drawColor(ContextCompat.getColor(this.context, R.color.PrimaryDark))

            /*Drawing shapes require paint object*/
            paint.color = ContextCompat.getColor(this.context, R.color.PrimaryColor)

            canvas.drawCircle(
                originX,
                originY,
                radiusBoundedArea,
                paint
            )   //Draw joysticks bounded area
            paint.color = ContextCompat.getColor(this.context, R.color.PrimaryLight)
            canvas.drawCircle(joystickX, joystickY, joystickRadius, paint)    //Draw joystick

            /*Unlocks the canvas so that it can be locked and written to again*/
            holder.unlockCanvasAndPost(canvas)
        }
    }

    /*Called when object is created*/
    override fun surfaceCreated(holder: SurfaceHolder) {
        originX = width.toFloat() / 2   //Set center origin to middle of area this view is placed in
        originY = height.toFloat() / 2
        radiusBoundedArea = width.coerceAtMost(height)
            .toFloat() / 3    //Removing coerceatmost makes both circles larger
        joystickRadius = width.coerceAtMost(height).toFloat() / 5

        drawJoystick(originX, originY)  //Draw joystick in the center of its bounded area
    }

    /*OnTouchListener callback. Called whenever the JoystickView is touched*/
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == this) {
            if (event != null && event.action != MotionEvent.ACTION_UP) {   //If valid press in valid area
                val distanceToPressed = kotlin.math.sqrt(
                    (event.x - originX).toDouble().pow(2.0) + (event.y - originY).toDouble()
                        .pow(2.0)
                )
                if (distanceToPressed > radiusBoundedArea) {
                    /*Get unit vector and multiply by radius. Relative to chosen origin.*/
                    val xCoordinate =
                        (radiusBoundedArea * ((event.x - originX) / distanceToPressed) + originX).toFloat()
                    val yCoordinate =
                        (radiusBoundedArea * ((event.y - originY) / distanceToPressed) + originY).toFloat()
                    drawJoystick(
                        xCoordinate,
                        yCoordinate
                    )
                    /*This function will be implemented in an activity. It is then called with these parameters whenever joystick is moved*/
                    joystickCallback.onJoystickMoved(
                        50 + (50 * (xCoordinate - originX) / radiusBoundedArea).toInt(),
                        50 + (50 * (yCoordinate - originY) / radiusBoundedArea).toInt()
                    )
                    return true
                } else {    //If press is inside joysticks bounded area
                    drawJoystick(event.x, event.y)
                    joystickCallback.onJoystickMoved(
                        50 + (50 * (event.x - originX) / radiusBoundedArea).toInt(),
                        50 + (50 * (event.y - originY) / radiusBoundedArea).toInt()
                    )
                }
            } else {    //If not valid press (example released)
                drawJoystick(originX, originY)  //Reset
                joystickCallback.onJoystickMoved(0, 0)
            }
        }
        return true
    }

    interface JoystickListener {
        /*Force user of JoystickView to implement this function. Called from onTouch*/
        fun onJoystickMoved(xPercentageMoved: Int, yPercentageMoved: Int)
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
}