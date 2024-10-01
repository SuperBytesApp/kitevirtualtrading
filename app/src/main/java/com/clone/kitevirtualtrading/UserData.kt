package com.clone.kitevirtualtrading;

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
        val nrml : String,
        val monthly : String
) {
    // Secondary constructor
    constructor() : this("", "", "", "", "", "","","","","","","")

    companion object {
        // Factory method to create UserData instances
        fun create(qty: String, ltp: String, name: String, type: String, avg: String, profit: String,bal: String,last: String,ltpmin: String,ltpmax: String,nrml: String,monthly: String): UserData {
            return UserData(qty, ltp, name, type, avg, profit,bal,last,ltpmin,ltpmax,nrml,monthly)
        }
    }
}
