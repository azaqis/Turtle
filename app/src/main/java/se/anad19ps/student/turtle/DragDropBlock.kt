package se.anad19ps.student.turtle

import android.os.Parcel
import android.os.Parcelable

import java.io.Serializable

/*Holds data. Make sure to add data for parameter time and command*/
data class DragDropBlock(
    val dragImage: Int,
    val directionImage: Int,
    val text: String,
    val command: String,
    var parameter: Double,
    var displayParameter: Double,
    val type: e_type
) : Parcelable, Serializable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readSerializable() as e_type
    ) {
    }

    enum class e_type{
        DRIVE,
        MODULE,
        CUSTOM
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(dragImage)
        parcel.writeInt(directionImage)
        parcel.writeString(text)
        parcel.writeString(command)
        parcel.writeDouble(parameter)
        parcel.writeDouble(displayParameter)
        parcel.writeSerializable(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DragDropBlock> {
        override fun createFromParcel(parcel: Parcel): DragDropBlock {
            return DragDropBlock(parcel)
        }

        override fun newArray(size: Int): Array<DragDropBlock?> {
            return arrayOfNulls(size)
        }
    }
}
