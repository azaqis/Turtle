package se.anad19ps.student.turtle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import androidx.annotation.NonNull
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    private enum class IntroductionState{
        INTRODUCTION_WELCOME,
        INTRODUCTION_BLUETOOTH,
        INTRODUCTION_PROGRAMMING,
        INTRODUCTION_REMOTE_CONTROLLER,
        INTRODUCTION_CUSTOM_COMMANDS
    }

    private var introductionState = IntroductionState.INTRODUCTION_WELCOME

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        if(!isFirstTimeOpeningApp()){
            exitIntroduction()
        }

        if(savedInstanceState != null){
            introductionState = IntroductionState.valueOf(savedInstanceState.getString("INTRODUCTION_STATE")!!)
            when(introductionState){
                IntroductionState.INTRODUCTION_WELCOME ->{}
                IntroductionState.INTRODUCTION_BLUETOOTH -> showIntroductionBluetooth()
                IntroductionState.INTRODUCTION_PROGRAMMING -> showIntroductionProgramming()
                IntroductionState.INTRODUCTION_REMOTE_CONTROLLER -> showIntroductionRemoteController()
                IntroductionState.INTRODUCTION_CUSTOM_COMMANDS -> showIntroductionCustomCommands()
            }
        }
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
            showIntroductionBluetooth()
        }
        btnNext2.setOnClickListener{
            showIntroductionProgramming()
        }
        btnNext3.setOnClickListener{
            showIntroductionRemoteController()
        }
        btnNext4.setOnClickListener{
            showIntroductionCustomCommands()
        }

        btnSkip1.setOnClickListener{
            exitIntroduction()
        }
        btnSkip2.setOnClickListener{
            exitIntroduction()
        }
        btnSkip3.setOnClickListener{
            exitIntroduction()
        }
        btnSkip4.setOnClickListener{
            exitIntroduction()
        }

        btnFinish.setOnClickListener{
            exitIntroduction()
        }
    }

    private fun isFirstTimeOpeningApp() : Boolean{
        val preferences = this.getPreferences(Context.MODE_PRIVATE)
        val isFirstTimeOpeningApp = preferences.getBoolean("IS_FIRST_TIME_OPEN_APP", true)

        with(preferences.edit()){
            putBoolean("IS_FIRST_TIME_OPEN_APP", false)
            commit()
        }

        return isFirstTimeOpeningApp
    }

    private fun showIntroductionBluetooth(){
        hideAll()
        welcome_linear_layout_intro.visibility = View.GONE
        welcome_linear_layout_bluetooth.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_BLUETOOTH
    }

    private fun showIntroductionProgramming(){
        hideAll()
        welcome_linear_layout_bluetooth.visibility = View.GONE
        welcome_linear_layout_programming.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_PROGRAMMING
    }

    private fun showIntroductionRemoteController(){
        hideAll()
        welcome_linear_layout_programming.visibility = View.GONE
        welcome_linear_layout_remote_controller.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_REMOTE_CONTROLLER
    }

    private fun showIntroductionCustomCommands(){
        hideAll()
        welcome_linear_layout_remote_controller.visibility = View.GONE
        welcome_linear_layout_manage_custom_commands.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_CUSTOM_COMMANDS
    }

    private fun exitIntroduction(){
        hideAll()
        val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideAll(){
        welcome_linear_layout_intro.visibility = View.GONE
        welcome_linear_layout_bluetooth.visibility = View.GONE
        welcome_linear_layout_programming.visibility = View.GONE
        welcome_linear_layout_remote_controller.visibility = View.GONE
        welcome_linear_layout_manage_custom_commands.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("INTRODUCTION_STATE", introductionState.toString())
    }
}