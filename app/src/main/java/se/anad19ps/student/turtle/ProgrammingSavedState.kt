package se.anad19ps.student.turtle

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgrammingSavedState(
    val itemList: ArrayList<DragDropBlock>,
    val selectedList: ArrayList<DragDropBlock>,
    val itemIdCounter: Long,
    val positionAlertDialog: Int
) : Parcelable