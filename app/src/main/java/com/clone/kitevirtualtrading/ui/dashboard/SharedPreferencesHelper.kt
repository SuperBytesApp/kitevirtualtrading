package com.clone.kitevirtualtrading.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import com.clone.kitevirtualtrading.Item

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveItem(item: Item) {
        editor.putString("item_name", item.ltp)
        editor.apply()
    }

    fun getSavedItem(): String? {
        val itemId = sharedPreferences.getString("item_name", "")
        if (itemId != "") {
            val itemName = sharedPreferences.getString("item_name", "")
            return itemName
        }
        return null
    }

}
