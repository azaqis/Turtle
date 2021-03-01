package se.anad19ps.student.turtle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_manage_custom_dragdropblocks.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*


class ManageCustomDragDropBlocksActivity : AppCompatActivity() {

    companion object{
        private var titlesList = mutableListOf<String>()
        private var descriptionList = mutableListOf<String>()
        private var imagesList = mutableListOf<Int>()
        private lateinit var customDragDropBlockManager : SaveCustomDragDropBlockManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_custom_dragdropblocks)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)
        customDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        val buttonCreate = findViewById<Button>(R.id.buttonCreate)
        buttonCreate.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))

        buttonCreate.setOnClickListener{
            val intent = Intent(this, CreateCustomDragDropBlocksActivity::class.java)
            startActivity(intent)
        }

        val buttonDelete = findViewById<Button>(R.id.buttonDelete)
        buttonDelete.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))

        //postToList()
        testCode()

        addToList(customDragDropBlockManager.loadCustomDragDropBlock("test1")!!.text, "TEST", customDragDropBlockManager.loadCustomDragDropBlock("test1")!!.directionImage)
        addToList(customDragDropBlockManager.loadCustomDragDropBlock("test2")!!.text, "TEST", customDragDropBlockManager.loadCustomDragDropBlock("test2")!!.directionImage)
        addToList(customDragDropBlockManager.loadCustomDragDropBlock("test3")!!.text, "TEST", customDragDropBlockManager.loadCustomDragDropBlock("test3")!!.directionImage)
        addToList(customDragDropBlockManager.loadCustomDragDropBlock("test4")!!.text, "TEST", customDragDropBlockManager.loadCustomDragDropBlock("test4")!!.directionImage)



        //Should change name of recycler_view to follow naming convention
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = ManageCustomDragDropBlocksRecyclerAdapter(titlesList, descriptionList, imagesList)
    }

    private fun testCode(){
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
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE
            )
            Log.e("CUSTOM_LOG", "TEST CODE ADDED A NEW DRAGDROPBLOCK WITH NAME: " + "test" + 1);
            customDragDropBlockManager.saveDragDropBlock("test$i", item, false)
        }
    }

    private fun addToList(title: String, description: String, image: Int) {
        titlesList.add(title)
        descriptionList.add(description)
        imagesList.add(image)
    }

    private fun postToList() {
        for (i in 1..25) {
            addToList("Title $i", "Description $i", R.mipmap.ic_launcher_round)
        }
    }
}