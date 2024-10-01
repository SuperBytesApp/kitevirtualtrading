package com.clone.upstoxclone;

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
        val ltpp: String
) {
    // Secondary constructor
    constructor() : this("", "", "", "", "", "","","","","","")

    companion object {
        // Factory method to create UserData instances
        fun create(qty: String, ltp: String, name: String, type: String, avg: String, profit: String,bal: String,last: String,ltpmin: String,ltpmax: String,ltpp: String): UserData {
            return UserData(qty, ltp, name, type, avg, profit,bal,last,ltpmin,ltpmax,ltpp)
        }
    }
}
