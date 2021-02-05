package se.anad19ps.student.turtle

import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import android.content.Context
import java.security.AccessController.getContext


class HamburgerMenu(navView: NavigationView) : AppCompatActivity(){
    lateinit var toggle: ActionBarDrawerToggle
    init {
        Log.d("HAMBURGER", "1")
        toggle = ActionBarDrawerToggle(
            this,
            findViewById(R.id.drawerLayout),
            R.string.open,
            R.string.close
        )
        Log.d("HAMBURGER", "2")
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setIcon(R.drawable.hamburger_menu_icon)
        Log.d("HAMBURGER", "3")
        hamburgerMenuIcon.setOnClickListener {
            Log.d("najs", "asdasd")
            drawerLayout.openDrawer(GravityCompat.START)
        }
        Log.d("HAMBURGER", "4")

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item1 -> Toast.makeText(
                    applicationContext,
                    "Clicked item 1",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.item2 -> Toast.makeText(
                    applicationContext,
                    "Clicked item 2",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.item3 -> Toast.makeText(
                    applicationContext,
                    "Clicked item 3",
                    Toast.LENGTH_SHORT
                ).show()
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
