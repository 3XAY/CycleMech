package com.xyzian.cyclemech

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PartsRepository (context: Context){
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("parts_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object{
        private const val PARTS_KEY = "bike_parts"
    }

    fun saveParts(parts: List<BikePart>){
        val json = gson.toJson(parts)
        with(sharedPreferences.edit()){
            putString(PARTS_KEY, json)
            apply()
        }
    }

    fun loadParts(): List<BikePart>{
        val json = sharedPreferences.getString(PARTS_KEY, null)
        return if (json != null){
            val type = object : TypeToken<List<BikePart>>() {}.type
            gson.fromJson(json, type)
        } else{
            listOf()
        }
    }
}