package se.anad19ps.student.turtle

import android.annotation.SuppressLint
import android.graphics.Color
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
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProgrammingRecyclerAdapter.InnerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_drag_drop, parent, false)

        val holder = InnerViewHolder(itemView)

        /*Adding drag and drop when touching the 'drag dots' area*/
        holder.itemView.card_image_drag_dots.setOnTouchListener { view, event ->
            if(event.actionMasked == MotionEvent.ACTION_DOWN){  //If touching
                clickListener.onDragDots(holder)    //Call interface function
            }
            return@setOnTouchListener true
        }
        return holder
    }

    /*Fill view holder with data. Remember that recyclerview contains re-usable holders that needs to be loaded when enetring screen.
    * This function is called whenever a new element is loaded*/
    override fun onBindViewHolder(
        holder: ProgrammingRecyclerAdapter.InnerViewHolder,
        position: Int
    ) {
        /*Set graphics for element to match whatever data is stored in dragDropBlock*/
        holder.directionImage.setImageResource(itemList[position].directionImage)
        holder.dragImage.setImageResource(itemList[position].dragImage)
        holder.text.text = itemList[position].text
        holder.firstButton.text = itemList[position].displayParameter.toString()

        /*Choose appropriate graphics depending on type of block*/
        when(itemList[position].type){
            DragDropBlock.e_type.DRIVE ->
                holder.firstButton.setBackgroundResource(R.drawable.programming_card_parameter_button_drive)
            DragDropBlock.e_type.MODULE ->
                holder.firstButton.setBackgroundResource(R.drawable.programming_card_parameter_button_module)
            DragDropBlock.e_type.CUSTOM ->
                holder.firstButton.setBackgroundResource(R.drawable.programming_card_parameter_button_custom)
        }
    }

    override fun getItemCount() = itemList.count()

    /*view is the itemView that we passed in onCreateViewHolder*/
    inner class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var dragImage: ImageView = view.findViewById(R.id.card_image_drag_dots)
        var directionImage: ImageView = view.findViewById(R.id.card_image_direction)
        var text: TextView = view.findViewById(R.id.card_text)
        var firstButton: Button = view.findViewById(R.id.programming_card_button)

        init {
            view.setOnClickListener(this)
            firstButton.setOnClickListener { //This happens when each individual button is pressed
                onClick(firstButton)
            }
            view.setOnLongClickListener{
                clickListener.onLongClick(adapterPosition, view)
                true
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            /*itemView is the view that belongs to this specific instance of InnerViewHolder class. Did not work by just writing 'v == view'
            * Unclear why*/
            if (position != RecyclerView.NO_POSITION && v == itemView) {
                clickListener.onItemClick(position, v)
            }
            else if(position != RecyclerView.NO_POSITION && v == firstButton){
                clickListener.onParameterButtonClicked(position)
            }
        }
    }

    /*These have to be implemented by calling class*/
    interface ItemClickListener {
        fun onItemClick(position: Int, holder: View)
        fun onParameterButtonClicked(position : Int)
        fun onLongClick(position : Int, view : View)
        fun onDragDots(view: RecyclerView.ViewHolder)
    }
}