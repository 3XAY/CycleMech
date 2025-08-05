package com.xyzian.cyclemech

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context){
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cyclemech_prefs", Context.MODE_PRIVATE)

    companion object{
        private const val MILES_KEY = "total_miles"
    }

    fun setMiles(miles: Int) {
        with(sharedPreferences.edit()) {
            putInt(MILES_KEY, miles)
            apply()
        }
    }

    fun getMiles(): Int{
        return sharedPreferences.getInt(MILES_KEY, 0)
    }
}