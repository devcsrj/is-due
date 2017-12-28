/**
 * Is Due | API Tool - Retrofit2 HTML Converter - Your one-stop app for managing online statement of accounts
 * Copyright © 2017 Reijhanniel Jearl Campos (devcsrj@apache.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.devcsrj.retrofit2


import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A [Converter.Factory] that transforms [okhttp3.ResponseBody] to [Document].
 */
class HtmlConverterFactory : Converter.Factory() {

    companion object {

        @JvmStatic
        fun create(): HtmlConverterFactory {
            return HtmlConverterFactory()
        }
    }

    private val parser: Parser = Parser.htmlParser()

    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, Document>? {

        if (type != Document::class.java)
            return null

        return Converter {
            it.byteStream().use {
                Jsoup.parse(it, null, retrofit!!.baseUrl().toString(), parser)
            }
        }
    }


}
