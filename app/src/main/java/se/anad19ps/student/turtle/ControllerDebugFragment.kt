package se.anad19ps.student.turtle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_controller_debug.*
import kotlinx.android.synthetic.main.fragment_controller_debug.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ControllerDebugFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ControllerDebugFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    companion object {
        var debugList = mutableListOf<String>()
        var recyclerViewDebugList : RecyclerView? = null
        var operationsDone = 0

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ControllerTopFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ControllerDebugFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_controller_debug, container, false)

        Handler(Looper.getMainLooper()).post {
            buttonClearList.setBackgroundColor(
                ContextCompat.getColor(
                    root.context,
                    R.color.PrimaryComplement
                )
            )
        }

        var btnClear = root.findViewById<Button>(R.id.buttonClearList)
        btnClear.setOnClickListener{
            clearDebugList()
        }

        /*buttonClearList.setOnClickListener{
            clearDebugList()
        }*/

        recyclerViewDebugList = root.recyclerViewDebug
        recyclerViewDebugList!!.layoutManager = LinearLayoutManager(activity)
        recyclerViewDebugList!!.adapter = ControllerDebugRecyclerViewAdapter(debugList)

        /*var nonsenseString = "blablabla"
        var i = 0

        while(i<100){
            debugList.add(nonsenseString)
            i += 1
        }*/

        return root
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
        debugList.clear()
        updateList()
    }

    private fun updateList(){
        Handler(Looper.getMainLooper()).post {
            if(recyclerViewDebugList != null){
                recyclerViewDebugList!!.adapter!!.notifyDataSetChanged()
                recyclerViewDebugList!!.smoothScrollToPosition(debugList.size);
            }
        }
    }
}