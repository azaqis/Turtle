package se.anad19ps.student.turtle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = startActivity(Intent(this, ProgrammingActivity::class.java))
        finish()
        /*toggle = ActionBarDrawerToggle(
            this,
            findViewById(R.id.drawerLayout),
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setIcon(R.drawable.hamburger_menu_icon)

        hamburgerMenuIcon.setOnClickListener {
            Log.d("najs", "asdasd")
            drawerLayout.openDrawer(GravityCompat.START)
        }

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
        Log.d("MAIN", "1")
        drawerLayout.openDrawer(GravityCompat.START)
        Log.d("MAIN", "2")
        startActivity(Intent(this, BluetoothActivity::class.java))
        Log.d("MAIN", "3")
    }
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if(toggle.onOptionsItemSelected(item)){
                return true
            }

            return super.onOptionsItemSelected(item)
        }*/


    }
}