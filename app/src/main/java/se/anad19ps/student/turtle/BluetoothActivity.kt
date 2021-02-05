package se.anad19ps.student.turtle

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.transition.Slide
import kotlinx.android.synthetic.main.activity_bluetooth.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*


class BluetoothActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("BLUETOOTH", "1")
        setContentView(R.layout.activity_bluetooth)
        Log.d("BLUETOOTH", "2")
        HamburgerMenu(navView)
        Log.d("BLUETOOTH", "3")

    }

}