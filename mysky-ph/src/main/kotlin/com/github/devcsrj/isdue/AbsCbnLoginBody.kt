/**
 * Is Due | MySky PH - Your one-stop app for managing Philippine service provider invoices
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

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.nio.charset.StandardCharsets

internal class AbsCbnLoginBody(private val username: String,
                               private val password: String) : RequestBody() {

    override fun writeTo(sink: BufferedSink?) {
        sink!!.apply {
            writeString("""
                {"un":"$username","ps":"$password","dp":false,"ssoappid":""}
                """.trimIndent(), StandardCharsets.UTF_8)
        }
    }

    override fun contentType(): MediaType? {
        return MediaType.parse("application/json")
    }

}