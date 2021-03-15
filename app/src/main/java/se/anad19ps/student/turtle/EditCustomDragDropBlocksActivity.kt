package se.anad19ps.student.turtle

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit_custom_dragdropblock.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class EditCustomDragDropBlocksActivity : AppCompatActivity() {

    companion object {
        private enum class OpenDialog {
            DIALOG_NAME_EXISTS, DIALOG_NAME_IS_BLANK, DIALOG_CONFIRM_DELETE, NONE
        }

        private var openDialog = OpenDialog.NONE
        private var oldDragDropBlock: DragDropBlock? = null

        private lateinit var saveCustomDragDropBlockManager: SaveCustomDragDropBlockManager
        private lateinit var saveFilesManager: SaveFilesManager

        private const val INPUT_NAME = "inputName"
        private const val PARAMETER_ENABLED = "parameterEnabled"
        private const val INPUT_COMMAND = "inputCommand"
        private const val OPEN_DIALOG = "openDialog"
        private const val OLD_DRAG_DROP_BLOCK = "oldDragDropBlock"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_custom_dragdropblock)

        saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        saveFilesManager = SaveFilesManager(this)

        HamburgerMenu().setUpHamburgerMenu(this, drawer_layout_nav_view, drawer_layout, hamburger_menu_icon)

        if (intent.hasExtra("NAME_DRAGDDROPBLOCK")) {
            val nameFromExtra = intent.getSerializableExtra("NAME_DRAGDDROPBLOCK").toString()
            oldDragDropBlock = saveCustomDragDropBlockManager.getDragDropBlockByName(nameFromExtra)

            if (oldDragDropBlock != null) {
                edit_custom_dadb_edit_text_name.setText(oldDragDropBlock!!.text)
                edit_custom_dadb_parameter_check_box.isChecked = oldDragDropBlock!!.parameterEnabled
                edit_custom_dadb_command_edit_text.setText(oldDragDropBlock!!.command)
            } else {
                //If dragDropBlock is null here the dragDropBlock does not exists. Therefore it is not possible to edit it and therefore finishing this activity. This should not be able to happen
                finish()
            }
        }

        if (savedInstanceState != null) {
            edit_custom_dadb_edit_text_name.setText(savedInstanceState.getString(INPUT_NAME))
            edit_custom_dadb_parameter_check_box.isChecked = savedInstanceState.getBoolean(
                PARAMETER_ENABLED)
            edit_custom_dadb_command_edit_text.setText(savedInstanceState.getString(INPUT_COMMAND))
            openDialog =
                savedInstanceState.getString(OPEN_DIALOG)?.let { OpenDialog.valueOf(it) }!!
            oldDragDropBlock = savedInstanceState.getParcelable(OLD_DRAG_DROP_BLOCK)

            when(openDialog){
                OpenDialog.DIALOG_NAME_EXISTS -> displayDialogNameExists()
                OpenDialog.DIALOG_NAME_IS_BLANK -> displayDialogNameIsBlank()
                OpenDialog.DIALOG_CONFIRM_DELETE -> displayDialogConfirmDelete()
                else -> {}
            }
        }
        setUpButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val inputName: String = edit_custom_dadb_edit_text_name.text.toString()
        val parameterEnabled: Boolean = edit_custom_dadb_parameter_check_box.isChecked
        val inputCommand: String = edit_custom_dadb_command_edit_text.text.toString()

        outState.putString(INPUT_NAME, inputName)
        outState.putString(PARAMETER_ENABLED, parameterEnabled.toString())
        outState.putString(INPUT_COMMAND, inputCommand)
        outState.putString(OPEN_DIALOG, openDialog.toString())
        outState.putParcelable(OLD_DRAG_DROP_BLOCK, oldDragDropBlock)
    }

    private fun setUpButtons() {
        //Set-up update button
        val buttonUpdate = findViewById<Button>(R.id.edit_custom_dadb_button_update)
        buttonUpdate.setBackgroundColor(ContextCompat.getColor(this, R.color.PrimaryColor))
        buttonUpdate.setOnClickListener {
            if (oldDragDropBlock != null) {
                val dragImage = oldDragDropBlock!!.dragImage
                val directionImage = oldDragDropBlock!!.directionImage
                val text = edit_custom_dadb_edit_text_name.text.toString()
                val command = edit_custom_dadb_command_edit_text.text.toString()
                val parameter = oldDragDropBlock!!.parameter
                val displayParameter = oldDragDropBlock!!.displayParameter
                val type = oldDragDropBlock!!.type
                val parameterEnabled = edit_custom_dadb_parameter_check_box.isChecked
                val idNumber = oldDragDropBlock!!.idNumber

                if (text.isNotBlank() && command.isNotBlank()) {
                    val updatedDragDropBlock = DragDropBlock(
                        dragImage,
                        directionImage,
                        text,
                        command,
                        parameter,
                        displayParameter,
                        type,
                        parameterEnabled,
                        idNumber
                    )

                    if (saveCustomDragDropBlockManager.editDragDropBlock(
                            oldDragDropBlock!!.text,
                            updatedDragDropBlock
                        )
                    ) {
                        saveFilesManager.updateCustomDragDropBlocksInAllProjects(
                            oldDragDropBlock!!,
                            updatedDragDropBlock
                        )
                        finish()
                    } else {
                        displayDialogNameExists()
                    }
                } else {
                    displayDialogNameIsBlank()
                }
            }
        }

        //Set-up delete button
        val buttonDelete = findViewById<Button>(R.id.edit_custom_dadb_button_delete)
        buttonDelete.setBackgroundColor(ContextCompat.getColor(this, R.color.PrimaryComplement))
        buttonDelete.setOnClickListener {
            displayDialogConfirmDelete()
        }

        //Set-up cancel button
        val buttonCancel = findViewById<Button>(R.id.edit_custom_dadb_button_cancel)
        buttonCancel.setBackgroundColor(ContextCompat.getColor(this, R.color.GreyedButton))
        buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun displayDialogNameExists() {
        openDialog = OpenDialog.DIALOG_NAME_EXISTS

        val dialogNameExists = android.app.AlertDialog.Builder(this)
        dialogNameExists.setTitle(R.string.name_already_exists)
        dialogNameExists.setMessage(R.string.please_choose_a_unique_name)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    openDialog = OpenDialog.NONE
                }
            }
        }
        dialogNameExists.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameExists.setCancelable(false)
        dialogNameExists.create().show()
    }

    private fun displayDialogNameIsBlank() {
        openDialog = OpenDialog.DIALOG_NAME_IS_BLANK

        val dialogNameIsBlank = android.app.AlertDialog.Builder(this)
        dialogNameIsBlank.setTitle(R.string.name_or_command_field_is_empty)
        dialogNameIsBlank.setMessage(R.string.name_or_command_field_is_empty_warning)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    openDialog = OpenDialog.NONE
                }
            }
        }
        dialogNameIsBlank.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameIsBlank.setCancelable(false)
        dialogNameIsBlank.create().show()
    }

    private fun displayDialogConfirmDelete() {
        openDialog = OpenDialog.DIALOG_CONFIRM_DELETE

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
                    openDialog = OpenDialog.NONE
                }
            }
        }
        dialogConfirmDelete.setPositiveButton(R.string.yes, dialogClickListener)
        dialogConfirmDelete.setNegativeButton(R.string.no, dialogClickListener)
        dialogConfirmDelete.setCancelable(false)
        dialogConfirmDelete.create().show()
    }
}