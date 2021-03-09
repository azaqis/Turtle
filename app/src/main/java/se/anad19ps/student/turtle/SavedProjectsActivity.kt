package se.anad19ps.student.turtle

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.File
import kotlinx.android.synthetic.main.activity_saved_projects.*
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.input_text_dialog.view.*
import kotlinx.android.synthetic.main.top_bar.*
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

class SavedProjectsActivity : AppCompatActivity() {

    companion object {
        private lateinit var savedProjectsListViewAdapter : ArrayAdapter<String>
        private lateinit var savedFilesManager : SaveFilesManager
        private lateinit var listWithNames : ArrayList<String>
    }

    /*
    TODO IN THIS FILE
     - Look over what should be private and not private
     - Delete test code
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
            createNewProject()
        }

        for(name : String in listWithNames){
            Log.e("FILE_LOG","Array with names contained name: $name")
        }

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        savedProjectsListView.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            Utils.UtilsObject.showUpdatedToast(getString(R.string.clicked_on) + ": " + savedFilesManager.getArrayWithNames()[position], this)

            val intent = Intent(this, ProgrammingActivity::class.java)

            intent.putExtra("PROJECT_NAME", savedFilesManager.getArrayWithNames()[position])
            startActivity(intent)
            finish()
        }
    }

    private fun createNewProject(){
        val dialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(dialogInputName)
        dialogInputNameBuilder.setTitle(R.string.enter_project_name)
        dialogInputNameBuilder.setMessage(R.string.please_enter_project_name)

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    //Do nothing, just close dialog
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if(dialogInputName.dialogTextFieldName.text.toString().isBlank()){
                        val dialogNameBlankWarning = android.app.AlertDialog.Builder(this)
                        dialogNameBlankWarning.setTitle(R.string.name_can_not_be_blank)
                        dialogNameBlankWarning.setMessage(R.string.name_can_not_be_blank_warning)
                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEUTRAL -> {
                                    createNewProject()
                                }
                            }
                        }
                        dialogNameBlankWarning.setNeutralButton(R.string.okay, dialogClickListener)
                        dialogNameBlankWarning.create().show()
                    }
                    else if (savedFilesManager.createNewEmptyProject(
                            dialogInputName.dialogTextFieldName.text.toString(),
                            false
                        )
                    ) {
                        val intent = Intent(this, ProgrammingActivity::class.java)

                        intent.putExtra("PROJECT_NAME", dialogInputName.dialogTextFieldName.text.toString())
                        startActivity(intent)
                        finish()
                    } else {
                        val dialogRenaming = android.app.AlertDialog.Builder(this)
                        dialogRenaming.setTitle(R.string.project_name_already_exists)
                        dialogRenaming.setMessage(R.string.do_you_want_to_override_the_existing_save_file)

                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    createNewProject()
                                }
                                DialogInterface.BUTTON_POSITIVE -> {
                                    if (savedFilesManager.createNewEmptyProject(
                                            dialogInputName.dialogTextFieldName.text.toString(),
                                            true
                                        )
                                    ) {
                                        val intent = Intent(this, ProgrammingActivity::class.java)

                                        intent.putExtra("PROJECT_NAME", dialogInputName.dialogTextFieldName.text.toString())
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        }
                        dialogRenaming.setPositiveButton(R.string.yes, dialogClickListener)
                        dialogRenaming.setNegativeButton(R.string.no, dialogClickListener)
                        dialogRenaming.create().show()
                    }
                }
            }
        }
        dialogInputNameBuilder.setPositiveButton(R.string.save, inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton(R.string.cancel, inputNameDialogClickListener)
        dialogInputNameBuilder.show()
    }
}