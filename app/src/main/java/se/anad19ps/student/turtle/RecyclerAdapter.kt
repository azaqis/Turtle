package se.anad19ps.student.turtle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.setOverScrollMode
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.*

/*Uses internal class ViewHolder*/
class RecyclerAdapter(
    private val itemList: List<DragDropBlock>,
    private val clickListener: ItemClickListener

) : RecyclerView.Adapter<RecyclerAdapter.InnerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerAdapter.InnerViewHolder {
        var itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_drag_drop, parent, false)
        return InnerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.InnerViewHolder, position: Int) {
        holder.directionImage.setImageResource(itemList[position].directionImage)    //Data stored in DragDropBlock
        holder.dragImage.setImageResource(itemList[position].dragImage)
        holder.text.text = itemList[position].text
    }

    override fun getItemCount() = itemList.count()

    inner class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var dragImage: ImageView = view.findViewById(R.id.card_image_drag_dots)
        var directionImage: ImageView = view.findViewById(R.id.card_image_direction)
        var text: TextView = view.findViewById(R.id.card_text)

        init {
            view.setOnClickListener(this)
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