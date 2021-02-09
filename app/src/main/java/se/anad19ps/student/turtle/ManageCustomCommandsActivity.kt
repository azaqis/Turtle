package se.anad19ps.student.turtle

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage_custom_commands.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*


class ManageCustomCommandsActivity : AppCompatActivity() {

    private var titlesList = mutableListOf<String>()
    private var descriptionList = mutableListOf<String>()
    private var imagesList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_custom_commands)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        val buttonCreate = findViewById<Button>(R.id.button_create)
        buttonCreate.setBackgroundColor(getResources().getColor(R.color.PrimaryColor))

        val buttonDelete = findViewById<Button>(R.id.button_delete)
        buttonDelete.setBackgroundColor(getResources().getColor(R.color.PrimaryComplement))

        postToList()

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = ManageCustomCommandsRecyclerAdapter(titlesList, descriptionList, imagesList)
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