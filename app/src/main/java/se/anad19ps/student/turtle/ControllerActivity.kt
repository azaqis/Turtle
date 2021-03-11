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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        setupSpinnerAdapters()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun setupSpinnerAdapters() {
        topFragment = ControllerDebugFragment()

        val managerTopFragment = supportFragmentManager
        val transaction = managerTopFragment.beginTransaction()

        transaction.replace(R.id.fragmentTop, topFragment)
        transaction.addToBackStack(null)
        transaction.commit()


        val spinnerSpinnerController = findViewById<Spinner>(R.id.spinnerController)
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
                    if (position == 0)
                        bottomFragment = ControllerJoystickFragment()
                    else if (position == 1)
                        bottomFragment = ControllerArrowButtonsFragment()

                    val managerBottomFragment = supportFragmentManager
                    val transaction = managerBottomFragment.beginTransaction()

                    transaction.replace(R.id.fragmentBottom, bottomFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
    }

    override fun onJoystickMoved(xPercentageMoved: Int, yPercentageMoved: Int) {
        val tenthOfSecondInMS: Long = 100

        if (!coroutineActive) {
            coroutine = GlobalScope.launch {
                coroutineActive = true

                var xMovedString: String = xPercentageMoved.toString()
                var yMovedString: String = yPercentageMoved.toString()

                if (xMovedString.length == 1)
                    xMovedString = "00$xMovedString"
                else if (xMovedString.length == 2)
                    xMovedString = "0$xMovedString"

                if (yMovedString.length == 1)
                    yMovedString = "00$yMovedString"
                else if (yMovedString.length == 2)
                    yMovedString = "0$yMovedString"

                Utils.UtilsObject.bluetoothSendString("9$xMovedString$yMovedString", baseContext)
                delay(tenthOfSecondInMS)
                Log.d("TAG", "9,$xMovedString,$yMovedString")

                coroutineActive = false
            }
        } else {
            Log.d("TAG", "In else from joystickmoved")
        }
    }
}
