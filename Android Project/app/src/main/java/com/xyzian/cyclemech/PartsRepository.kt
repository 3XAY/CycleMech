package com.xyzian.cyclemech

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Handles the storage of the parts list as a JSON
class PartsRepository (context: Context){
    //Creates a file called "parts_prefs" in storage to store the parts list
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("parts_prefs", Context.MODE_PRIVATE)

    private val gson = Gson() //Library to convert Kotlin objects to JSON so it can be easily stored

    companion object{
        private const val PARTS_KEY = "bike_parts"
    }

    fun saveParts(parts: List<BikePart>){
        val json = gson.toJson(parts) //Converts bike list to JSON to be stored
        with(sharedPreferences.edit()){ //Opens an editor for SharedPreferences and saves the JSON just like the miles data
            putString(PARTS_KEY, json)
            apply()
        }
    }

    fun loadParts(): List<BikePart>{
        val json = sharedPreferences.getString(PARTS_KEY, null) //Gets the JSON from storage and saves it as a val
        return if (json != null){ //If the data exists, convert it to a list of objects
            val type = object : TypeToken<List<BikePart>>() {}.type
            gson.fromJson(json, type)
        } else{
            //Return an empty list in the event that the data doesn't exist
            listOf()
        }
    }
}