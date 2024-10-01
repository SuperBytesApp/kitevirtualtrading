package com.upstox.marketdatafeeder

import android.os.Parcel
import android.os.Parcelable

data class UserData(
    val qty: String,
    val ltp: String,
    val name: String,
    val type: String,
    val avg: String,
    val profit: String,
    val bal: String,
    val last: String,
    val ltpmin: String,
    val ltpmax: String,
    val lot: String,
    val marketid: String,
    val radio: String,
    var marketid2: String,
    var id : String,
    var nse : String
) : Parcelable {

    // Secondary constructor
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", "","","","")

    // Parcelable constructor
    private constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: ""
    )

    // Implementation of Parcelable interface
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(qty)
        parcel.writeString(ltp)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(avg)
        parcel.writeString(profit)
        parcel.writeString(bal)
        parcel.writeString(last)
        parcel.writeString(ltpmin)
        parcel.writeString(ltpmax)
        parcel.writeString(lot)
        parcel.writeString(marketid)
        parcel.writeString(radio)
        parcel.writeString(marketid2)
        parcel.writeString(id)
        parcel.writeString(nse)
    }

    override fun describeContents(): Int {
        return 0
    }

    // Companion object with CREATOR instance
    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }
}
