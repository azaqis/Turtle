package se.anad19ps.student.turtle

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*


class ControllerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        val spinnerSpinnerView = findViewById<Spinner>(R.id.spinnerView)
        val adapterSpinnerView = ArrayAdapter.createFromResource(
            this,
            R.array.controllerSpinnerView, R.layout.controller_spinner_layout
        )
        adapterSpinnerView.setDropDownViewResource(R.layout.controller_spinner_dropdown_layout)
        spinnerSpinnerView.adapter = adapterSpinnerView
        spinnerSpinnerView.onItemSelectedListener = this

        val spinnerSpinnerController = findViewById<Spinner>(R.id.spinnerController)
        val adapterSpinnerController = ArrayAdapter.createFromResource(
            this,
            R.array.controllerSpinnerController, R.layout.controller_spinner_layout
        )
        adapterSpinnerController.setDropDownViewResource(R.layout.controller_spinner_dropdown_layout)
        spinnerSpinnerController.adapter = adapterSpinnerController
        spinnerSpinnerController.onItemSelectedListener = this

    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        /*val text = parent!!.getItemAtPosition(position).toString()
        Toast.makeText(parent.context, text, Toast.LENGTH_SHORT).show()*/
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
