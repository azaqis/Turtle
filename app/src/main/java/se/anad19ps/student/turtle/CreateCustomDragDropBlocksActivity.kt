package se.anad19ps.student.turtle

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_create_custom_dragdropblock.create_custom_dadb_parameter_check_box
import kotlinx.android.synthetic.main.activity_create_custom_dragdropblock.create_custom_dadb_command_edit_text
import kotlinx.android.synthetic.main.activity_create_custom_dragdropblock.create_custom_dadb_edit_text_name
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class CreateCustomDragDropBlocksActivity : AppCompatActivity() {

    companion object {
        private lateinit var saveCustomDragDropBlockManager: SaveCustomDragDropBlockManager

        private enum class OpenDialog {
            DIALOG_NAME_EXISTS, DIALOG_NAME_IS_BLANK, NONE
        }

        private var openDialog = OpenDialog.NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_custom_dragdropblock)

        HamburgerMenu().setUpHamburgerMenu(this, drawer_layout_nav_view, drawer_layout, hamburgerMenuIcon)

        saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        if (savedInstanceState != null) {
            create_custom_dadb_edit_text_name.setText(savedInstanceState.getString("inputName"))
            create_custom_dadb_parameter_check_box.isChecked = savedInstanceState.getBoolean("parameterEnabled")
            create_custom_dadb_command_edit_text.setText(savedInstanceState.getString("inputCommand"))

            openDialog =
                savedInstanceState.getString("openDialog")?.let { OpenDialog.valueOf(it) }!!

            when(openDialog){
                OpenDialog.DIALOG_NAME_EXISTS -> displayDialogNameExists()
                OpenDialog.DIALOG_NAME_IS_BLANK -> displayDialogNameIsBlank()
                else -> {}
            }
        }

        setUpButtons()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val inputName: String = create_custom_dadb_command_edit_text.text.toString()
        val parameterEnabled: Boolean = create_custom_dadb_parameter_check_box.isChecked
        val inputCommand: String = create_custom_dadb_command_edit_text.text.toString()

        outState.putString("inputName", inputName)
        outState.putString("parameterEnabled", parameterEnabled.toString())
        outState.putString("inputCommand", inputCommand)
        outState.putString("openDialog", openDialog.toString())
    }

    private fun setUpButtons() {
        //Set-up save button
        val buttonSave = findViewById<Button>(R.id.createCustomCommandsButtonSave)
        buttonSave.setBackgroundColor(ContextCompat.getColor(this, R.color.PrimaryColor))
        buttonSave.setOnClickListener {
            val name = create_custom_dadb_edit_text_name.text.toString()
            val parameterEnabled = create_custom_dadb_parameter_check_box.isChecked
            val command = create_custom_dadb_command_edit_text.text.toString()

            if (name.isNotBlank() && command.isNotBlank()) {
                val dragDropBlock = DragDropBlock(
                    R.drawable.ic_drag_dots,
                    R.drawable.ic_custom,
                    name,
                    command,
                    1.0,
                    1.0,
                    DragDropBlock.e_type.CUSTOM,
                    parameterEnabled,
                    0
                )

                if (saveCustomDragDropBlockManager.saveDragDropBlock(dragDropBlock, false)) {
                    finish()
                } else {
                    displayDialogNameExists()
                }
            } else {
                displayDialogNameIsBlank()
            }
        }

        //Set-up cancel button
        val buttonCancel = findViewById<Button>(R.id.createCustomCommandsButtonCancel)
        buttonCancel.setBackgroundColor(ContextCompat.getColor(this, R.color.PrimaryComplement))
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
                    openDialog = Companion.OpenDialog.NONE
                }
            }
        }
        dialogNameIsBlank.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameIsBlank.setCancelable(false)
        dialogNameIsBlank.create().show()
    }
}