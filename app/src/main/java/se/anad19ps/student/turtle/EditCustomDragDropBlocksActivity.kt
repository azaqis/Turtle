package se.anad19ps.student.turtle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_edit_custom_dragdropblock.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class EditCustomDragDropBlocksActivity : AppCompatActivity() {

    companion object{
        private lateinit var saveCustomDragDropBlockManager : SaveCustomDragDropBlockManager
        private var dragDropBlock : DragDropBlock? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_custom_dragdropblock)

        saveCustomDragDropBlockManager = SaveCustomDragDropBlockManager(this)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        if (intent.hasExtra("NAME_DRAGDDROPBLOCK")) {
            val nameFromExtra = intent.getSerializableExtra("NAME_DRAGDDROPBLOCK").toString()
            dragDropBlock = saveCustomDragDropBlockManager.getDragDropBlockByName(nameFromExtra)

            if(dragDropBlock != null){
                editTextDragDropBlockName.setText(dragDropBlock!!.text)
                //PARAMETER ENABLED OR NOT SHOULD BE SET HERE
                editTextDragDropBlockCommand.setText(dragDropBlock!!.command)
            }
        }

        val buttonUpdate = findViewById<Button>(R.id.editCustomCommandsButtonUpdate)
        buttonUpdate.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))
        buttonUpdate.setOnClickListener{

            if(dragDropBlock != null){
                //MIGHT DELETE ALL THESE VALS
                val dragImage = dragDropBlock!!.dragImage
                val directionImage = dragDropBlock!!.directionImage
                val text = editTextDragDropBlockName.text.toString()
                val command = editTextDragDropBlockCommand.text.toString()
                val parameter = dragDropBlock!!.parameter
                val displayParameter = dragDropBlock!!.displayParameter
                val type = dragDropBlock!!.type

                val updatedDragDropBlock = DragDropBlock(dragImage, directionImage, text, command, parameter, displayParameter, type)
                saveCustomDragDropBlockManager.editDragDropBlock(dragDropBlock!!.text, updatedDragDropBlock)

                finish()
            }

        }


        val buttonDelete = findViewById<Button>(R.id.editCustomCommandsButtonDelete)
        buttonDelete.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))
        buttonDelete.setOnClickListener{
            val intent = Intent(this, ManageCustomDragDropBlocksActivity::class.java)
            startActivity(intent)
        }
    }
}