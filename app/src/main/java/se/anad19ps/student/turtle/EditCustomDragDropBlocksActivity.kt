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
        private var oldDragDropBlock : DragDropBlock? = null

        private enum class OpenDialog{
            DIALOG_NAME_EXISTS, DIALOG_NAME_IS_BLANK, NONE
        }

        private var openDialog = OpenDialog.NONE

        private lateinit var saveFilesManager : SaveFilesManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_custom_dragdropblock)

        saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        saveFilesManager = SaveFilesManager(this)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        if (intent.hasExtra("NAME_DRAGDDROPBLOCK")) {
            val nameFromExtra = intent.getSerializableExtra("NAME_DRAGDDROPBLOCK").toString()
            oldDragDropBlock = saveCustomDragDropBlockManager.getDragDropBlockByName(nameFromExtra)

            if(oldDragDropBlock != null){
                editTextDragDropBlockName.setText(oldDragDropBlock!!.text)
                checkBox.isChecked = oldDragDropBlock!!.parameterEnabled
                editTextDragDropBlockCommand.setText(oldDragDropBlock!!.command)
            }
            else{
                //If dragDropBlock is null here the dragDropBlock does not exists. Therefore it is not possible to edit it and therefore finishing this activity. This should not be able to happen
                finish()
            }
        }
        if(savedInstanceState != null){
            editTextDragDropBlockName.setText(savedInstanceState.getString("inputName"))
            checkBox.isChecked = savedInstanceState.getBoolean("parameterEnabled")
            editTextDragDropBlockCommand.setText(savedInstanceState.getString("inputCommand"))
            openDialog = savedInstanceState.getString("openDialog")?.let { OpenDialog.valueOf(it) }!!

            when(openDialog){
                OpenDialog.DIALOG_NAME_EXISTS -> displayDialogNameExists()
                OpenDialog.DIALOG_NAME_IS_BLANK -> displayDialogNameIsBlank()
            }
        }

        val buttonUpdate = findViewById<Button>(R.id.editCustomCommandsButtonUpdate)
        buttonUpdate.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))
        buttonUpdate.setOnClickListener {
            if (oldDragDropBlock != null) {
                //MIGHT DELETE ALL THESE VALS
                val dragImage = oldDragDropBlock!!.dragImage
                val directionImage = oldDragDropBlock!!.directionImage
                val text = editTextDragDropBlockName.text.toString()
                val command = editTextDragDropBlockCommand.text.toString()
                val parameter = oldDragDropBlock!!.parameter
                val displayParameter = oldDragDropBlock!!.displayParameter
                val type = oldDragDropBlock!!.type
                val parameterEnabled = checkBox.isChecked
                val idNumber = oldDragDropBlock!!.idNumber

                if(text.isNotBlank() && command.isNotBlank()){
                    val updatedDragDropBlock = DragDropBlock(dragImage, directionImage, text, command, parameter, displayParameter, type, parameterEnabled, idNumber)

                    if (saveCustomDragDropBlockManager.editDragDropBlock(oldDragDropBlock!!.text, updatedDragDropBlock)){
                        saveFilesManager.updateCustomDragDropBlocksInAllProjects(oldDragDropBlock!!, updatedDragDropBlock, this)
                        finish()
                    }
                    else {
                        displayDialogNameExists()
                    }
                }
                else{
                    displayDialogNameIsBlank()
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
                        saveCustomDragDropBlockManager.deleteCustomDragDropBlock(oldDragDropBlock!!.text)
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

    private fun displayDialogNameExists(){
        openDialog = OpenDialog.DIALOG_NAME_EXISTS

        val dialogNameExists = android.app.AlertDialog.Builder(this)
        dialogNameExists.setTitle(R.string.name_already_exists)
        dialogNameExists.setMessage(R.string.please_choose_a_unique_name)
        //Might delete this click listener
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    openDialog = OpenDialog.NONE
                }
            }
        }
        dialogNameExists.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameExists.create().show()
    }

    private fun displayDialogNameIsBlank(){
        openDialog = OpenDialog.DIALOG_NAME_IS_BLANK

        val dialogNameIsBlank = android.app.AlertDialog.Builder(this)
        dialogNameIsBlank.setTitle(R.string.name_or_command_field_is_empty)
        dialogNameIsBlank.setMessage(R.string.name_or_command_field_is_empty_warning)
        //Might delete this click listener
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    openDialog = OpenDialog.NONE
                }
            }
        }
        dialogNameIsBlank.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameIsBlank.create().show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val inputName : String = editTextDragDropBlockName.text.toString()
        val parameterEnabled : Boolean = checkBox.isChecked
        val inputCommand : String = editTextDragDropBlockCommand.text.toString()

        outState.putString("inputName", inputName)
        outState.putString("parameterEnabled", parameterEnabled.toString())
        outState.putString("inputCommand", inputCommand)
        outState.putString("openDialog", openDialog.toString())
    }
}