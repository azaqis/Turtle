package se.anad19ps.student.turtle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class DriveSpinnerAdapter(
    private val contentList: List<ProgrammingItemDataBlock>,
    context: Context
) : ArrayAdapter<ProgrammingItemDataBlock>(context, 0, contentList) {

   /* override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return init(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return init(position, convertView, parent)
    }

    private fun init(position : Int, v : View?, parent : ViewGroup) : View{
        val item = getItem(position)

        /*Converts layout to view object*/
        val view = LayoutInflater.from(context).inflate(R.layout.card_spinner_block, parent, false)    //Make view from layout

        if (item != null) {
            view.card_image_direction_move.setImageResource(item.directionImage)
            view.card_text_move.text = item.text
        }
        return view
    }*/
}