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

import org.springframework.shell.table.TableModel

/**
 * Base class for table models that contain a header, and is populated by a collection.
 *
 * @param <T> Specific model type
</T> */
internal abstract class BaseTableModel<in T>(private val columnNames: Array<String>,
                                             private val collection: Collection<T>) : TableModel() {

    override fun getRowCount(): Int {
        return collection.size + 1
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getValue(row: Int, column: Int): Any? {
        if (row <= 0)
            return columnNames[column]

        val t = collection.elementAtOrElse(row - 1, { null }) ?: return null
        return getColumnValue(t, column)
    }

    internal abstract fun getColumnValue(t: T, column: Int): Any
}
