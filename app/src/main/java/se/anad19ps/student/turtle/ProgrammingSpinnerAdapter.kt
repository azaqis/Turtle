package se.anad19ps.student.turtle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.card_spinner_block.view.*

class ProgrammingSpinnerAdapter(
    private val contentList: List<DragDropBlock>,
    context: Context
) : ArrayAdapter<DragDropBlock>(context, 0, contentList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return init(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return init(position, convertView, parent)
    }

    private fun init(position: Int, v: View?, parent: ViewGroup): View {
        val item = getItem(position)

        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.card_spinner_block, parent, false)    //Make view from layout

        /*Different colors for different spinners*/
        when (item?.type) {
            DragDropBlock.e_type.DRIVE ->
                view.card_spinner_constraint.setBackgroundColor(ContextCompat.getColor(context, R.color.SpinnerBackgroundTurquoise))
            DragDropBlock.e_type.MODULE ->
                view.card_spinner_constraint.setBackgroundColor(ContextCompat.getColor(context, R.color.SpinnerBackgroundPink))
            DragDropBlock.e_type.CUSTOM ->
                view.card_spinner_constraint.setBackgroundColor(ContextCompat.getColor(context, R.color.SpinnerBackgroundOrange))
        }
        if (item != null) {
            view.card_spinner_image.setImageResource(item.directionImage)
        }
        view.card_spinner_text.text = item?.text
        return view
    }
}