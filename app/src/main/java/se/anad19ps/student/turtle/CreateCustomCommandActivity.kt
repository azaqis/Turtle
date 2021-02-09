package se.anad19ps.student.turtle

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

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        buttonSave.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))

        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        buttonCancel.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
    }
}