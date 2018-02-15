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

internal class CompositeInvoiceApi(private val apis: List<InvoiceApi>) : InvoiceApi {

    override fun getDueInvoices(): List<Invoice> {
        return apis
                .flatMap { api -> api.getDueInvoices().map { invoiceWithProvider(api, it) } }
                .sortedByDescending { it.date }
    }

    override fun getPaidInvoices(limit: Int): List<Invoice> {
        return apis
                .flatMap { api -> api.getPaidInvoices(limit).map { invoiceWithProvider(api, it) } }
                .sortedByDescending { it.date }
    }

    override fun getProviderName() = "all"

    private fun invoiceWithProvider(api: InvoiceApi, invoice: Invoice): Invoice {
        return invoice.copy(id = api.getProviderName() + "::" + invoice.id)
    }

}