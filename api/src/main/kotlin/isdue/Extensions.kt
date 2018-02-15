/**
 * Is Due | API - Your one-stop app for managing Philippine service provider invoices
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

import java.io.Serializable
import java.util.function.Supplier

fun <T> (Supplier<T>).memoize(): Supplier<T> = MemoizingSupplier(this)

internal class MemoizingSupplier<T>(private val delegate: Supplier<T>) : Supplier<T>, Serializable {

    @Volatile
    @Transient
    internal var initialized: Boolean = false
    // "value" does not need to be volatile; visibility piggy-backs
    // on volatile read of "initialized".
    @Transient
    internal var value: T? = null

    override fun get(): T {
        // A 2-field variant of Double Checked Locking.
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    val t = delegate.get()
                    value = t
                    initialized = true
                    return t
                }
            }
        }
        return value!!
    }

    override fun toString(): String {
        return "Suppliers.memoize($delegate)"
    }

    companion object {

        private const val serialVersionUID: Long = 0
    }
}