package com.nytimes.android.external.store3.middleware

import com.google.gson.Gson
import com.nytimes.android.external.store3.base.Parser
import com.nytimes.android.external.store3.util.ParserException

import java.lang.reflect.Type

import javax.inject.Inject

class GsonStringParser<Parsed> @Inject
constructor(private val gson: Gson, private val type: Type) : Parser<String, Parsed> {

    @Throws(ParserException::class)
    override suspend fun apply(raw: String): Parsed {
        return gson.fromJson<Parsed>(raw, type)
    }
}
