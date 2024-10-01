package com.upstox.marketdatafeeder.pref

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WatchlistSharedPreferencesManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("WatchlistPrefs", Context.MODE_PRIVATE)
    }

    fun saveWatchlist(watchlist: List<WachlistData>) {
        val jsonString = Gson().toJson(watchlist)
        sharedPreferences.edit().putString("watchlist", jsonString).apply()
    }

    fun getWatchlist(): List<WachlistData> {
        val jsonString = sharedPreferences.getString("watchlist", null)
        return if (jsonString != null) {
            Gson().fromJson(jsonString, object : TypeToken<List<WachlistData>>() {}.type)
        } else {
            emptyList()
        }
    }


    fun removeItemFromWatchlist(item: WachlistData) {
        val currentWatchlist = getWatchlist().toMutableList()
        currentWatchlist.remove(item)
        saveWatchlist(currentWatchlist)
    }
    // ... other methods
}
