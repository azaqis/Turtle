package se.anad19ps.student.turtle

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.coroutines.*


class ControllerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    JoystickView.JoystickListener {

    companion object {
        private lateinit var bottomFragment: Fragment
        private lateinit var topFragment: Fragment
        private var coroutine: Job? = null
        private var coroutineActive: Boolean = false
        private const val JOYSTICK_COMMAND = "9"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        setupTopFragment()
        setupSpinnerAdapters()
    }

    private fun setupSpinnerAdapters() {
        val spinnerSpinnerController = findViewById<Spinner>(R.id.controller_spinner_bottom)
        val adapterSpinnerController = ArrayAdapter.createFromResource(
            this,
            R.array.controllerSpinnerController, R.layout.controller_spinner_layout
        )
        adapterSpinnerController.setDropDownViewResource(R.layout.controller_spinner_dropdown_layout)
        spinnerSpinnerController.adapter = adapterSpinnerController

        spinnerSpinnerController.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setupItemSelected(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
    }

    private fun setupItemSelected(position: Int) {
        if (position == 0)
            bottomFragment = ControllerJoystickFragment()
        else if (position == 1)
            bottomFragment = ControllerArrowButtonsFragment()

        val managerBottomFragment = supportFragmentManager
        val transaction = managerBottomFragment.beginTransaction()

        transaction.replace(R.id.controller_fragment_bottom, bottomFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupTopFragment() {
        topFragment = ControllerDebugFragment()

        val managerTopFragment = supportFragmentManager
        val transaction = managerTopFragment.beginTransaction()

        transaction.replace(R.id.controller_fragment_top, topFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onJoystickMoved(xPercentageMoved: Int, yPercentageMoved: Int) {
        val tenthOfSecondInMS: Long = 100

        if (!coroutineActive) {
            coroutine = GlobalScope.launch {
                coroutineActive = true

                val xMovedString = getConvertedPercentageToString(xPercentageMoved)
                val yMovedString = getConvertedPercentageToString(yPercentageMoved)

                Utils.UtilsObject.bluetoothSendString(
                    "$JOYSTICK_COMMAND$xMovedString$yMovedString",
                    baseContext
                )
                delay(tenthOfSecondInMS)
                Log.d("TAG", "$JOYSTICK_COMMAND,$xMovedString,$yMovedString")

                coroutineActive = false
            }
        } else {
            Log.d("TAG", "In else from joystickmoved")
        }
    }

    private fun getConvertedPercentageToString(percentageMoved: Int) : String{
        var movedString: String = percentageMoved.toString()

        if (movedString.length == 1)
            movedString = "00$movedString"
        else if (movedString.length == 2)
            movedString = "0$movedString"

        return movedString
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
