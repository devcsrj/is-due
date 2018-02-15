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
package isdue.cli

import isdue.InvoiceApiFactory
import isdue.mysky.MySkyConfig
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean

@SpringBootApplication
internal open class Cli {

    @Bean
    open fun invoiceApiFactory(): InvoiceApiFactory {
        return InvoiceApiFactory(mapOf(
                Pair("mysky", MySkyConfig::class.java)
        ))
    }
}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .bannerMode(Banner.Mode.CONSOLE)
            .sources(Cli::class.java)
            .run(*args)
}