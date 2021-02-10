package se.anad19ps.student.turtle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.google.android.material.navigation.NavigationView


class HamburgerMenu(){
    lateinit var toggle: ActionBarDrawerToggle

    fun setUpHamburgerMenu(
        con: Context,
        navView: NavigationView,
        drawerLayout: DrawerLayout,
        hamburgerMenuIcon: ImageView
    ) {

        toggle = ActionBarDrawerToggle(
            con as Activity?,
            drawerLayout,
            R.string.open,
            R.string.close
        )

        drawerLayout.addDrawerListener(toggle)

        drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(view: View, v: Float) {
                drawerLayout.visibility = View.VISIBLE
                /*
                Eftersom drawer döljs och visas fungerar inte slide funktionen. Detta borde lösas här, helst  genom att anropa orginalkoden för slide men innan det göra den vissible. Detta fick jag ej dock att fungera!

                super.onDrawerSlide(view, v)
                */
            }
            override fun onDrawerOpened(view: View) {
                //drawerLayout.visibility = View.VISIBLE
            }
            override fun onDrawerClosed(view: View) {
                drawerLayout.visibility = View.INVISIBLE
            }

            override fun onDrawerStateChanged(i: Int) {}
        })

        toggle.syncState()
        drawerLayout.visibility = View.INVISIBLE

        hamburgerMenuIcon.setOnClickListener {
            drawerLayout.visibility = View.VISIBLE
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener {



            when (it.itemId) {
                R.id.drawerItemBluetoothConnect -> {
                    val intent = Intent(con, SelectBluetoothDeviceActivity::class.java)
                    ContextCompat.startActivity(con, intent, null)
                    con.finish()
                }
                R.id.drawerItemProgramming -> {
                    val intent = Intent(con, ProgrammingActivity::class.java)
                    ContextCompat.startActivity(con, intent, null)
                    con.finish()
                }
                R.id.drawerItemRemoteController -> {
                    val intent = Intent(con, ControllerActivity::class.java)
                    ContextCompat.startActivity(con, intent, null)
                    con.finish()
                }
                R.id.drawerItemManageCommands -> {
                    val intent = Intent(con, ManageCustomCommandsActivity::class.java)
                    ContextCompat.startActivity(con, intent, null)
                    con.finish()
                }

            }
            true
        }
    }

    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
    */
}
