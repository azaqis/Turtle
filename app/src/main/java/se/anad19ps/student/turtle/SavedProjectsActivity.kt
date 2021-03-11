package se.anad19ps.student.turtle

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.activity_saved_projects.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.input_text_dialog.view.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlin.collections.ArrayList

class SavedProjectsActivity : AppCompatActivity() {

    companion object {
        private lateinit var savedProjectsListViewAdapter : ArrayAdapter<String>
        private lateinit var savedFilesManager : SaveFilesManager
        private lateinit var listWithNames : ArrayList<String>
        private enum class OpenDialog{
            DIALOG_INPUT_NAME, DIALOG_NAME_BLANK_WARNING, DIALOG_NAME_EXISTS_WARNING, NONE
        }
        private var openDialog = OpenDialog.NONE

        private var inputText : String? = null
        private var inputtedTextExists : String? = null
    }

    /*
    TODO IN THIS FILE
     - Check if names is logical
     - Comment code
     - Remove static strings and link to strings file instead
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_projects)

        savedFilesManager = SaveFilesManager(this)

        listWithNames = savedFilesManager.getArrayWithNames()

        savedProjectsListViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listWithNames)
        savedProjectsListView.adapter = savedProjectsListViewAdapter

        savedProjectsCreateButton.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))

        savedProjectsCreateButton.setOnClickListener {
            displayDialogInputName(null)
        }

        for(name : String in listWithNames){
            Log.e("FILE_LOG","Array with names contained name: $name")
        }

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        savedProjectsListView.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->

            val intent = Intent(this, ProgrammingActivity::class.java)

            intent.putExtra("PROJECT_NAME", savedFilesManager.getArrayWithNames()[position])
            startActivity(intent)
            finish()
        }

        if(savedInstanceState != null){
            val stringRecived = savedInstanceState.getString("openDialog")
            openDialog = OpenDialog.valueOf(stringRecived!!)

            when(openDialog){
                OpenDialog.DIALOG_INPUT_NAME ->{
                    displayDialogInputName(savedInstanceState.getString("inputNameFromTextBox"))
                }
                OpenDialog.DIALOG_NAME_EXISTS_WARNING -> displayDialogNameExistsWarning(
                    savedInstanceState.getString("inputtedTextExists")!!
                )
                OpenDialog.DIALOG_NAME_BLANK_WARNING -> displayDialogNameBlankWarning()
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val openDialogString = openDialog.toString()
        outState.putString("openDialog", openDialogString)
        if(openDialog == OpenDialog.DIALOG_INPUT_NAME){
            //outState.putString("inputNameFromTextBox", inputText)
        }
        else if(openDialog == OpenDialog.DIALOG_NAME_EXISTS_WARNING){
            outState.putString("inputtedTextExists", inputtedTextExists)
        }
    }

    private fun displayDialogInputName(savedInputText : String?){
        openDialog = OpenDialog.DIALOG_INPUT_NAME

        val viewDialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(viewDialogInputName)
        dialogInputNameBuilder.setTitle(R.string.enter_project_name)
        dialogInputNameBuilder.setMessage(R.string.please_enter_project_name)

        if(savedInputText != null){
            viewDialogInputName!!.dialogTextFieldName.setText(savedInputText)
        }

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    openDialog = OpenDialog.NONE
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if(viewDialogInputName!!.dialogTextFieldName.text.toString().isBlank()){
                        displayDialogNameBlankWarning()
                    }
                    else if (savedFilesManager.createNewEmptyProject(
                            viewDialogInputName!!.dialogTextFieldName.text.toString(),
                            false
                        )
                    ) {
                        val intent = Intent(this, ProgrammingActivity::class.java)

                        intent.putExtra("PROJECT_NAME", viewDialogInputName!!.dialogTextFieldName.text.toString())
                        startActivity(intent)
                        finish()
                    } else {
                        displayDialogNameExistsWarning(viewDialogInputName!!.dialogTextFieldName.text.toString())
                    }
                }
            }
        }

        viewDialogInputName!!.dialogTextFieldName.doAfterTextChanged {
            inputText = viewDialogInputName!!.dialogTextFieldName.text.toString()
        }

        dialogInputNameBuilder.setPositiveButton(R.string.save, inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton(R.string.cancel, inputNameDialogClickListener)
        dialogInputNameBuilder.show()
    }

    private fun displayDialogNameBlankWarning(){
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
        dialogNameBlankWarning.create().show()
    }

    private fun displayDialogNameExistsWarning(inputNameThatExists : String){
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

                        intent.putExtra("PROJECT_NAME", inputNameThatExists)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        dialogNameExistsWarning.setPositiveButton(R.string.yes, dialogClickListener)
        dialogNameExistsWarning.setNegativeButton(R.string.no, dialogClickListener)
        dialogNameExistsWarning.create().show()
    }
}