package com.upstox.marketdatafeeder.pref

import android.content.Context
import android.content.SharedPreferences

class AccessTokenPref(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "MyPrefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
    }

    var accessToken: String?
        get() = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
        set(value) {
            sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, value).apply()
        }
}