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

import isdue.Invoice
import org.springframework.shell.TerminalSizeAware
import org.springframework.shell.table.BorderStyle
import org.springframework.shell.table.CellMatchers
import org.springframework.shell.table.SizeConstraints
import org.springframework.shell.table.Table
import org.springframework.shell.table.TableBuilder
import java.math.BigDecimal

internal class InvoicesTable(private val invoices: List<Invoice>) : TerminalSizeAware {
    private val table: Table

    init {

        val model = InvoicesTableModel(invoices)
        val tableBuilder = TableBuilder(model)
        tableBuilder
                .on(CellMatchers.column(0)) // ID
                .addSizer { _, _, _ -> SizeConstraints.Extent(10, 20) }
                .on(CellMatchers.column(1)) // Amount
                .addSizer { _, _, _ -> SizeConstraints.Extent(10, 20) }
                .on(CellMatchers.column(2)) // Date
                .addSizer { _, _, _ -> SizeConstraints.Extent(10, 20) }
        this.table = tableBuilder
                .addHeaderAndVerticalsBorders(BorderStyle.fancy_heavy)
                .build()
    }

    override fun render(terminalWidth: Int): CharSequence {
        val outstanding = invoices
                .map { it.amount }
                .fold(BigDecimal.ZERO, { acc, v -> acc.add(v) })
        var out = table.render(terminalWidth)
        out += "\n"
        out += "Total dues: $outstanding"
        return out
    }
}
