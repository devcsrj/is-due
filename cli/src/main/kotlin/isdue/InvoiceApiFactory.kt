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

import info.macias.kaconf.ConfiguratorBuilder
import isdue.cli.MissingPropertyException
import isdue.mysky.MySkyConfig
import isdue.mysky.MySkyInvoiceApi
import okhttp3.HttpUrl
import java.io.File

class InvoiceApiFactory(private val apiMap: Map<String, Class<out Config>>) {

    /**
     * Attempts to construct [InvoiceApi]s for all registered invoice providers from the [file].
     *
     * @param file File to load credentials from
     */
    fun create(file: File): List<InvoiceApi> {
        if (!file.isFile)
            return emptyList()

        return apiMap.keys.map { create(it, file) }
    }

    /**
     * Attempts to construct [InvoiceApi] from the registered invoice provider matching [key] from the [file]
     *
     * @param key Invoice provider key
     * @param file File to load credentials from
     */
    fun create(key: String, file: File): InvoiceApi {
        if (!file.isFile)
            throw IllegalArgumentException("'$file' is not a valid file.")
        val clazz = apiMap[key]
                ?: throw IllegalArgumentException("Unregistered provider '$key'. Found '${apiMap.keys}'")

        val config = clazz.newInstance() as Config
        val configurator = ConfiguratorBuilder()
                .addSource(JavaUtilPropertySource(file))
                .build()
        configurator.configure(config)

        return create(config)
    }

    /**
     * Constructs an [InvoiceApi] from a [Config] object
     *
     * @param config Configuration details
     */
    fun create(config: Config): InvoiceApi {
        return when (config) {
            is MySkyConfig -> {
                checkProperty(config.username != null, "mysky.username")
                checkProperty(config.password != null, "mysky.password")
                checkProperty(HttpUrl.get(config.baseUrl) != null, "mysky.base-url")
                checkProperty(HttpUrl.get(config.authUrl) != null, "mysky.auth-url")

                MySkyInvoiceApi(
                        HttpUrl.get(config.baseUrl)!!,
                        HttpUrl.get(config.authUrl)!!,
                        config.username!!,
                        config.password!!)
            }
            else ->
                throw AssertionError("Unknown config class: $config")
        }
    }

    private fun checkProperty(condition: Boolean, key: String) {
        if (!condition)
            throw MissingPropertyException("Property '$key' is missing")
    }
}