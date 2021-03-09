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
            val intent = Intent(this, ProgrammingActivity::class.java)

            intent.putExtra("PROJECT_NAME", savedFilesManager.getArrayWithNames()[position])
            startActivity(intent)
            finish()
        }
    }

    private fun createNewProject(){
        val dialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(dialogInputName)
        dialogInputNameBuilder.setTitle("Enter a project name")
        dialogInputNameBuilder.setMessage("Please enter a project name.")

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    //Do nothing, just close dialog
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if(dialogInputName.dialogTextFieldName.text.toString().isBlank()){
                        val dialogNameBlankWarning = android.app.AlertDialog.Builder(this)
                        dialogNameBlankWarning.setTitle("Name can not be blank")
                        dialogNameBlankWarning.setMessage("Please enter a name that is not blank or only containing spaces!")
                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEUTRAL -> {
                                    createNewProject()
                                }
                            }
                        }
                        dialogNameBlankWarning.setNeutralButton("OK", dialogClickListener)
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
                        dialogRenaming.setTitle("Project name exist already")
                        dialogRenaming.setMessage("Do you want to override the existing save file?")

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
                        dialogRenaming.setPositiveButton("Yes", dialogClickListener)
                        dialogRenaming.setNegativeButton("No", dialogClickListener)
                        dialogRenaming.create().show()
                    }
                }
            }
        }
        dialogInputNameBuilder.setPositiveButton("Save", inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton("Cancel", inputNameDialogClickListener)
        dialogInputNameBuilder.show()
    }
}