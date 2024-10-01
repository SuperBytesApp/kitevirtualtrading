package com.clone.kitevirtualtrading

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    // Function to save data in SharedPreferences
    fun saveData(userData: UserData) {
        val json = gson.toJson(userData)
        editor.putString("userData", json)
        editor.apply()
    }


    fun saveData2(userData: UserData) {
        val json = gson.toJson(userData)
        editor.putString("userData2", json)
        editor.apply()
    }

    fun saveData3(userData: UserData) {
        val json = gson.toJson(userData)
        editor.putString("userData3", json)
        editor.apply()
    }

    // Function to retrieve data from SharedPreferences
    fun getData(): UserData? {
        val json = sharedPreferences.getString("userData", null)
        return gson.fromJson(json, UserData::class.java)
    }

    fun getData2(): UserData? {
        val json = sharedPreferences.getString("userData2", null)
        return gson.fromJson(json, UserData::class.java)
    }


    fun getData3(): UserData? {
        val json = sharedPreferences.getString("userData3", null)
        return gson.fromJson(json, UserData::class.java)
    }


    fun clearUserData() {
        editor.remove("userData")
        editor.apply()
    }
    fun clearUserData2() {
        editor.remove("userData2")
        editor.apply()
    }
    fun clearUserData3() {
        editor.remove("userData3")
        editor.apply()
    }

}
