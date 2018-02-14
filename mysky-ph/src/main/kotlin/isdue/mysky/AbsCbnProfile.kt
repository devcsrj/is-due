/**
 * Is Due | MySky PH - Your one-stop app for managing Philippine service provider invoices
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
package isdue.mysky

import pl.droidsonroids.jspoon.annotation.Selector
import java.util.Date

internal class AbsCbnProfile {

    @Selector("#name-static > label.value:nth-child(1)")
    lateinit var firstName: String

    @Selector("#name-static > label.value:nth-child(2)")
    lateinit var lastName: String

    @Selector("#birthday-static > label.value", format = "MMM dd, yyyy")
    lateinit var birthDate: Date

    @Selector("#hdSkyAccount", attr = "value")
    lateinit var skyAccountId: String


}