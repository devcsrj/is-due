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

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.function.Supplier

@Test
class MySkyInvoiceApiSpec {

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
                .setHeader("Set-Cookie", "s=e707fe2a-e161-4453-8b88-fdd6c838a722; path=/")
                .setHeader("Set-Cookie", "i=2d87ea3a-c20e-4b54-8182-a2d063ed0489; path=/")
                .setHeader("Set-Cookie", "u=firstname.lastname@maildrop.cc; path=/")
                .setHeader("Set-Cookie", "f=Firstname; path=/")
                .setBody(buffer))

        val skyUrl = skyServer.url("/metromanila/")
        val authUrl = authServer.url("/")

        val api = MySkyInvoiceApi(skyUrl, authUrl, "firstname.lastname@maildrop.cc", "password")
        val accountId = api.accountId.get()
        assertEquals("9075111372", accountId)
    }

    @Test
    fun testGetPaidDues() {
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
        api.accountId = Supplier { "9075111372" }

        val actual = api.getPaid()
        assertEquals(actual.size, 2)

    }

}