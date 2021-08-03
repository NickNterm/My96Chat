package com.iqsoft.my96chat.models

import android.os.Parcel
import android.os.Parcelable

data class Conversation(
    val assignedTo: ArrayList<String> = ArrayList(),
    var messages: ArrayList<Message> = ArrayList(),
    var id: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList()!!,
        parcel.createTypedArrayList(Message.CREATOR)!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(assignedTo)
        parcel.writeTypedList(messages)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }
    }
}