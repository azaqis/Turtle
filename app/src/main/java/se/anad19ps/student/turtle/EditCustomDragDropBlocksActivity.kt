package se.anad19ps.student.turtle

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_edit_custom_dragdropblock.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class EditCustomDragDropBlocksActivity : AppCompatActivity() {

    companion object{
        private lateinit var saveCustomDragDropBlockManager : SaveCustomDragDropBlockManager
        private var dragDropBlock : DragDropBlock? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_custom_dragdropblock)

        saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        if (intent.hasExtra("NAME_DRAGDDROPBLOCK")) {
            val nameFromExtra = intent.getSerializableExtra("NAME_DRAGDDROPBLOCK").toString()
            dragDropBlock = saveCustomDragDropBlockManager.getDragDropBlockByName(nameFromExtra)

            if(dragDropBlock != null){
                editTextDragDropBlockName.setText(dragDropBlock!!.text)
                //PARAMETER ENABLED OR NOT SHOULD BE SET HERE
                editTextDragDropBlockCommand.setText(dragDropBlock!!.command)
            }
            else{
                //If dragDropBlock is null here the dragDropBlock does not exists. Therefore it is not possible to edit it and therefore finishing this activity. This should not be able to happen
                finish()
            }
        }

        val buttonUpdate = findViewById<Button>(R.id.editCustomCommandsButtonUpdate)
        buttonUpdate.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))
        buttonUpdate.setOnClickListener {
            if (dragDropBlock != null) {
                //MIGHT DELETE ALL THESE VALS
                val dragImage = dragDropBlock!!.dragImage
                val directionImage = dragDropBlock!!.directionImage
                val text = editTextDragDropBlockName.text.toString()
                val command = editTextDragDropBlockCommand.text.toString()
                val parameter = dragDropBlock!!.parameter
                val displayParameter = dragDropBlock!!.displayParameter
                val type = dragDropBlock!!.type

                if(text.isNotBlank() && command.isNotBlank()){
                    val updatedDragDropBlock = DragDropBlock(dragImage, directionImage, text, command, parameter, displayParameter, type)

                    if (saveCustomDragDropBlockManager.editDragDropBlock(dragDropBlock!!.text, updatedDragDropBlock)){
                        finish()
                    }
                    else {
                       val dialogNameExists = android.app.AlertDialog.Builder(this)
                        dialogNameExists.setTitle(R.string.name_already_exists)
                        dialogNameExists.setMessage(R.string.please_choose_a_unique_name)
                        //Might delete this click listener
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
        }


        val buttonDelete = findViewById<Button>(R.id.editCustomCommandsButtonDelete)
        buttonDelete.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonDelete.setOnClickListener{
            val dialogConfirmDelete = android.app.AlertDialog.Builder(this)
            dialogConfirmDelete.setTitle(R.string.confirm_delete)
            dialogConfirmDelete.setMessage(R.string.confirm_delete_custom_command_warning)
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        saveCustomDragDropBlockManager.deleteCustomDragDropBlock(dragDropBlock!!.text)
                        finish()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
            dialogConfirmDelete.setPositiveButton(R.string.yes, dialogClickListener)
            dialogConfirmDelete.setNegativeButton(R.string.no, dialogClickListener)
            dialogConfirmDelete.create().show()
        }

        val buttonCancel = findViewById<Button>(R.id.editCustomCommandsButtonCancel)
        buttonCancel.setBackgroundColor(getResources().getColor(R.color.GreyedButton))
        buttonCancel.setOnClickListener{
            finish()
        }
    }
}