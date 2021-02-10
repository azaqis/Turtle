package se.anad19ps.student.turtle

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_programming.*
import kotlinx.android.synthetic.main.card_drag_drop.view.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import java.util.*
import kotlin.collections.ArrayList


class ProgrammingActivity : AppCompatActivity(), ProgrammingRecyclerAdapter.ItemClickListener {

    private var layoutManager : RecyclerView.LayoutManager? = null
    private lateinit var adapter : RecyclerView.Adapter<ProgrammingRecyclerAdapter.InnerViewHolder>
    private var itemList = mutableListOf<DragDropBlock>()
    private lateinit var itemTouchHelper : ItemTouchHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)


        /*Spinner stuff*/
        val spinnerAdapterDriving = ArrayAdapter.createFromResource(
            this,
            R.array.list, R.layout.support_simple_spinner_dropdown_item
        )
        spinnerAdapterDriving.setDropDownViewResource(R.layout.programming_spinner_driving_dropdown_layout)
        programming_spinner_driving.adapter = spinnerAdapterDriving

        val spinnerAdapterModules = ArrayAdapter.createFromResource(
            this,
            R.array.list, R.layout.support_simple_spinner_dropdown_item
        )
        spinnerAdapterModules.setDropDownViewResource(R.layout.programming_spinner_modules_dropdown_layout)
        programming_spinner_modules.adapter = spinnerAdapterModules

        val spinnerAdapterCustom = ArrayAdapter.createFromResource(
            this,
            R.array.list, R.layout.support_simple_spinner_dropdown_item
        )
        spinnerAdapterCustom.setDropDownViewResource(R.layout.programming_spinner_custom_dropdown_layout)
        programming_spinner_custom.adapter = spinnerAdapterCustom




        populateListGarbage(100)

        layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        adapter = ProgrammingRecyclerAdapter(itemList, this)
        programming_recycle_view.adapter = adapter

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)
    }

    private fun populateListGarbage(num: Int) : List<DragDropBlock>{
        itemList = ArrayList<DragDropBlock>()

        for(i in 0 until num){
            val drawable = when (i%4){
                0 -> R.drawable.ic_arrow_up
                1 -> R.drawable.ic_arrow_down
                2 -> R.drawable.ic_arrow_right
                else -> R.drawable.ic_arrow_left
            }
            val item = DragDropBlock(R.drawable.ic_drag_dots, drawable, "Insert text $i", "Garbage command")
            itemList.add(item)
        }
        return itemList
    }

    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(
            ItemTouchHelper.DOWN
        ), ItemTouchHelper.RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            var startPosition = viewHolder.adapterPosition
            var endPosition = target.adapterPosition

            Collections.swap(itemList, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true;
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            var position = viewHolder.adapterPosition

            itemList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
    }

    override fun onItemClick(position: Int, holder: View) {
        holder.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AAAAAA"))
    }

}