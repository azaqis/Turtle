package se.anad19ps.student.turtle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment

class ControllerArrowButtonsFragment : Fragment() {

    companion object {
        private const val FORWARD_COMMAND = "2"
        private const val REVERSE_COMMAND = "8"
        private const val RIGHT_COMMAND = "6"
        private const val LEFT_COMMAND = "4"
        private const val AVOID_OBSTACLES_COMMAND = "3"
        private const val LINE_FOLLOWING_COMMAND = "1"
        private const val STOP_COMMAND = "5"
        private const val GEAR_UP_COMMAND = "u"
        private const val GEAR_DOWN_COMMAND = "d"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_controller_arrow_buttons, container, false)

        setupButtons(root)

        return root
    }

    private fun setupButtons(root: View) {
        val btnUp =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_up) as ImageButton
        val btnDown =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_down) as ImageButton
        val btnLeft =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_left) as ImageButton
        val btnRight =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_right) as ImageButton

        val btnAvoidObstacles =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_avoid_obstacles) as Button
        val btnLineFollow =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_line_follower) as Button
        val btnStop = root.findViewById<View>(R.id.fragment_controller_arrows_button_stop) as Button

        val btnGearUp =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_gear_up) as Button
        val btnGearDown =
            root.findViewById<View>(R.id.fragment_controller_arrows_button_gear_down) as Button

        btnAvoidObstacles.setBackgroundColor(
            getColor(
                root.context,
                R.color.PrimaryColor
            )
        )
        btnLineFollow.setBackgroundColor(
            getColor(
                root.context,
                R.color.PrimaryColor
            )
        )
        btnStop.setBackgroundColor(
            getColor(
                root.context,
                R.color.PrimaryComplement
            )
        )

        btnUp.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(
                FORWARD_COMMAND,
                root.context
            )    //Switch-case in arduino-code, 2 is forward, 8 is backwards etc.
        }
        btnDown.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(REVERSE_COMMAND, root.context)
        }
        btnLeft.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(LEFT_COMMAND, root.context)
        }
        btnRight.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(RIGHT_COMMAND, root.context)
        }

        btnAvoidObstacles.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(AVOID_OBSTACLES_COMMAND, root.context)
        }
        btnLineFollow.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(LINE_FOLLOWING_COMMAND, root.context)
        }
        btnStop.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(STOP_COMMAND, root.context)
        }

        btnGearUp.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(GEAR_UP_COMMAND, root.context)
        }
        btnGearDown.setOnClickListener {
            Utils.UtilsObject.vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString(GEAR_DOWN_COMMAND, root.context)
        }
    }
}