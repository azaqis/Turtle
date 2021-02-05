package se.anad19ps.student.turtle

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class ControllerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        val spinner = findViewById<Spinner>(R.id.spinnerView)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.controllerSpinnerView, R.layout.controller_spinner_layout
        )
        adapter.setDropDownViewResource(R.layout.controller_spinner_dropdown_layout)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val text = parent!!.getItemAtPosition(position).toString()
        Toast.makeText(parent.context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
