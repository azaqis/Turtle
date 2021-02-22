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

class SavedProjectsActivity : AppCompatActivity() {

    companion object {
        private lateinit var savedProjectsListViewAdapter : ArrayAdapter<String>
        private lateinit var savedFilesManager : SaveFilesManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_projects)

        savedFilesManager = SaveFilesManager(this)

        savedProjectsListViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, savedFilesManager.getArrayWithNames())
        savedProjectsListView.adapter = savedProjectsListViewAdapter

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        //TEST CODE
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

        savedProjectsListView.onItemClickListener = AdapterView.OnItemClickListener { _, v, position, _ ->
            Toast.makeText(this, "Clicked on: " + savedFilesManager.getArrayWithNames()[position], Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProgrammingActivity::class.java)

            val arrayWithDragAndDropBlocks = savedFilesManager.loadProject(savedFilesManager.getArrayWithNames()[position])

            val newArray : Array<DragDropBlock>
            newArray = arrayWithDragAndDropBlocks!!.toTypedArray()


            intent.putExtra("PROJECT_DATA", newArray)
            startActivity(intent)
        }

        //TEST CODE END


    }
}