package se.anad19ps.student.turtle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ControllerDebugRecyclerViewAdapter(private var strings: List<String>) :
    RecyclerView.Adapter<ControllerDebugRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemString: TextView = itemView.findViewById(R.id.text_view_Debug)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_controller_debug_row_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemString.text = strings[position]
    }

    override fun getItemCount(): Int {
        return strings.size
    }

}