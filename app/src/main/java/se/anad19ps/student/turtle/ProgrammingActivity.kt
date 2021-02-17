package se.anad19ps.student.turtle

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_programming.*
import kotlinx.android.synthetic.main.card_drag_drop.view.*
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class ProgrammingActivity : AppCompatActivity(), ProgrammingRecyclerAdapter.ItemClickListener {

    private var layoutManager : RecyclerView.LayoutManager? = null
    private lateinit var adapter : ProgrammingRecyclerAdapter
   //private lateinit var adapter : RecyclerView.Adapter<ProgrammingRecyclerAdapter.InnerViewHolder>
    private var itemList = mutableListOf<DragDropBlock>()
    private lateinit var itemTouchHelper : ItemTouchHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        setupSpinners()

        populateListGarbage(10)

        layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        adapter = ProgrammingRecyclerAdapter(itemList, this)
        programming_recycle_view.adapter = adapter

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)




        /*Start coroutine from button click. Traverse list*/
        var job : Job? = null
        var tmpState = 0
        programming_play_button.setOnClickListener{
            if(tmpState == 0) {
                job = GlobalScope.launch(Dispatchers.Main) {
                    traverseList()
                }
            }
        }
    }

    private fun setupSpinners(){
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
            val item = DragDropBlock(
                R.drawable.ic_drag_dots,
                drawable,
                "Insert text $i",
                "Garbage command",
                4
            )
            itemList.add(item)
        }
        return itemList
    }


    /*For rearranging recyclerview*/
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
        changeItemParameterDialog(position, holder)
    }

    private fun changeItemParameterDialog(position: Int, holder: View){
        val builder = AlertDialog.Builder(this).create()
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.input_dialog_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.input_dialog_text_in)
        var alertDialog : AlertDialog
        var positiveBtn : android.widget.Button

        editText.setText(itemList[position].parameter.toString())

        builder.setTitle("Change parameter")
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "Ok"){dialog, which ->
            dialogPressOk(position, editText.text.toString().toInt())
        }
        builder.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){dialog, which ->
            //Nothing
        }
        builder.setView(dialogLayout)
        builder.show()

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*Button is enabled if no non numeric chars in editText*/
                //builder.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editText.toString().matches(Regex("[0-9]"))
            }
        })
    }

    private fun dialogPressOk(position: Int, value: Int){
        itemList[position].parameter = value
        adapter.notifyDataSetChanged()
    }

    /*How should this function work? Can it be paused? If commands are send with time parameter through bluetooth stopping will not take effect until
    * block of code is complete in robot*/
    private suspend fun traverseList(){
        val items = adapter.getList().toMutableList()
        val recycler = findViewById<RecyclerView>(R.id.programming_recycle_view)
        var savedParameterTimes = mutableListOf<Int>()

        /*Store parameter values so that they can be reinserted later*/
        for(item in items){
            savedParameterTimes.add(item.parameter)
        }

        recycler.scrollToPosition(0)

        /*Update to work for millis later*/
        items.forEachIndexed{index, dragDropBlock ->
            while (dragDropBlock.parameter > 0){
                delay(200)
                dragDropBlock.parameter--
                adapter.notifyDataSetChanged()
            }
        }

        items.forEachIndexed { index, dragDropBlock ->
            dragDropBlock.parameter = savedParameterTimes[index]
        }
        adapter.notifyDataSetChanged()
    }
}