package se.anad19ps.student.turtle

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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
            val parameterEnabled = checkBox.isChecked
            val command = editTextDragDropBlockCommand.text.toString()

            if(name.isNotBlank() && command.isNotBlank()){
                var dragDropBlock = DragDropBlock(R.drawable.ic_drag_dots, R.drawable.ic_custom, name, command, 1.0, 1.0, DragDropBlock.e_type.CUSTOM,parameterEnabled)
                var saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

                if(saveCustomDragDropBlockManager.saveDragDropBlock(dragDropBlock, false)){
                  finish()
                }
                else{
                    var dialogNameExists = android.app.AlertDialog.Builder(this)
                    dialogNameExists.setTitle(R.string.title_already_exists)
                    dialogNameExists.setMessage(R.string.please_choose_a_unique_title)
                    val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEUTRAL -> {
                            }
                        }
                    }
                    dialogNameExists.setNeutralButton(R.string.okay, dialogClickListener)
                    dialogNameExists.create().show()
                }
            }
            else{
                val dialogNameIsBlank = android.app.AlertDialog.Builder(this)
                dialogNameIsBlank.setTitle(R.string.name_or_command_field_is_empty)
                dialogNameIsBlank.setMessage(R.string.name_or_command_field_is_empty_warning)
                //Might delete this click listener
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_NEUTRAL -> {
                        }
                    }
                }
                dialogNameIsBlank.setNeutralButton(R.string.okay, dialogClickListener)
                dialogNameIsBlank.create().show()
            }

        }

        val buttonCancel = findViewById<Button>(R.id.createCustomCommandsButtonCancel)
        buttonCancel.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonCancel.setOnClickListener{
            finish()
        }
    }
}