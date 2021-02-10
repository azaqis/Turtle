package se.anad19ps.student.turtle

import android.app.ActionBar
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_programming.*
import kotlinx.android.synthetic.main.card_drag_drop.view.*
import java.util.*
import kotlin.collections.ArrayList


class ProgrammingActivity : AppCompatActivity(), RecyclerAdapter.ItemClickListener {

    private var layoutManager : RecyclerView.LayoutManager? = null
    private lateinit var adapter : RecyclerView.Adapter<RecyclerAdapter.InnerViewHolder>
    private var itemList = mutableListOf<DragDropBlock>()
    private var itemListSpinner = mutableListOf<SpinnerMoveBlock>()
    private lateinit var itemTouchHelper : ItemTouchHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        populateListGarbage(100)

        itemListSpinner.add(SpinnerMoveBlock(R.drawable.ic_arrow_up, "Move forward"))
        itemListSpinner.add(SpinnerMoveBlock(R.drawable.ic_arrow_right, "Turn right"))

        layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        adapter = RecyclerAdapter(itemList, this)
        programming_recycle_view.adapter = adapter

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)

        val spinnerAdapter = SpinnerAdapter(itemListSpinner , this)
        spinner.adapter = spinnerAdapter


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


    /*Move through list based on item time*/
    fun goThroughList(position : Int, view : View){
        for(i in 0 until itemList.size){

        }
    }

}