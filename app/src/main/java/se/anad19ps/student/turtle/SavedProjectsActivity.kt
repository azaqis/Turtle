package se.anad19ps.student.turtle

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.activity_saved_projects.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.input_text_dialog.view.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlin.collections.ArrayList

class SavedProjectsActivity : AppCompatActivity() {

    companion object {
        private enum class OpenDialog {
            DIALOG_INPUT_NAME,
            DIALOG_NAME_BLANK_WARNING,
            DIALOG_NAME_EXISTS_WARNING,
            NONE
        }

        private lateinit var savedProjectsListViewAdapter: ArrayAdapter<String>
        private lateinit var savedFilesManager: SaveFilesManager
        private lateinit var listWithNames: ArrayList<String>

        private var openDialog = OpenDialog.NONE
        private var inputText: String? = null
        private var inputtedTextExists: String? = null

        private const val OPEN_DIALOG = "openDialog"
        private const val INPUT_NAME_FROM_TEXT_BOX = "inputNameFromTextBox"
        private const val INPUT_TEXT_EXIST = "inputTextExist"
        private const val PROJECT_NAME = "PROJECT_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_projects)
        HamburgerMenu().setUpHamburgerMenu(
            this,
            drawer_layout_nav_view,
            drawer_layout,
            hamburger_menu_icon
        )


        SaveFilesManager(this)

        if (savedInstanceState != null) {
            openDialog =
                savedInstanceState.getString(OPEN_DIALOG)?.let { OpenDialog.valueOf(it) }!!

            when (openDialog) {
                OpenDialog.DIALOG_INPUT_NAME -> {
                    displayDialogInputName(savedInstanceState.getString(INPUT_NAME_FROM_TEXT_BOX))
                }
                OpenDialog.DIALOG_NAME_EXISTS_WARNING -> displayDialogNameExistsWarning(
                    savedInstanceState.getString(INPUT_TEXT_EXIST)!!
                )
                OpenDialog.DIALOG_NAME_BLANK_WARNING -> displayDialogNameBlankWarning()
                else -> {
                }
            }
        }

        listWithNames = savedFilesManager.getArrayWithNames()
        setUpButtonsAndListView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val openDialogString = openDialog.toString()
        outState.putString(OPEN_DIALOG, openDialogString)

        if (openDialog == OpenDialog.DIALOG_INPUT_NAME) {
            outState.putString(INPUT_NAME_FROM_TEXT_BOX, inputText)
        } else if (openDialog == OpenDialog.DIALOG_NAME_EXISTS_WARNING) {
            outState.putString(INPUT_TEXT_EXIST, inputtedTextExists)
        }
    }

    private fun setUpButtonsAndListView() {
        //Set-up create button
        saved_projects_create_button.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.PrimaryColor
            )
        )
        saved_projects_create_button.setOnClickListener {
            displayDialogInputName(null)
        }

        //Set-up list view
        savedProjectsListViewAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listWithNames)
        saved_projects_list_view.adapter = savedProjectsListViewAdapter
        saved_projects_list_view.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val intent = Intent(this, ProgrammingActivity::class.java)
                intent.putExtra(PROJECT_NAME, savedFilesManager.getArrayWithNames()[position])
                startActivity(intent)
                finish()
            }
    }

    private fun displayDialogInputName(savedInputText: String?) {
        openDialog = OpenDialog.DIALOG_INPUT_NAME

        val viewDialogInputName =
            LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(viewDialogInputName)
        dialogInputNameBuilder.setTitle(R.string.enter_project_name)
        dialogInputNameBuilder.setMessage(R.string.please_enter_project_name)

        if (savedInputText != null) {
            viewDialogInputName!!.input_text_dialog_layout_dialog_text_field_name.setText(
                savedInputText
            )
        }

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    openDialog = OpenDialog.NONE
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    when {
                        viewDialogInputName!!.input_text_dialog_layout_dialog_text_field_name.text.toString()
                            .isBlank() -> {
                            displayDialogNameBlankWarning()
                        }
                        savedFilesManager.createNewEmptyProject(
                            viewDialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString(),
                            false
                        ) -> {
                            val intent = Intent(this, ProgrammingActivity::class.java)
                            intent.putExtra(
                                PROJECT_NAME,
                                viewDialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString()
                            )
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            displayDialogNameExistsWarning(viewDialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString())
                        }
                    }
                }
            }
        }

        viewDialogInputName!!.input_text_dialog_layout_dialog_text_field_name.doAfterTextChanged {
            inputText =
                viewDialogInputName.input_text_dialog_layout_dialog_text_field_name.text.toString()
        }

        dialogInputNameBuilder.setPositiveButton(R.string.save, inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton(R.string.cancel, inputNameDialogClickListener)
        dialogInputNameBuilder.setCancelable(false)
        dialogInputNameBuilder.show()
    }

    private fun displayDialogNameBlankWarning() {
        openDialog = OpenDialog.DIALOG_NAME_BLANK_WARNING

        val dialogNameBlankWarning = android.app.AlertDialog.Builder(this)
        dialogNameBlankWarning.setTitle(R.string.name_can_not_be_blank)
        dialogNameBlankWarning.setMessage(R.string.name_can_not_be_blank_warning)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    displayDialogInputName(null)
                }
            }
        }
        dialogNameBlankWarning.setNeutralButton(R.string.okay, dialogClickListener)
        dialogNameBlankWarning.setCancelable(false)
        dialogNameBlankWarning.create().show()
    }

    private fun displayDialogNameExistsWarning(inputNameThatExists: String) {
        openDialog = OpenDialog.DIALOG_NAME_EXISTS_WARNING

        inputtedTextExists = inputNameThatExists

        val dialogNameExistsWarning = android.app.AlertDialog.Builder(this)
        dialogNameExistsWarning.setTitle(R.string.project_name_already_exists)
        dialogNameExistsWarning.setMessage(R.string.do_you_want_to_override_the_existing_save_file)

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEGATIVE -> {
                    displayDialogInputName(inputNameThatExists)
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if (savedFilesManager.createNewEmptyProject(
                            inputNameThatExists,
                            true
                        )
                    ) {
                        val intent = Intent(this, ProgrammingActivity::class.java)

                        intent.putExtra(PROJECT_NAME, inputNameThatExists)

                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        dialogNameExistsWarning.setPositiveButton(R.string.yes, dialogClickListener)
        dialogNameExistsWarning.setNegativeButton(R.string.no, dialogClickListener)
        dialogNameExistsWarning.setCancelable(false)
        dialogNameExistsWarning.create().show()
    }
}