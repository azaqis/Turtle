package se.anad19ps.student.turtle

import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ControllerArrowButtonsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ControllerArrowButtonsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_controller_arrow_buttons, container, false)

        initButtons(root)

        return root
    }

    private fun initButtons(root : View){
        val btnUp = root.findViewById<View>(R.id.buttonUp) as Button
        val btnDown = root.findViewById<View>(R.id.buttonDown) as Button
        val btnLeft = root.findViewById<View>(R.id.buttonLeft) as Button
        val btnRight = root.findViewById<View>(R.id.buttonRight) as Button

        val btnAvoidObstacles = root.findViewById<View>(R.id.buttonAvoidObstacles) as Button
        val btnLineFollow = root.findViewById<View>(R.id.buttonLineFollower) as Button
        val btnStop = root.findViewById<View>(R.id.buttonStop) as Button

        val btnGearUp = root.findViewById<View>(R.id.buttonGearUp) as Button
        val btnGearDown = root.findViewById<View>(R.id.buttonGearDown) as Button

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
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("2", root.context)    //Switch-case in arduino-code, 2 is forward, 8 is backwards etc.
        }
        btnDown.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("8", root.context)
        }
        btnLeft.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("4", root.context)
        }
        btnRight.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("6", root.context)
        }

        btnAvoidObstacles.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("3", root.context)
        }
        btnLineFollow.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("1", root.context)
        }
        btnStop.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("5", root.context)
        }

        btnGearUp.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("u", root.context)
        }
        btnGearDown.setOnClickListener {
            vibrate(requireView())
            Utils.UtilsObject.bluetoothSendString("d", root.context)
        }
    }

    private fun vibrate(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ControllerArrowButtonsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ControllerArrowButtonsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}