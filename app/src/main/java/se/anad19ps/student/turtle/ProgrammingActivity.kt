package se.anad19ps.student.turtle

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
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
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProgrammingActivity : AppCompatActivity(), ProgrammingRecyclerAdapter.ItemClickListener {
    private enum class RunState {
        IDLE,
        RUNNING,
        PAUSE
    }

    private val newProjectStandardName = "New Project"

    private var markForDeletion = false //Marks if a click should add to deleteList
    private var deleteList = HashMap<DragDropBlock, View>()    //Also holds View for individual recycler item to reset colors

    private lateinit var adapter: ProgrammingRecyclerAdapter

    private lateinit var spinnerDriveAdapter: ProgrammingSpinnerAdapter
    private lateinit var spinnerModulesAdapter: ProgrammingSpinnerAdapter
    private lateinit var spinnerCustomAdapter: ProgrammingSpinnerAdapter

    private var itemList = ArrayList<DragDropBlock>()   //List for items in RecyclerView

    private var driveBlocksSpinnerList =
        mutableListOf<DragDropBlock>() //Lists for items in spinners
    private var modulesBlocksSpinnerList = mutableListOf<DragDropBlock>()
    private var customBlocksSpinnerList = mutableListOf<DragDropBlock>()

    private lateinit var itemTouchHelper: ItemTouchHelper   //For RecyclerView drag and drop. Receives events from RecyclerView

    private lateinit var state: RunState    //State for iteration through list. Needed for play, pause and stop
    private val sem = Semaphore(1)  //Can force the state machine to halt coroutine when pausing

    private lateinit var saveFilesManager: SaveFilesManager
    private lateinit var projectName: String
    private lateinit var customCommandManager: SaveCustomDragDropBlockManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programming)

        HamburgerMenu().setUpHamburgerMenu(this, navView, drawerLayout, hamburgerMenuIcon)

        Thread(Runnable {    //Setting up spinners and buttons slowed down startup of entire activity
            setupSpinners()
            setupButtons()
        }).start()

        saveFilesManager = SaveFilesManager(this)
        customCommandManager = SaveCustomDragDropBlockManager(this)

        val intent = intent

        if (intent.hasExtra("PROJECT_NAME")) {
            projectName = intent.getSerializableExtra("PROJECT_NAME") as String
            itemList = saveFilesManager.loadProject(projectName)
        } else {
            val lastOpenProject = saveFilesManager.getNameOfLastOpenedProject()
            if (lastOpenProject != null) {
                itemList = saveFilesManager.loadProject(lastOpenProject)
                projectName = lastOpenProject
            } else {
                projectName = newProjectStandardName
            }

        }

        state = RunState.IDLE

        /*Setting RecyclerView layout to linear*/
        val layoutManager = LinearLayoutManager(this)
        programming_recycle_view.layoutManager = layoutManager

        /*Restore saved state if there is any. Affects runtime configuration changes*/
        if (savedInstanceState != null) {
            itemList =
                savedInstanceState.getParcelableArrayList<DragDropBlock>("itemList") as ArrayList<DragDropBlock>
        }

        adapter = ProgrammingRecyclerAdapter(itemList, this)
        programming_recycle_view.adapter = adapter

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(programming_recycle_view)
    }

    /*Save necessary states and variables for run time configuration changes*/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("itemList", itemList)
    }

    override fun onStart() {
        super.onStart()
        programming_text_view_current_project.text = projectName
    }

    private fun setupButtons() {
        /*Start coroutine from button click. Traverse list*/
        var job: Job? = null

        /*Play button*/
        programming_play_button.setOnClickListener {
            if (state == RunState.IDLE) {
                job = GlobalScope.launch(Dispatchers.Main) {
                    state = RunState.RUNNING
                    programming_play_button.setImageResource(R.drawable.ic_pause)
                    traverseList()
                }
            } else if (state == RunState.PAUSE) {
                if (sem.availablePermits == 0) {
                    sem.release()   //Used to avoid idle wait in traverseList
                }
                programming_play_button.setImageResource(R.drawable.ic_pause)
                state = RunState.RUNNING
            } else if (state == RunState.RUNNING) {
                programming_play_button.setImageResource(R.drawable.ic_play_arrow)
                state = RunState.PAUSE
            }
        }

        /*Reset button*/
        programming_reset_btn.setOnClickListener {  //Stop coroutine and reset list traversal
            job?.cancel()
            resetListTraverse()
        }

        /*Delete button*/
        programming_delete_btn.setOnClickListener {
            if (deleteList.isNotEmpty()) {
                val indexes = ArrayList<Int>()  //To record indexed that should be deleted

                itemList.forEachIndexed { index, dragDropBlock -> //Record indexes
                    if (deleteList.contains(dragDropBlock)) {
                        indexes.add(index)
                    }
                }

                indexes.sort()  //Sort indexes by size
                indexes.reverse()   //So largest index is first. This way we don't need to change index after every removal

                for (i in 0 until indexes.size) {
                    deleteList[itemList[indexes[i]]]?.card_drag_drop?.setCardBackgroundColor(Color.WHITE)   //Reset holders to standard color
                    itemList.removeAt(indexes[i])
                    adapter.notifyItemRemoved(indexes[i])
                }

                deleteList.clear()
                markForDeletion = false //So clicks no longer marks for deletion
            }
            /* Causes a bug, don't know why. Code commented out should only run if no DragDropBlock is selected, but for some reason this code always runs when clicking trashcan even if items is selected.
            else{
                if(projectName != newProjectStandardName || itemList.isNotEmpty()){
                    deleteProject()
                }
                else{
                    Utils.UtilsObject.showUpdatedToast("Project is empty, nothing to delete", this)
                }

            }

             */
        }

        /*Load button*/
        programming_load_button.setOnClickListener {
            val newIntent = Intent(this, SavedProjectsActivity::class.java)

            if (projectName != newProjectStandardName || itemList.isNotEmpty()) {
                askUserForSavingProjectAndChangeActivity(newIntent)
            } else {
                startActivity(newIntent)
            }

        }

        programming_save_button.setOnClickListener {
            if (projectName != newProjectStandardName || itemList.isNotEmpty()) {
                saveProject()
            } else {
                Utils.UtilsObject.showUpdatedToast(
                    "This is an unsaved empty project. No need to save until changes has been made",
                    this
                )
            }

        }
    }

    private fun deleteProject() {
        val dialogWantToSave = android.app.AlertDialog.Builder(this)
        dialogWantToSave.setTitle("Do you want to delete this project?")
        dialogWantToSave.setMessage("All progress for this project will be lost.")
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEGATIVE -> {
                    Utils.UtilsObject.showUpdatedToast("Project not deleted", this)
                    Log.e("FILE_LOG", "No clicked, Project not deleted")
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    Utils.UtilsObject.showUpdatedToast("Project deleted", this)
                    Log.e("FILE_LOG", "Yes clicked, project deleted")

                    saveFilesManager.deleteProject(projectName)
                    itemList.clear()
                    projectName = newProjectStandardName
                    programming_text_view_current_project.text = projectName
                    adapter.notifyDataSetChanged()
                }
            }
        }
        dialogWantToSave.setPositiveButton("Yes", dialogClickListener)
        dialogWantToSave.setNegativeButton("No", dialogClickListener)
        dialogWantToSave.create().show()


    }

    // This and saveProjectAndChangeActivity() are basically the same code, could reuse code if i could make a return inside the dialog click listeners, but that seems to not be possible
    private fun saveProject() {
        val dialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(dialogInputName)
        dialogInputNameBuilder.setTitle("Enter a project name")
        dialogInputNameBuilder.setMessage("Please enter a project name. If you don't want to overwrite, please enter a unique name")
        dialogInputName.dialogTextFieldName.setText(projectName)

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    Utils.UtilsObject.showUpdatedToast("Project not saved", this)
                    Log.e("FILE_LOG", "Cancel clicked, project not saves")
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if (dialogInputName.dialogTextFieldName.text.toString().isBlank()) {
                        val dialogNameBlankWarning = android.app.AlertDialog.Builder(this)
                        dialogNameBlankWarning.setTitle("Name can not be blank")
                        dialogNameBlankWarning.setMessage("Please enter a name that is not blank or only containing spaces!")
                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEUTRAL -> {
                                    saveProject()
                                }
                            }
                        }
                        dialogNameBlankWarning.setNeutralButton("OK", dialogClickListener)
                        dialogNameBlankWarning.create().show()
                    } else if (saveFilesManager.saveProject(
                            dialogInputName.dialogTextFieldName.text.toString(),
                            itemList,
                            false
                        )
                    ) {
                        Toast.makeText(this, "Saved project", Toast.LENGTH_SHORT).show()
                        projectName = dialogInputName.dialogTextFieldName.text.toString()
                        programming_text_view_current_project.text = projectName
                    } else {
                        val dialogRenaming = android.app.AlertDialog.Builder(this)
                        dialogRenaming.setTitle("Project name exist already")
                        dialogRenaming.setMessage("Do you want to override the existing save file?")

                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    saveProject()
                                }
                                DialogInterface.BUTTON_POSITIVE -> {
                                    if (saveFilesManager.saveProject(
                                            dialogInputName.dialogTextFieldName.text.toString(),
                                            itemList,
                                            true
                                        )
                                    ) {
                                        Utils.UtilsObject.showUpdatedToast(
                                            "Overwriting $projectName",
                                            this
                                        )
                                        Log.e("FILE_LOG", "Overwriting: $projectName")
                                        projectName =
                                            dialogInputName.dialogTextFieldName.text.toString()
                                        programming_text_view_current_project.text = projectName
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
        dialogInputNameBuilder.setPositiveButton("Save", inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton("Cancel", inputNameDialogClickListener)
        dialogInputNameBuilder.show()
    }

    //Asking user if it wants to save project, and then changes activity.
    private fun askUserForSavingProjectAndChangeActivity(intent: Intent) {
        val dialogWantToSave = android.app.AlertDialog.Builder(this)
        dialogWantToSave.setTitle("Do you want to save this project before opening a new project?")
        dialogWantToSave.setMessage("If you don't save, all progress in the current project will be lost")
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEGATIVE -> {
                    Toast.makeText(
                        this,
                        "Did not save project",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(intent)
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    saveProjectAndChangeActivity(intent)
                }
            }
        }
        dialogWantToSave.setPositiveButton("Yes", dialogClickListener)
        dialogWantToSave.setNegativeButton("No", dialogClickListener)
        dialogWantToSave.create().show()
    }

    //Asking user for a name, saving project with that name and cahnging activity. If name exist, asking user for a overwriting. Otherwise asking user for a new name
    private fun saveProjectAndChangeActivity(intent: Intent) {
        val dialogInputName = LayoutInflater.from(this).inflate(R.layout.input_text_dialog, null)
        val dialogInputNameBuilder = AlertDialog.Builder(this).setView(dialogInputName)
        dialogInputNameBuilder.setTitle("Enter a project name")
        dialogInputNameBuilder.setMessage("Please enter a project name. If you don't want to overwrite, please enter a unique name")
        dialogInputName.dialogTextFieldName.setText(projectName)

        val inputNameDialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    Toast.makeText(this, "Cancel clicked", Toast.LENGTH_SHORT).show()
                }
                DialogInterface.BUTTON_POSITIVE -> {
                    if (dialogInputName.dialogTextFieldName.text.toString().isBlank()) {
                        val dialogNameBlankWarning = android.app.AlertDialog.Builder(this)
                        dialogNameBlankWarning.setTitle("Name can not be blank")
                        dialogNameBlankWarning.setMessage("Please enter a name that is not blank or only containing spaces!")
                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEUTRAL -> {
                                    saveProject()
                                }
                            }
                        }
                        dialogNameBlankWarning.setNeutralButton("OK", dialogClickListener)
                        dialogNameBlankWarning.create().show()
                    } else if (saveFilesManager.saveProject(
                            dialogInputName.dialogTextFieldName.text.toString(),
                            itemList,
                            false
                        )
                    ) {
                        Toast.makeText(this, "Saved project", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, SavedProjectsActivity::class.java))
                        finish()
                    } else {
                        val dialogRenaming = android.app.AlertDialog.Builder(this)
                        dialogRenaming.setTitle("Project name exist already")
                        dialogRenaming.setMessage("Do you want to override the existing save file?")

                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    Toast.makeText(
                                        this,
                                        "Did not overwrite save file",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    saveProjectAndChangeActivity(intent)
                                }
                                DialogInterface.BUTTON_POSITIVE -> {
                                    Toast.makeText(this, "Overwrite save file", Toast.LENGTH_SHORT)
                                        .show()
                                    Log.e("FILE_LOG", "Overwriting: $projectName")
                                    if (saveFilesManager.saveProject(
                                            dialogInputName.dialogTextFieldName.text.toString(),
                                            itemList,
                                            true
                                        )
                                    ) {
                                        startActivity(intent)
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
        dialogInputNameBuilder.setPositiveButton("Save", inputNameDialogClickListener)
        dialogInputNameBuilder.setNeutralButton("Cancel", inputNameDialogClickListener)
        dialogInputNameBuilder.show()
    }

    private fun populateList(
        num: Int,
        type: DragDropBlock.e_type
    ): ArrayList<DragDropBlock> {
        val list = ArrayList<DragDropBlock>()

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
                "Some text",
                "Command",
                1.0,
                1.0,
                type
            )
            list.add(item)
        }
        return list
    }


    /*May want to limit number of chars in each spinner item. Affects the size of the spinners*/
    private fun setupSpinners() {
        driveBlocksSpinnerList = populateList(5, DragDropBlock.e_type.DRIVE)
        spinnerDriveAdapter = ProgrammingSpinnerAdapter(driveBlocksSpinnerList, this)
        //spinnerDriveAdapter.setDropDownViewResource(R.layout.programming_spinner_driving_dropdown_layout)
        driveBlocksSpinnerList.add(
            0,   //Unused object. Shown only in title. Cannot be added to itemList
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_drive,
                "Driving",
                "Null",
                1.0,
                1.0,
                DragDropBlock.e_type.DRIVE
            )
        )
        programming_spinner_driving.adapter = spinnerDriveAdapter
        programming_spinner_driving.setSelection(0, false)


        modulesBlocksSpinnerList = populateList(5, DragDropBlock.e_type.MODULE)
        spinnerModulesAdapter = ProgrammingSpinnerAdapter(modulesBlocksSpinnerList, this)
        //spinnerModulesAdapter.setDropDownViewResource(R.layout.programming_spinner_modules_dropdown_layout)
        modulesBlocksSpinnerList.add(
            0,
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_modules,
                "Modules",
                "Null",
                1.0,
                1.0,
                DragDropBlock.e_type.MODULE
            )
        )
        programming_spinner_modules.adapter = spinnerModulesAdapter
        programming_spinner_modules.setSelection(0, false)

        //Ugly way to do it, should not initialize a new SaveCustomDragDropBlockManager, rather use the one in the main thread
        customBlocksSpinnerList =
            SaveCustomDragDropBlockManager(this).getArrayWithCustomDragDropBlocks()
        spinnerCustomAdapter = ProgrammingSpinnerAdapter(customBlocksSpinnerList, this)
        spinnerCustomAdapter.setDropDownViewResource(R.layout.programming_spinner_modules_dropdown_layout)
        customBlocksSpinnerList.add(
            0,
            DragDropBlock(
                R.drawable.ic_drag_dots,
                R.drawable.ic_custom,
                "Custom",
                "Null",
                1.0,
                1.0,
                DragDropBlock.e_type.CUSTOM
            )
        )
        programming_spinner_custom.adapter = spinnerCustomAdapter
        programming_spinner_custom.setSelection(0, false)

        programming_spinner_driving.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) { //We shouldn't add the title block
                    val block = (parent?.getItemAtPosition(position) as DragDropBlock).copy()
                    itemList.add(block)
                    adapter.notifyDataSetChanged()
                    programming_spinner_driving.setSelection(//Always make title block stay on top
                        0,
                        false
                    )
                }
            }
        }

        programming_spinner_modules.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val block = (parent?.getItemAtPosition(position) as DragDropBlock).copy()
                    itemList.add(block)
                    adapter.notifyDataSetChanged()
                    programming_spinner_modules.setSelection(0, false)
                }
            }
        }

        programming_spinner_custom.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val block = (parent?.getItemAtPosition(position) as DragDropBlock).copy()
                    itemList.add(block)
                    adapter.notifyDataSetChanged()
                    programming_spinner_custom.setSelection(0, false)
                }
            }
        }
    }

    /*For rearranging recyclerview*/
    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(
            ItemTouchHelper.DOWN
        ), ItemTouchHelper.RIGHT
    ) {
        override fun onMove(    //Handles reordering and movement in recyclerview
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            Collections.swap(itemList, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true
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
    override fun onDragDots(view: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(view) //onMove from ItemTouchHelper.simpleCallback will be accessed through startDrag
    }

    /*Used to mark item for deletion*/
    override fun onItemClick(position: Int, holder: View) {
        if (markForDeletion) {
            /*If item is already added to deleteList we want to deselect it*/
            if (deleteList.contains(itemList[position])) {
                holder.card_drag_drop.setCardBackgroundColor(Color.WHITE)
                holder.card_image_drag_dots.setImageResource(R.drawable.ic_drag_dots)
                deleteList.remove(itemList[position])
                if (deleteList.isEmpty()) {
                    markForDeletion = false
                }
            } else {
                holder.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AABBCC"))
                holder.card_image_drag_dots.setImageResource(R.drawable.ic_baseline_delete_24)
                deleteList[itemList[position]] = holder
            }
        }
    }

    override fun onParameterButtonClicked(position: Int) {
        changeItemParameterDialog(position)
    }

    /*LongClick activates selection for deletion*/
    override fun onLongClick(position: Int, view: View) {
        /*No need to activate if already activated*/
        if (!markForDeletion) {
            view.card_drag_drop.setCardBackgroundColor(Color.parseColor("#AABBCC"))
            view.card_image_drag_dots.setImageResource(R.drawable.ic_baseline_delete_24)
            deleteList[itemList[position]] =
                view   //Map (DragDropBlock at position) with accompanying recycler item view
            markForDeletion = true
        }
    }

    /*Shows an input dialog for changing an items parameter.*/
    private fun changeItemParameterDialog(position: Int) {
        val builder = AlertDialog.Builder(this).create()
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.input_dialog_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.input_dialog_text_in)

        editText.setText(itemList[position].parameter.toString())

        builder.setTitle("Change parameter")
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->
            updateItemValue(position, editText.text.toString().toDouble())
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

    private fun updateItemValue(position: Int, value: Double) {
        itemList[position].parameter = value
        itemList[position].displayParameter = value
        adapter.notifyDataSetChanged()
    }

    private suspend fun traverseList() {

        if (Utils.UtilsObject.isBluetoothConnectionThreadActive()) {
            val recycler = findViewById<RecyclerView>(R.id.programming_recycle_view)
            val tenthOfSecondInMS: Long = 100
            val secondInMS: Long = 1000



            itemList.forEachIndexed { index, item ->
                recycler.scrollToPosition(index) //Scrolls list so that current item is on screen

                var parameter: Int = item.displayParameter.toInt()

                while (item.displayParameter > 0) {
                    when (state) {  //State machine
                        RunState.RUNNING -> {
                            Utils.UtilsObject.bluetoothSendString("72$parameter", this.baseContext)
                            //Utils.UtilsObject.showUpdatedToast("72$parameter", this.baseContext)
                            delay(tenthOfSecondInMS) //Will finish current 'delayTimeMillis' period before pause
                            parameter--


                            /*Works with Locale but crashes without? Only for Andreas*/
                            item.displayParameter =
                                String.format(Locale.ENGLISH, "%.1f", item.displayParameter - 0.1)
                                    .toDouble()



                            adapter.notifyDataSetChanged()
                        }
                        RunState.PAUSE -> {
                            sem.acquire()
                        }
                        else -> {
                            state = RunState.IDLE
                        } //So we go to a known state if something would go wrong
                    }
                }
            }

            Utils.UtilsObject.showUpdatedToast("Project has run through completely!", this)
            delay(secondInMS * 3)

            resetListTraverse()
        }
    }

    /*Resetting list to its original state*/
    private fun resetListTraverse() {
        if (sem.availablePermits == 0) {
            sem.release()
        }
        state = RunState.IDLE
        for (i in itemList)
            i.displayParameter = i.parameter
        adapter.notifyDataSetChanged()
        programming_play_button.setImageResource(R.drawable.ic_play_arrow)
    }
}