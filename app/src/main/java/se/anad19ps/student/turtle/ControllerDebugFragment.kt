package se.anad19ps.student.turtle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_controller_debug.view.*

class ControllerDebugFragment : Fragment() {

    companion object {
        var debugList = mutableListOf<String>()
        var recyclerViewDebugList : RecyclerView? = null
        var operationsDone = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_controller_debug, container, false)

        val btnClear = root.findViewById<Button>(R.id.fragment_controller_debug_button_clear_list)
        btnClear.setOnClickListener{
            clearDebugList()
        }

        recyclerViewDebugList = root.fragment_controller_debug_recycler_view
        recyclerViewDebugList!!.layoutManager = LinearLayoutManager(activity)
        recyclerViewDebugList!!.adapter = ControllerDebugRecyclerViewAdapter(debugList)

        return root
    }

    override fun onStart() {
        super.onStart()
        updateList()
    }

    fun addStringToDebugList(incomingString : String){
        if(recyclerViewDebugList != null){
            operationsDone+=1
            debugList.add("$operationsDone: $incomingString")
            updateList()
        }
    }

    private fun clearDebugList(){
        operationsDone = 0
        if(debugList.isNotEmpty())
            debugList.clear()
        updateList()
    }

    private fun updateList(){
        Handler(Looper.getMainLooper()).post {
            if(recyclerViewDebugList != null){
                recyclerViewDebugList!!.adapter!!.notifyDataSetChanged()
                recyclerViewDebugList!!.smoothScrollToPosition(debugList.size)
            }
        }
    }
}