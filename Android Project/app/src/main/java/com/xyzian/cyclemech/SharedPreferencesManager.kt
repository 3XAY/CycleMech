package com.xyzian.cyclemech

import android.content.Context
import android.content.SharedPreferences

//This class is used to manage the mileage data and saves it in storage, functionally the same as a dictionary (key-value store)
class SharedPreferencesManager(context: Context){
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cyclemech_prefs", Context.MODE_PRIVATE) //Creates a file called "cyclemech_prefs" in storage to store the mile data

    companion object{ //Companion objects enable you to define constants within the class
        private const val MILES_KEY = "total_miles" //This is a constant that is also a key which is used to access the total mile data
    }

    fun setMiles(miles: Float) {
        with(sharedPreferences.edit()) { //Opens an editor for SharedPreferences
            putFloat(MILES_KEY, miles) //Saves the miles variable with the key "total_miles" as a float
            apply() //Saves the changes in the background
        }
    }

    fun getMiles(): Float{
        return sharedPreferences.getFloat(MILES_KEY, 0.0F) //Gets the total miles from storage and returns it as a float
    }
}