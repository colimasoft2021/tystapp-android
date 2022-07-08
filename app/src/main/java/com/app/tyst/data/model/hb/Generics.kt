package com.dc.mvvmskeleton.data.model.hb

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.*

/**
 * Created by pankaj on 10/1/18.
 */

class Generics internal constructor(private var jsonElement: JsonElement) {

    val asObject: JsonObject?
        get() = if (jsonElement is JsonObject) {

            jsonElement.asJsonObject
        } else null

    fun <T> getAsList(clazz: Class<Array<T>>): List<T> {
        if (jsonElement is JsonArray) {
            val jsonToObject = Gson().fromJson(jsonElement, clazz)
            return listOf(*jsonToObject)
        } else if (jsonElement is JsonObject) {
            val jsonToObject = Gson().fromJson(jsonElement, clazz.componentType!!) as T
            return listOf(jsonToObject)
        }
        return ArrayList()
    }

    fun <T> getAsObject(clazz: Class<T>): T? {
        if (jsonElement is JsonArray) {
            return Gson().fromJson(jsonElement.asJsonArray.get(0), clazz)
        } else if (jsonElement is JsonObject) {
            return Gson().fromJson(jsonElement, clazz)
        }
        return null
    }

    companion object {
        fun with(jsonElement: JsonElement): Generics {
            return Generics(jsonElement)
        }
    }
}
