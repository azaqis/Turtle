package se.anad19ps.student.turtle

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

import java.io.Serializable

@Parcelize
data class DragDropBlock(
    var dragImage: Int,
    val directionImage: Int,
    var text: String,
    val command: String,
    var parameter: Double,
    var displayParameter: Double,
    val type: e_type,
    val parameterEnabled : Boolean,
    var idNumber : Long
) : Parcelable {
    enum class e_type {
        DRIVE,
        MODULE,
        CUSTOM
    }
}