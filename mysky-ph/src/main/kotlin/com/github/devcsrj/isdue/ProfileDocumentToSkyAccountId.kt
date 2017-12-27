/**
 * Is Due | MySky PH - Your one-stop app for managing online statement of accounts
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
package com.github.devcsrj.isdue

import org.jsoup.nodes.Document
import java.util.function.Function

internal class ProfileDocumentToSkyAccountId : Function<Document, String> {

    override fun apply(doc: Document): String {
        val accountElems = doc.select("#acc > div.static-block")
        val accounts = accountElems
                .map { Pair(it.child(0).text(), it.child(1).text()) }
                .map { Pair(it.first.substringBefore(":"), it.second) }
                .fold(HashMap<String, String>(), { a, i -> a.put(i.first, i.second); a })
        return accounts["SKY"]!!
    }

}