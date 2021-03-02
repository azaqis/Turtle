package se.anad19ps.student.turtle

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_custom_dragdropblock.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class CreateCustomDragDropBlocksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_custom_dragdropblock)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        val buttonSave = findViewById<Button>(R.id.createCustomCommandsButtonSave)
        buttonSave.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))
        buttonSave.setOnClickListener{
            val name = editTextDragDropBlockName.text.toString()
            val parameterEnabled = checkBox.isEnabled
            val command = editTextDragDropBlockCommand.text.toString()
            var dragDropBlock = DragDropBlock(R.drawable.ic_drag_dots, R.drawable.ic_arrow_up, name, command, 1, parameterEnabled.compareTo(false), DragDropBlock.e_type.CUSTOM)
            var saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

            if(saveCustomDragDropBlockManager.saveDragDropBlock(name, dragDropBlock, false)){
                finish()
            }
            else{
                var dialogNameExists = android.app.AlertDialog.Builder(this)
                dialogNameExists.setTitle("Do you want to save this project before opening a new project?")
                dialogNameExists.setMessage("If you don't save, all progress in the current project will be lost")
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_NEUTRAL -> {
                        }
                    }
                }
                dialogNameExists.setNeutralButton("OK", dialogClickListener)
                dialogNameExists.create().show()
            }
        }

        val buttonCancel = findViewById<Button>(R.id.createCustomCommandsButtonCancel)
        buttonCancel.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonCancel.setOnClickListener{
            finish()
        }
    }
}