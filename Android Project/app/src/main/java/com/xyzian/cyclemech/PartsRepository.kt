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
            listOf(
                BikePart(1, "Chain", "Shimano", "CN-HG54", 15F, 0F, 100F,"", 25.00, "10 Speed, Amazon"),
                BikePart(2, "Rear Derailleur", "Microshift", "Advent X", 15F, 0F, 100F,"",  63.00, "With clutch, 365 Cycles"),
                BikePart(3, "Chainring", "Deckas", "N/A", 15F, 0F, 100F,  "", 10.00, "38T 104BCD, Amazon"),
                BikePart(4, "Cassette", "Microshift", "Advent X E-Series", 15F, 0F, 100F, "", 47.00, "Amazon"),
                BikePart(5, "Tubes", "Goodyear", "N/A", 15F, 0F, 100F,  "", 12.00, "Walmart"),
                BikePart(6, "Tires", "Continental", "Cross King", 15F, 0F, 100F,  "", 63.00, "26x2.2, Al's (swapped in bike shop)"),
                BikePart(7, "Brake pads", "CNC", "N/A", 15F, 0F, 100F,  "", 5.00, "Part of a whole set ($27), Amazon"),
                BikePart(8, "Shifter", "Shimano", "SL-M6000", 15F, 0F, 100F,  "", 28.00, "Amazon")
            )
        }
    }
}