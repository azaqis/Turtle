package se.anad19ps.student.turtle

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgrammingSavedState(
    val itemList: ArrayList<DragDropBlock>,
    val deleteList: ArrayList<DragDropBlock>,
    val itemIdCounter: Long
) : Parcelable