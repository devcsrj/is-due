package com.github.devcsrj.isdue

import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Locale
import java.util.function.Function

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

            val localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            val date = localDate.atStartOfDay(ZoneOffset.UTC)
            val nf = NumberFormat.getInstance(Locale.US)
            val amount = format.parse(amountStr) as BigDecimal

            invoices.add(Invoice(ref, amount, date))
        }

        return Collections.unmodifiableList(invoices)
    }

}