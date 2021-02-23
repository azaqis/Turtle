package se.anad19ps.student.turtle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class EditCustomDragDropBlocksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_custom_dragdropblock)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        val buttonUpdate = findViewById<Button>(R.id.editCustomCommandsButtonUpdate)
        buttonUpdate.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))
        buttonUpdate.setOnClickListener{
            val intent = Intent(this, ManageCustomDragDropBlocksActivity::class.java)
            startActivity(intent)
        }


        val buttonDelete = findViewById<Button>(R.id.editCustomCommandsButtonDelete)
        buttonDelete.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonDelete.setOnClickListener{
            val intent = Intent(this, ManageCustomDragDropBlocksActivity::class.java)
            startActivity(intent)
        }
    }
}