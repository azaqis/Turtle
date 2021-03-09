package se.anad19ps.student.turtle

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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
                checkBox.isChecked = dragDropBlock!!.parameterEnabled
                editTextDragDropBlockCommand.setText(dragDropBlock!!.command)
            }
            else{
                //If dragDropBlock is null here the dragDropBlock does not exists. Therefore it is not possible to edit it and therefore finishing this activity. This should not be able to happen
                finish()
            }
        }
        else if(savedInstanceState != null){
            editTextDragDropBlockName.setText(savedInstanceState.getString("inputName"))
            checkBox.isChecked = savedInstanceState.getBoolean("parameterEnabled")
            editTextDragDropBlockCommand.setText(savedInstanceState.getString("inputCommand"))
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
                val parameterEnabled = checkBox.isChecked

                if(text.isNotBlank() && command.isNotBlank()){
                    val updatedDragDropBlock = DragDropBlock(dragImage, directionImage, text, command, parameter, displayParameter, type, parameterEnabled)

                    if (saveCustomDragDropBlockManager.editDragDropBlock(dragDropBlock!!.text, updatedDragDropBlock)){
                        finish()
                    }
                    else {
                       val dialogNameExists = android.app.AlertDialog.Builder(this)
                        dialogNameExists.setTitle("Name already exists")
                        dialogNameExists.setMessage("Please choose a unique name")
                        //Might delete this click listener
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
                else{
                    val dialogNameIsBlank = android.app.AlertDialog.Builder(this)
                    dialogNameIsBlank.setTitle("Name or command field is empty")
                    dialogNameIsBlank.setMessage("Please make sure that neither name or command field is empty or only containing spaces!")
                    //Might delete this click listener
                    val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEUTRAL -> {
                            }
                        }
                    }
                    dialogNameIsBlank.setNeutralButton("OK", dialogClickListener)
                    dialogNameIsBlank.create().show()
                }
            }
        }


        val buttonDelete = findViewById<Button>(R.id.editCustomCommandsButtonDelete)
        buttonDelete.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonDelete.setOnClickListener{
            val dialogConfirmDelete = android.app.AlertDialog.Builder(this)
            dialogConfirmDelete.setTitle("Confirm deletion")
            dialogConfirmDelete.setMessage("Are you sure you want to delete this custom command?")
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
            dialogConfirmDelete.setPositiveButton("Yes", dialogClickListener)
            dialogConfirmDelete.setNegativeButton("No", dialogClickListener)
            dialogConfirmDelete.create().show()
        }

        val buttonCancel = findViewById<Button>(R.id.editCustomCommandsButtonCancel)
        buttonCancel.setBackgroundColor(getResources().getColor(R.color.GreyedButton))
        buttonCancel.setOnClickListener{
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        val inputName : String = editTextDragDropBlockName.text.toString()
        val parameterEnabled : Boolean = checkBox.isChecked
        val inputCommand : String = editTextDragDropBlockCommand.text.toString()

        outState.putString("inputName", inputName)
        outState.putString("parameterEnabled", parameterEnabled.toString())
        outState.putString("inputCommand", inputCommand)
    }
}