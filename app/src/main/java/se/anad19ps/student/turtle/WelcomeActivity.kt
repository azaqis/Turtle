package se.anad19ps.student.turtle

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    companion object {
        private enum class IntroductionState {
            INTRODUCTION_WELCOME,
            INTRODUCTION_BLUETOOTH,
            INTRODUCTION_PROGRAMMING,
            INTRODUCTION_REMOTE_CONTROLLER,
            INTRODUCTION_CUSTOM_COMMANDS,
            INTRODUCTION_LOCATION_INFO
        }

        private var introductionState = IntroductionState.INTRODUCTION_WELCOME
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        if (!hasGoneThroughIntroduction()) {
            exitIntroduction()
        }

        if (savedInstanceState != null) {
            introductionState =
                IntroductionState.valueOf(savedInstanceState.getString("INTRODUCTION_STATE")!!)
            when (introductionState) {
                IntroductionState.INTRODUCTION_WELCOME -> {
                }
                IntroductionState.INTRODUCTION_BLUETOOTH -> showIntroductionBluetooth()
                IntroductionState.INTRODUCTION_PROGRAMMING -> showIntroductionProgramming()
                IntroductionState.INTRODUCTION_REMOTE_CONTROLLER -> showIntroductionRemoteController()
                IntroductionState.INTRODUCTION_CUSTOM_COMMANDS -> showIntroductionCustomCommands()
                IntroductionState.INTRODUCTION_LOCATION_INFO -> showLocationInfo()
            }
        }

        welcome_linear_layout_intro.visibility = View.VISIBLE
        setupButtons()
    }

    private fun getScrollView(): ScrollView {
        return findViewById<View>(R.id.welcome_scroll_view) as ScrollView
    }

    private fun setupButtons() {
        val btnNextIntro = findViewById<View>(R.id.welcome_button_next_intro) as Button
        val btnNextBluetooth = findViewById<View>(R.id.welcome_button_next_bluetooth) as Button
        val btnNextProgramming = findViewById<View>(R.id.welcome_button_next_programming) as Button
        val btnNextRemoteController = findViewById<View>(R.id.welcome_button_next_remote_controller) as Button

        val btnSkipIntro = findViewById<View>(R.id.welcome_button_skip_intro) as Button
        val btnSkipBluetooth = findViewById<View>(R.id.welcome_button_skip_bluetooth) as Button
        val btnSkipProgramming = findViewById<View>(R.id.welcome_button_skip_programming) as Button
        val btnSkipRemoteController = findViewById<View>(R.id.welcome_button_skip_remote_controller) as Button

        val btnFinish = findViewById<View>(R.id.welcome_button_finish) as Button

        val btnLocationFinish = findViewById<View>(R.id.welcome_button_finish_api_29_or_above) as Button

        btnNextIntro.setOnClickListener {
            showIntroductionBluetooth()
        }
        btnNextBluetooth.setOnClickListener {
            showIntroductionProgramming()
        }
        btnNextProgramming.setOnClickListener {
            showIntroductionRemoteController()
        }
        btnNextRemoteController.setOnClickListener {
            showIntroductionCustomCommands()
        }

        btnSkipIntro.setOnClickListener {
            tryExitIntroduction()
        }
        btnSkipBluetooth.setOnClickListener {
            tryExitIntroduction()
        }
        btnSkipProgramming.setOnClickListener {
            tryExitIntroduction()
        }
        btnSkipRemoteController.setOnClickListener {
            tryExitIntroduction()
        }

        btnFinish.setOnClickListener {
            tryExitIntroduction()
        }

        btnLocationFinish.setOnClickListener{
            exitIntroduction()
        }
    }

    private fun tryExitIntroduction(){
        if(!isApi29OrAbove())
            exitIntroduction()
        else
            showLocationInfo()
    }

    private fun isApi29OrAbove() : Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    private fun scrollToTop() {
        getScrollView().post(Thread {
            getScrollView().smoothScrollTo(0, 0)
        })
    }

    private fun hasGoneThroughIntroduction(): Boolean {
        val preferences = this.getPreferences(Context.MODE_PRIVATE)

        return preferences.getBoolean("IS_FIRST_TIME_OPEN_APP", true)
    }

    private fun showIntroductionBluetooth() {
        hideAll()
        welcome_linear_layout_bluetooth.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_BLUETOOTH
        scrollToTop()
    }

    private fun showIntroductionProgramming() {
        hideAll()
        welcome_linear_layout_programming.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_PROGRAMMING
        scrollToTop()
    }

    private fun showIntroductionRemoteController() {
        hideAll()
        welcome_linear_layout_remote_controller.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_REMOTE_CONTROLLER
        scrollToTop()
    }

    private fun showIntroductionCustomCommands() {
        hideAll()
        welcome_linear_layout_manage_custom_commands.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_CUSTOM_COMMANDS
        scrollToTop()
    }

    private fun showLocationInfo() {
        hideAll()
        welcome_linear_layout_location_warning.visibility = View.VISIBLE
        introductionState = IntroductionState.INTRODUCTION_LOCATION_INFO
        scrollToTop()
    }

    private fun exitIntroduction() {
        hideAll()
        val preferences = this.getPreferences(Context.MODE_PRIVATE)
        with(preferences.edit()) {
            putBoolean("IS_FIRST_TIME_OPEN_APP", false)
            commit()
        }

        val intent = Intent(this, SelectBluetoothDeviceActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideAll() {
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