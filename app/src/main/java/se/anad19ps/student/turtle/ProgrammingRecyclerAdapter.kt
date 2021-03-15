package se.anad19ps.student.turtle

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_drag_drop.view.*

/*clickListener is linked to whatever ItemClickListener we pass*/
class ProgrammingRecyclerAdapter(
    private val itemList: List<DragDropBlock>,
    private val clickListener: ItemClickListener

) : RecyclerView.Adapter<ProgrammingRecyclerAdapter.InnerViewHolder>() {

    /*Creates views for each item. Each InnerViewHolder is created with its own copy of card_drag_drop */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProgrammingRecyclerAdapter.InnerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_drag_drop, parent, false)
        val holder = InnerViewHolder(itemView)

        /*Adding drag and drop when touching the 'drag dots' area*/
        holder.itemView.card_drag_drop_image_drag_dots.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN)
                clickListener.onDragDots(holder)
            else if (event.actionMasked == MotionEvent.ACTION_UP)
                view.performClick()
            /*Function returns inside function. Demands @ to know what function is returning*/
            return@setOnTouchListener true
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: ProgrammingRecyclerAdapter.InnerViewHolder,
        position: Int
    ) {
        /*Was not able to solve with wrong position parameter.This solves the problem but does not allow recycling*/
        holder.setIsRecyclable(false)
        val parameterEnabled = itemList[position].parameterEnabled

        /*Set data in the recycler item to match that from the DragDropBlock*/
        holder.directionImage.setImageResource(itemList[position].directionImage)
        holder.dragImage.setImageResource(itemList[position].dragImage)
        holder.text.text = itemList[position].text
        if (parameterEnabled)
            holder.parameterButton.text = itemList[position].displayParameter.toString()


        /*Choose appropriate graphics depending on type of block and if parameter is enabled*/
        when (itemList[position].type) {
            DragDropBlock.BlockType.DRIVE ->
                if (parameterEnabled)
                    holder.parameterButton.setBackgroundResource(R.drawable.programming_card_parameter_button_drive)
                else
                    holder.parameterButton.setBackgroundResource(R.drawable.programming_card_parameter_button_drive_filled)
            DragDropBlock.BlockType.MODULE ->
                if (parameterEnabled)
                    holder.parameterButton.setBackgroundResource(R.drawable.programming_card_parameter_button_module)
                else
                    holder.parameterButton.setBackgroundResource(R.drawable.programming_card_parameter_button_module_filled)
            DragDropBlock.BlockType.CUSTOM ->
                if (parameterEnabled)
                    holder.parameterButton.setBackgroundResource(R.drawable.programming_card_parameter_button_custom)
                else
                    holder.parameterButton.setBackgroundResource(R.drawable.programming_card_parameter_button_custom_filled)
        }
    }

    override fun getItemCount() = itemList.count()

    /*Out class for items in the recycler. 'view' is the itemView that we passed in onCreateViewHolder*/
    inner class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var dragImage: ImageView = view.findViewById(R.id.card_drag_drop_image_drag_dots)
        var directionImage: ImageView = view.findViewById(R.id.card_drag_drop_image_direction)
        var text: TextView = view.findViewById(R.id.card_drag_drop_text)
        var parameterButton: Button = view.findViewById(R.id.card_drag_drop_button)

        init {
            view.setOnClickListener(this)
            parameterButton.setOnClickListener { //This happens when each individual button is pressed
                onClick(parameterButton)
            }
            view.setOnLongClickListener {
                clickListener.onLongClick(adapterPosition)
                true
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            /*When an item is clicked*/
            if (position != RecyclerView.NO_POSITION && v == itemView) {
                clickListener.onItemClick(position)
            }
            /*If we pressed a button and parameter is enabled*/
            else if (position != RecyclerView.NO_POSITION && v == parameterButton && itemList[adapterPosition].parameterEnabled) {
                clickListener.onParameterButtonClicked(position)
            }
        }
    }

    /*These have to be implemented by calling class*/
    interface ItemClickListener {
        fun onItemClick(position: Int)
        fun onParameterButtonClicked(position: Int)
        fun onLongClick(position: Int)
        fun onDragDots(view: RecyclerView.ViewHolder)
    }
}