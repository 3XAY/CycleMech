package com.xyzian.cyclemech

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context){
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cyclemech_prefs", Context.MODE_PRIVATE)

    companion object{
        private const val MILES_KEY = "total_miles"
    }

    fun setMiles(miles: Float) {
        with(sharedPreferences.edit()) {
            putFloat(MILES_KEY, miles)
            apply()
        }
    }

    fun getMiles(): Float{
        return sharedPreferences.getFloat(MILES_KEY, 0.0F)
    }
}