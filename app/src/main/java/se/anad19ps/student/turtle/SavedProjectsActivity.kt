package se.anad19ps.student.turtle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import java.io.File
import kotlinx.android.synthetic.main.activity_saved_projects.*
import kotlinx.android.synthetic.main.activity_select_bluetooth_device.*
import kotlinx.android.synthetic.main.drawer_layout.*
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

        for(name : String in listWithNames){
            Log.e("FILE_LOG","Array with names contaned name: $name")
        }

        /*
        savedFilesManager.getArrayWithNames()
        savedFilesManager.getArrayWithNames()
        savedFilesManager.getArrayWithNames()
        */

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        //TEST CODE
        /*
        var itemList = mutableListOf<DragDropBlock>()
        val num = 5

        for (i in 0 until num) {
            val drawable = when (i % 4) {
                0 -> R.drawable.ic_arrow_up
                1 -> R.drawable.ic_arrow_down
                2 -> R.drawable.ic_arrow_right
                else -> R.drawable.ic_arrow_left
            }
            val item = DragDropBlock(
                R.drawable.ic_drag_dots,
                drawable,
                "Insert text test $i",
                "Garbage command",
                1,
                1,
                DragDropBlock.e_type.DRIVE
            )
            itemList.add(item)
        }

        savedFilesManager.saveProject("MyProject1", itemList)
        savedFilesManager.saveProject("MyProject2", itemList)
        savedFilesManager.saveProject("MyProject3", itemList)
        savedFilesManager.saveProject("MyProject4", itemList)
        savedFilesManager.saveProject("MyProject5", itemList)
        savedFilesManager.saveProject("MyProject6", itemList)
        savedFilesManager.saveProject("MyProject7", itemList)
        savedProjectsListViewAdapter.notifyDataSetChanged()
        */
        //TEST CODE END

        savedProjectsListView.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            Toast.makeText(this, "Clicked on: " + savedFilesManager.getArrayWithNames()[position], Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProgrammingActivity::class.java)

            //val arrayWithDragAndDropBlocks = savedFilesManager.loadProject(savedFilesManager.getArrayWithNames()[position])

            //val newArray : Array<DragDropBlock>
            //newArray = arrayWithDragAndDropBlocks!!.toTypedArray()

            intent.putExtra("PROJECT_NAME", savedFilesManager.getArrayWithNames()[position])
            //intent.putExtra("PROJECT_DATA", newArray)
            startActivity(intent)
            finish()
        }
    }
}