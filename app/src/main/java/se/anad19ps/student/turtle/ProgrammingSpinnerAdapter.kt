package se.anad19ps.student.turtle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.card_spinner_block.view.*

class ProgrammingSpinnerAdapter(
    itemList: List<DragDropBlock>,
    context: Context
) : ArrayAdapter<DragDropBlock>(context, 0, itemList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return init(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return init(position, parent)
    }

    private fun init(position: Int, parent: ViewGroup): View {
        val item = getItem(position)

        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.card_spinner_block, parent, false)

        /*Set color on item depending on type*/
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