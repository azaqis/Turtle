package se.anad19ps.student.turtle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_manage_custom_dragdropblocks.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class ManageCustomDragDropBlocksActivity : AppCompatActivity() {

    companion object {
        private var titlesList = mutableListOf<String>()
        private var descriptionList = mutableListOf<String>()
        private var imagesList = mutableListOf<Int>()
        private lateinit var customDragDropBlockManager: SaveCustomDragDropBlockManager
        private lateinit var recyclerViewAdapter: ManageCustomDragDropBlocksRecyclerAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_custom_dragdropblocks)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburger_menu_icon)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recyclerViewAdapter =
            ManageCustomDragDropBlocksRecyclerAdapter(titlesList, descriptionList, imagesList)
        recycler_view.adapter = recyclerViewAdapter

        setUpButtons()

        customDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        populateLists()
    }

    private fun setUpButtons() {
        val buttonCreate = findViewById<Button>(R.id.buttonCreate)
        buttonCreate.setBackgroundColor(ContextCompat.getColor(this, R.color.PrimaryColor))
        buttonCreate.setOnClickListener {
            val intent = Intent(this, CreateCustomDragDropBlocksActivity::class.java)
            startActivity(intent)
        }
    }

    private fun populateLists() {
        titlesList.clear()
        descriptionList.clear()
        imagesList.clear()

        val dragDropBlockArray = customDragDropBlockManager.getArrayWithCustomDragDropBlocks()

        for (dragDropBlock: DragDropBlock in dragDropBlockArray) {
            titlesList.add(dragDropBlock.text)
            descriptionList.add(dragDropBlock.command)
            imagesList.add(dragDropBlock.directionImage)
        }
        recyclerViewAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        customDragDropBlockManager.loadCustomDragDropBlocks()
        populateLists()
    }
}