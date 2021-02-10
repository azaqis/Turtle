package se.anad19ps.student.turtle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class CreateCustomCommandActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_custom_command)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        val buttonSave = findViewById<Button>(R.id.createCustomCommandsButtonSave)
        buttonSave.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))
        buttonSave.setOnClickListener{
            finish()
        }

        val buttonCancel = findViewById<Button>(R.id.createCustomCommandsButtonCancel)
        buttonCancel.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonCancel.setOnClickListener{
            finish()
        }
    }
}