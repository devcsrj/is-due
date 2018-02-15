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

import isdue.CompositeInvoiceApi
import isdue.InvoiceApiFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.io.File
import java.nio.file.Paths

@ShellComponent
internal class InvoiceApiCommands(private val apiFactory: InvoiceApiFactory) {

    private var api: CompositeInvoiceApi = CompositeInvoiceApi(emptyList())

    @ShellMethod("Loads credentials for providers from a file.")
    fun load(@ShellOption(defaultValue = ShellOption.NULL,
            help = "The file to load credentials from. If not provided, the cli will look" +
                    " for '.my-dues' from this application's load directory.") source: File?) {

        var sourceToUse = source
        if (sourceToUse == null) {
            val userDir = Paths.get(System.getProperty("user.dir"))
            sourceToUse = userDir.resolve(".my-dues").toFile()
        }

        api = CompositeInvoiceApi(apiFactory.create(sourceToUse!!))
    }

    @ShellMethod("Prints due invoices.")
    fun dues(): InvoicesTable {
        val invoices = api.getDueInvoices()
        return InvoicesTable(invoices)
    }

    @ShellMethod("Prints paid invoices.")
    fun paid(@ShellOption(defaultValue = "5") limit: Int): InvoicesTable {
        val invoices = api.getPaidInvoices(limit)
        return InvoicesTable(invoices)
    }

}
