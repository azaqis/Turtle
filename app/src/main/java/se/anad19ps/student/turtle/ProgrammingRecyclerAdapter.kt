package se.anad19ps.student.turtle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/*Uses internal class ViewHolder*/
class ProgrammingRecyclerAdapter(
    private val itemList: List<DragDropBlock>,
    private val clickListener: ItemClickListener

) : RecyclerView.Adapter<ProgrammingRecyclerAdapter.InnerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProgrammingRecyclerAdapter.InnerViewHolder {
        var itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_drag_drop, parent, false)
        return InnerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgrammingRecyclerAdapter.InnerViewHolder, position: Int) {
        holder.directionImage.setImageResource(itemList[position].directionImage)    //Data stored in DragDropBlock
        holder.dragImage.setImageResource(itemList[position].dragImage)
        holder.text.text = itemList[position].text
    }

    override fun getItemCount() = itemList.count()

    inner class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var dragImage: ImageView = view.findViewById(R.id.card_image_drag_dots)
        var directionImage: ImageView = view.findViewById(R.id.card_image_direction)
        var text: TextView = view.findViewById(R.id.card_text)

        val firstButton = view.findViewById<Button>(R.id.programming_card_button)

        init {
            view.setOnClickListener(this)
            firstButton.setOnClickListener{ //This happens when each individual button is pressed
                firstButton.text = "BUTTON!"
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition

            if (position != RecyclerView.NO_POSITION) { //User might click empty space between items during animation
                if (v != null) {
                    clickListener.onItemClick(position, v)
                }
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, holder: View)
    }
}