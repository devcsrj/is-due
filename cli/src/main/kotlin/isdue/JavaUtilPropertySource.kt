/**
 * Is Due | CLI - Your one-stop app for managing Philippine service provider invoices
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
package isdue

import info.macias.kaconf.ConfiguratorException
import info.macias.kaconf.sources.AbstractPropertySource
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.util.Properties

class JavaUtilPropertySource(file: File) : AbstractPropertySource() {

    private lateinit var properties: Properties

    init {
        val props = Properties()
        try {
            FileInputStream(file).use { `is` ->
                props.load(`is`)
                this.properties = props
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Unable to load properties from file '$file'", e)
        }

    }

    override fun get(name: String): String? {
        return properties.getProperty(name)
    }

    override fun <T> get(name: String, type: Class<T>): T {
        try {
            return super.get(name, type)
        } catch (exception: ConfiguratorException) {
            if (type == URI::class.java)
                return URI.create(get(name)) as T
            throw exception
        }

    }

    override fun isAvailable() = true
}
