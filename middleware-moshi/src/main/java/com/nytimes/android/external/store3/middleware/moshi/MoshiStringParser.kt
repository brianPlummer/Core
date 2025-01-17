package com.nytimes.android.external.store3.middleware.moshi

import com.nytimes.android.external.store3.base.Parser
import com.nytimes.android.external.store3.util.ParserException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject

class MoshiStringParser<Parsed> @Inject
constructor(moshi: Moshi, type: Type) : Parser<String, Parsed> {

    private val jsonAdapter: JsonAdapter<Parsed> = moshi.adapter(type)

    @Throws(ParserException::class)
    override suspend fun apply(s: String): Parsed {
        try {
            return jsonAdapter.fromJson(s)!!
        } catch (e: IOException) {
            throw ParserException(e?.message ?: "", e)
        }
    }
}
