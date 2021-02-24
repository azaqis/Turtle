package se.anad19ps.student.turtle

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.fragment_controller_debug.*
import kotlinx.android.synthetic.main.top_bar.*


class ControllerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, JoystickView.JoystickListener {

    companion object{
        private lateinit var bottomFragment : Fragment
        private lateinit var topFragment : Fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        setupSpinnerAdapters()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        /*val text = parent!!.getItemAtPosition(position).toString()
        Toast.makeText(parent.context, text, Toast.LENGTH_SHORT).show()*/
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun setupSpinnerAdapters(){
        val spinnerSpinnerView = findViewById<Spinner>(R.id.spinnerView)
        val adapterSpinnerView = ArrayAdapter.createFromResource(
            this,
            R.array.controllerSpinnerView, R.layout.controller_spinner_layout
        )
        adapterSpinnerView.setDropDownViewResource(R.layout.controller_spinner_dropdown_layout)
        spinnerSpinnerView.adapter = adapterSpinnerView


        spinnerSpinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                //This works JUST below for the other spinner, but this doesnt change the view or makes the current one working
                /*when (position) {
                    0 -> topFragment = ControllerDebugFragment()
                    1 -> topFragment = ControllerSpeedometerFragment()
                    2 -> topFragment = ControllerUltraSonicFragment()
                }

                var managerTopFragment = supportFragmentManager
                var transaction = managerTopFragment.beginTransaction()

                transaction.replace(R.id.fragmentTop, topFragment)
                transaction.addToBackStack(null)
                transaction.commit()*/
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        //--------------------------------------------------------------------------------------------------------------------------------


        val spinnerSpinnerController = findViewById<Spinner>(R.id.spinnerController)
        val adapterSpinnerController = ArrayAdapter.createFromResource(
            this,
            R.array.controllerSpinnerController, R.layout.controller_spinner_layout
        )
        adapterSpinnerController.setDropDownViewResource(R.layout.controller_spinner_dropdown_layout)
        spinnerSpinnerController.adapter = adapterSpinnerController


        spinnerSpinnerController.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0)
                    bottomFragment = ControllerJoystickFragment()
                else if (position == 1)
                    bottomFragment = ControllerArrowButtonsFragment()

                var managerBottomFragment = supportFragmentManager
                var transaction = managerBottomFragment.beginTransaction()

                transaction.replace(R.id.fragmentBottom, bottomFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }
    override fun onJoystickMoved(xPercentageMoved: Float, yPercentageMoved: Float) {
        Log.d("TAG", "X: $xPercentageMoved Y: $yPercentageMoved")
    }
}
