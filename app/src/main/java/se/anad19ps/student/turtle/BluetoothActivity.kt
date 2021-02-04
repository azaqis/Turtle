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
import kotlinx.android.synthetic.main.top_bar.*


class BluetoothActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)


        toggle = ActionBarDrawerToggle(this, findViewById(R.id.drawerLayout), R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setIcon(R.drawable.hamburger_menu_icon)

        hamburgerMenuIcon.setOnClickListener{
            Log.d("najs", "asdasd")
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.item1 -> Toast.makeText(applicationContext, "Clicked item 1", Toast.LENGTH_SHORT).show()
                R.id.item2 -> Toast.makeText(applicationContext, "Clicked item 2", Toast.LENGTH_SHORT).show()
                R.id.item3 -> Toast.makeText(applicationContext, "Clicked item 3", Toast.LENGTH_SHORT).show()
            }
            true
        }
        Log.d("najs", "asdasd")
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}