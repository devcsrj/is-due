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
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Locale
import java.util.function.Function

/**
 * Transforms responses from [MySkyInvoiceHttpApi.getPaymentHistory]
 */
internal class PaymentHistoryDocumentToInvoices : Function<Document, List<Invoice>> {

    override fun apply(doc: Document): List<Invoice> {
        val rows = doc.select("tbody > tr")
        val invoices = mutableListOf<Invoice>()

        val format = DecimalFormat("0,000.00")
        format.isParseBigDecimal = true
        rows.forEach {
            val dateStr = it.child(0).text()
            val amountStr = it.child(1).text().substringAfter("P ")
            val mode = it.child(2).text()
            val ref = it.child(3).text()

            val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            val nf = NumberFormat.getInstance(Locale.US)
            val amount = format.parse(amountStr) as BigDecimal

            invoices.add(Invoice(ref, amount, date))
        }

        return Collections.unmodifiableList(invoices)
    }

}