package se.anad19ps.student.turtle

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ManageCustomDragDropBlocksRecyclerAdapter (private var titles: List<String>, private var details: List<String>, private var images:List<Int>) :
RecyclerView.Adapter<ManageCustomDragDropBlocksRecyclerAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemTitle: TextView = itemView.findViewById(R.id.text_view_title)
        val itemDescription: TextView = itemView.findViewById(R.id.text_view_description)
        val itemPicture: ImageView = itemView.findViewById(R.id.image_view)

        init {
            itemView.setOnClickListener { v : View ->
                val position: Int = adapterPosition
                val customDragDropBlockName = v.findViewById<View>(R.id.text_view_title).toString()
                val intent = Intent(itemView.context, EditCustomDragDropBlocksActivity::class.java)
                intent.putExtra("NAME_DRAGDDROPBLOCK", customDragDropBlockName)
                itemView.context.startActivity(intent)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.manage_custom_commands_recyclerview_row_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTitle.text = titles[position]
        holder.itemDescription.text = details[position]
        holder.itemPicture.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}