package se.anad19ps.student.turtle

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
import kotlinx.android.synthetic.main.input_text_dialog.view.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import se.anad19ps.student.turtle.Utils
import java.util.*
import kotlin.collections.ArrayList

class ProgrammingActivity : AppCompatActivity(), ProgrammingRecyclerAdapter.ItemClickListener {
    private enum class RunState {
        IDLE,
        RUNNING,
        PAUSE
    }

    private var markForDeletion = false //Marks if a click should add to deleteList
    private var deleteList = ArrayList<DragDropBlock>() //Items in this list will be deleted when delete button is pressed

    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var adapter: ProgrammingRecyclerAdapter

    private lateinit var spinnerDriveAdapter : ProgrammingSpinnerAdapter
    private lateinit var spinnerModulesAdapter : ProgrammingSpinnerAdapter
    private lateinit var spinnerCustomAdapter : ProgrammingSpinnerAdapter

    private var itemList = ArrayList<DragDropBlock>()   //List for items in RecyclerView
    private var driveBlocksSpinner = mutableListOf<DragDropBlock>() //Lists for items in spinners
    private var modulesBlocksSpinner = mutableListOf<DragDropBlock>()
    private var customBlocksSpinner = mutableListOf<DragDropBlock>()

    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var state: RunState    //State for iteration through list. Needed for play, pause and stop
    private val sem = Semaphore(1)  //Can force the state machine to halt coroutine when pausing

    private lateinit var saveFilesManager : SaveFilesManager
    private lateinit var projectName : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        setupSpinners()

        setupButtons()

        //populateListGarbage(100)

        //I added code here start
        saveFilesManager = SaveFilesManager(this)

        //Should change so that i send project name to this activity and then load the data via the savedFilesManager here instead
        var intent = getIntent()
        val a : Array<DragDropBlock> = intent.getSerializableExtra("PROJECT_DATA") as Array<DragDropBlock>
        itemList = a.toCollection(ArrayList())

        projectName = intent.getSerializableExtra("PROJECT_NAME") as String
        //notifyDataSetChanged don't do anything
        //adapter.notifyDataSetChanged()
        //I added code here end
		
		//Commented out to be able to populate from file
        //itemList = populateListGarbage(2, DragDropBlock.e_type.DRIVE)

        state = RunState.IDLE

        layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        adapter = ProgrammingRecyclerAdapter(itemList, this)
        programming_recycle_view.adapter = adapter

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)


    }

    private fun setupButtons(){
        /*Start coroutine from button click. Traverse list*/
        var job: Job? = null
        programming_play_button.setOnClickListener {
            if(state == RunState.IDLE){
                job = GlobalScope.launch(Dispatchers.Main) {
                    state = RunState.RUNNING
                    programming_play_button.setImageResource(R.drawable.ic_pause)
                    traverseList()
                }
            }
            else if (state == RunState.PAUSE) {
                if(sem.availablePermits == 0) {
                    sem.release()   //Used to avoid idle wait in traverseList
                }
                programming_play_button.setImageResource(R.drawable.ic_pause)
                state = RunState.RUNNING
            }
            else if(state == RunState.RUNNING){
                programming_play_button.setImageResource(R.drawable.ic_play_arrow)
                state = RunState.PAUSE
            }
        }
        programming_reset_btn.setOnClickListener{
            job?.cancel()
            resetListTraverse()
        }

        /*Delete button*/
        programming_delete_btn.setOnClickListener{
            if(deleteList.isNotEmpty()){
                val indexes = ArrayList<Int>()  //To record indexed that should be deleted

                itemList.forEachIndexed{index, dragDropBlock -> //Record indexes
                    if(deleteList.contains(dragDropBlock)){
                        indexes.add(index)
                    }
                }

                indexes.sort()  //Sort indexes by size
                indexes.reverse()   //So largest index is first. This way we don't need to change index after every removal

                for(i in 0 until indexes.size){
                    itemList.removeAt(indexes[i])
                    adapter.notifyItemRemoved(indexes[i])
                }

                deleteList.clear()
                markForDeletion = false //So clicks no longer marks for deletion
            }
        }

        programming_load_button.setOnClickListener {
            //loadList(populateListGarbage(20, DragDropBlock.e_type.DRIVE))

            //I know it looks like a mess below, i will clean up the code later
            var dialog = android.app.AlertDialog.Builder(this)
            dialog.setTitle("Do you want to save this project before opening a new project?")
            dialog.setMessage("If you don't save, all progress in the current project will be lost")
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> {
                        Toast.makeText(
                            this,
                            "Did not save project",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this, SavedProjectsActivity::class.java))
                    }
                    DialogInterface.BUTTON_POSITIVE -> {
                        Toast.makeText(
                            this,
                            "Saving project...",
                            Toast.LENGTH_SHORT
                        ).show()

                        val inputNameDialog = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
                        val inputNameDialogBuilder = AlertDialog.Builder(this).setView(inputNameDialog)
                        inputNameDialogBuilder.setTitle("Title from code")
                        inputNameDialogBuilder.setMessage("Message from code")
                        inputNameDialog.dialogTextFieldName.setText(projectName)

                        val inputNameDialogClickListener = DialogInterface.OnClickListener{_, which ->
                            when(which){
                                DialogInterface.BUTTON_NEUTRAL ->{
                                    Toast.makeText(this, "Cancel clicked", Toast.LENGTH_SHORT).show()
                                }
                                DialogInterface.BUTTON_POSITIVE ->{
                                    if(saveFilesManager.saveProject(inputNameDialog.dialogTextFieldName.text.toString(), itemList, false)){
                                        Toast.makeText(this, "Saved project", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, SavedProjectsActivity::class.java))
                                        finish()
                                    }
                                    else{
                                        var dialogRenaming = android.app.AlertDialog.Builder(this)
                                        dialogRenaming.setTitle("Project name exist already")
                                        dialogRenaming.setMessage("Do you want to override the existing save file?")

                                        val dialogClickListener = DialogInterface.OnClickListener{_, which ->
                                            when(which){
                                                DialogInterface.BUTTON_NEGATIVE ->{
                                                    Toast.makeText(this, "Did not overwrite save file", Toast.LENGTH_SHORT).show()
                                                }
                                                DialogInterface.BUTTON_POSITIVE ->{
                                                    Toast.makeText(this, "Overwrite save file", Toast.LENGTH_SHORT).show()
                                                    Log.e("FILE_LOG", "Overwriting: $projectName")
                                                    if(saveFilesManager.saveProject(inputNameDialog.dialogTextFieldName.text.toString(), itemList, true)){
                                                        startActivity(Intent(this, SavedProjectsActivity::class.java))
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

                        inputNameDialogBuilder.setPositiveButton("Save", inputNameDialogClickListener)
                        inputNameDialogBuilder.setNeutralButton("Cancel", inputNameDialogClickListener)
                        inputNameDialogBuilder.show()
                    }
                }
            }
            dialog.setPositiveButton("Yes", dialogClickListener)
            dialog.setNegativeButton("No", dialogClickListener)
            dialog.create().show()
        }
    }

    /*May want to limit number of chars in each spinner item. Affects the size of the spinners*/
    private fun setupSpinners() {
        driveBlocksSpinner = populateListGarbage(5, DragDropBlock.e_type.DRIVE)
        spinnerDriveAdapter = ProgrammingSpinnerAdapter(driveBlocksSpinner, this)
        spinnerDriveAdapter.setDropDownViewResource(R.layout.programming_spinner_driving_dropdown_layout)
        programming_spinner_driving.adapter = spinnerDriveAdapter

        modulesBlocksSpinner = populateListGarbage(4, DragDropBlock.e_type.MODULE)
        spinnerModulesAdapter = ProgrammingSpinnerAdapter(modulesBlocksSpinner, this)
        spinnerModulesAdapter.setDropDownViewResource(R.layout.programming_spinner_modules_dropdown_layout)
        programming_spinner_modules.adapter = spinnerModulesAdapter

        customBlocksSpinner = populateListGarbage(5, DragDropBlock.e_type.CUSTOM)
        spinnerCustomAdapter = ProgrammingSpinnerAdapter(customBlocksSpinner, this)
        spinnerCustomAdapter.setDropDownViewResource(R.layout.programming_spinner_modules_dropdown_layout)
        programming_spinner_custom.adapter = spinnerCustomAdapter

        programming_spinner_driving.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Found a bug? One item of each kind (driving, module, custom gets automatically added in the programming view even if you dont click on an item in the spinner)
                //itemList.add(parent?.getItemAtPosition(position) as DragDropBlock)
                adapter.notifyDataSetChanged()
            }
        }

        programming_spinner_modules.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //itemList.add(parent?.getItemAtPosition(position) as DragDropBlock)
                adapter.notifyDataSetChanged()
            }
        }

        programming_spinner_custom.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //itemList.add(parent?.getItemAtPosition(position) as DragDropBlock)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun populateListGarbage(num: Int, type: DragDropBlock.e_type): ArrayList<DragDropBlock> {
        val list = ArrayList<DragDropBlock>()

        for (i in 0 until num){
            val drawable = when (i % 4) {
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
                1,
                1,
                type
            )
            list.add(item)
        }
        return list
    }

    public fun loadList(list: MutableList<DragDropBlock>){
        itemList.clear()
        itemList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    /*For rearranging recyclerview*/
    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(
            ItemTouchHelper.DOWN
        ), ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            Collections.swap(itemList, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true;
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //No swipe
        }

        /*Disable longPress so longClicks are reserved for hold to delete function. Move by calling startDrag()*/
        override fun isLongPressDragEnabled(): Boolean {
            return false
        }
    }

    /*When touching dragDots*/
    override fun onDragDots(view : RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(view) //onMove from ItemTouchHelper.simpleCallback will be accessed through startDrag
    }

    /*Used to mark item for deletion*/
    override fun onItemClick(position: Int, holder: View) {
        if(markForDeletion) {
            /*If item is already added to deleteList we want to deselect it*/
            if(deleteList.contains(itemList[position])){
                holder.card_drag_drop.setCardBackgroundColor(Color.WHITE)
                holder.card_image_drag_dots.setImageResource(R.drawable.ic_drag_dots)
                deleteList.remove(itemList[position])
                if(deleteList.isEmpty()){
                    markForDeletion = false
                }
            }
            else {
                holder.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AABBCC"))
                holder.card_image_drag_dots.setImageResource(R.drawable.ic_baseline_delete_24)
                deleteList.add(itemList[position])
            }
        }
    }

    override fun onParameterButtonClicked(position: Int) {
        changeItemParameterDialog(position)
    }

    /*LongClick activates selection for deletion*/
    override fun onLongClick(position: Int, view : View) {
        /*No need to activate if already activated*/
        if(!markForDeletion) {
            view.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AABBCC"))
            view.card_image_drag_dots.setImageResource(R.drawable.ic_baseline_delete_24)
            deleteList.add(itemList[position])
            markForDeletion = true
        }
    }
    
    private fun changeItemParameterDialog(position: Int) {
        val builder = AlertDialog.Builder(this).create()
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.input_dialog_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.input_dialog_text_in)

        editText.setText(itemList[position].parameter.toString())

        builder.setTitle("Change parameter")
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->
            updateItemValue(position, editText.text.toString().toInt())
        }
        builder.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, which ->
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
                builder.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s.isNullOrBlank()
            }
        })
    }

    private fun updateItemValue(position: Int, value: Int) {
        itemList[position].parameter = value
        itemList[position].displayParameter = value
        adapter.notifyDataSetChanged()
    }

    private suspend fun traverseList() {
        val recycler = findViewById<RecyclerView>(R.id.programming_recycle_view)
        val delayTimeMillis : Long = 100

        itemList.forEachIndexed{ index, item ->
            recycler.scrollToPosition(index) //Scrolls list so that current item is on screen

            while (item.displayParameter > 0) {
                when (state) {  //State machine
                    RunState.RUNNING -> {
                        /*ADD COMMAND TO BLUETOOTH HERE?*/
                        delay(delayTimeMillis) //Will finish current 'delayTimeMillis' period before pause
                        item.displayParameter--
                        adapter.notifyDataSetChanged()
                    }
                    RunState.PAUSE -> {
                        sem.acquire()
                    }
                    else -> {state = RunState.IDLE} //So we go to a known state if something would go wrong
                }
            }
        }
        resetListTraverse()
    }

    private fun resetListTraverse(){
        if(sem.availablePermits == 0) {  //Must keep this check
            sem.release()
        }
        state = RunState.IDLE
        adapter.resetDragDropBlockParameters()
        adapter.notifyDataSetChanged()
        programming_play_button.setImageResource(R.drawable.ic_play_arrow)
    }
}