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

import isdue.Invoice
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.function.Supplier
import kotlin.test.assertEquals

class MySkyInvoiceApiTest {

    @Test
    fun testLoginScrapesAccountId() {
        val skyServer = MockWebServer()
        val authServer = MockWebServer()
        val buffer = Buffer()
        javaClass.getResourceAsStream("/abs-cbn-auth-success.html").use {
            buffer.writeAll(Okio.source(it))
        }
        authServer.enqueue(MockResponse()
                .setResponseCode(200)
                .setBody(buffer))

        val skyUrl = skyServer.url("/metromanila/")
        val authUrl = authServer.url("/")

        val api = MySkyInvoiceApi(skyUrl, authUrl, "firstname.lastname@maildrop.cc", "password")
        val profile = api.profile.get()
        assertEquals("9075111372", profile.skyAccountId)
    }

    @Test
    fun testGetPaidInvoices() {
        val skyServer = MockWebServer()
        val buffer = Buffer()
        javaClass.getResourceAsStream("/mysky-payment-history.html").use {
            buffer.writeAll(Okio.source(it))
        }
        skyServer.enqueue(MockResponse()
                .setResponseCode(200)
                .setBody(buffer))
        val authServer = MockWebServer()

        val skyUrl = skyServer.url("/metromanila/")
        val authUrl = authServer.url("/")

        val api = MySkyInvoiceApi(skyUrl, authUrl, "firstname.lastname@maildrop.cc", "password")
        api.profile = Supplier {
            val profile = AbsCbnProfile()
            profile.skyAccountId = "9075111372"
            profile
        }

        val actual = api.getPaidInvoices()
        assertEquals(actual.size, 2)

    }

    @Test
    fun testGetDueInvoices() {
        val skyServer = MockWebServer()
        val buffer = Buffer()
        javaClass.getResourceAsStream("/mysky-account.json").use {
            buffer.writeAll(Okio.source(it))
        }
        skyServer.enqueue(MockResponse()
                .setResponseCode(200)
                .setBody(buffer))
        val authServer = MockWebServer()

        val skyUrl = skyServer.url("/metromanila/")
        val authUrl = authServer.url("/")

        val api = MySkyInvoiceApi(skyUrl, authUrl, "firstname.lastname@maildrop.cc", "password")
        api.profile = Supplier {
            val profile = AbsCbnProfile()
            profile.skyAccountId = "9075111372"
            profile
        }

        val actual = api.getDueInvoices()
        assertEquals(actual, listOf(
                Invoice("124121521", BigDecimal.ZERO, LocalDate.of(2018, 1, 5))
        ))
    }

}