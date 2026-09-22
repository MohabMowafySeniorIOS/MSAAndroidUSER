package com.msa.android.di

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

/** API القديم يرجّع social كـ [] أحياناً وكـ object أحياناً. */
class JsonAdapters {
    @FromJson
    fun readSocial(reader: JsonReader): Map<String, String>? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.skipValue()
                emptyMap()
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val values = linkedMapOf<String, String>()
                reader.beginObject()
                while (reader.hasNext()) {
                    val key = reader.nextName()
                    if (reader.peek() == JsonReader.Token.STRING) values[key] = reader.nextString()
                    else reader.skipValue()
                }
                reader.endObject()
                values
            }
            else -> {
                reader.skipValue()
                emptyMap()
            }
        }
    }

    @ToJson
    fun writeSocial(value: Map<String, String>?): Map<String, String>? = value
}
