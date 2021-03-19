package se.anad19ps.student.turtle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.NonNull
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setupButtons()
    }

    private fun setupButtons(){
        val btnNext1 = findViewById<View>(R.id.welcome_button_next_1) as Button
        val btnNext2 = findViewById<View>(R.id.welcome_button_next_2) as Button
        val btnNext3 = findViewById<View>(R.id.welcome_button_next_3) as Button
        val btnNext4 = findViewById<View>(R.id.welcome_button_next_4) as Button

        val btnSkip1 = findViewById<View>(R.id.welcome_button_skip_1) as Button
        val btnSkip2 = findViewById<View>(R.id.welcome_button_skip_2) as Button
        val btnSkip3 = findViewById<View>(R.id.welcome_button_skip_3) as Button
        val btnSkip4 = findViewById<View>(R.id.welcome_button_skip_4) as Button

        val btnFinish = findViewById<View>(R.id.welcome_button_finish) as Button

        btnNext1.setOnClickListener{
            welcome_linear_layout_intro.visibility = View.GONE
            welcome_linear_layout_bluetooth.visibility = View.VISIBLE
        }
        btnNext2.setOnClickListener{
            welcome_linear_layout_bluetooth.visibility = View.GONE
            welcome_linear_layout_programming.visibility = View.VISIBLE
        }
        btnNext3.setOnClickListener{
            welcome_linear_layout_programming.visibility = View.GONE
            welcome_linear_layout_remote_controller.visibility = View.VISIBLE
        }
        btnNext4.setOnClickListener{
            welcome_linear_layout_remote_controller.visibility = View.GONE
            welcome_linear_layout_manage_custom_commands.visibility = View.VISIBLE
        }

        btnSkip1.setOnClickListener{
            val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSkip2.setOnClickListener{
            val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSkip3.setOnClickListener{
            val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSkip4.setOnClickListener{
            val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnFinish.setOnClickListener{
            val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}