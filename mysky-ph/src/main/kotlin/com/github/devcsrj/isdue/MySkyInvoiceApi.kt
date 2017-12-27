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

import com.github.devcsrj.retrofit2.HtmlConverterFactory
import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Suppliers
import devcsrj.okhttp3.logging.HttpLoggingInterceptor
import okhttp3.HttpUrl
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.net.CookieManager
import java.time.Year
import java.time.ZoneOffset
import java.util.function.Supplier

class MySkyInvoiceApi : InvoiceApi {

    private val httpClient: OkHttpClient
    private val invoiceApi: MySkyInvoiceHttpApi
    private val username: String
    private val password: String

    @VisibleForTesting
    internal var accountId: Supplier<String>

    constructor(url: HttpUrl, authUrl: HttpUrl, username: String, password: String) {
        val cookieManager = CookieManager()
        this.httpClient = OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(cookieManager))
                .addInterceptor(HttpLoggingInterceptor())
                .build()

        this.accountId = Suppliers.memoize(Supplier {
            val api = Retrofit.Builder()
                    .baseUrl(authUrl)
                    .addConverterFactory(HtmlConverterFactory.create())
                    .client(httpClient)
                    .validateEagerly(true)
                    .build()
                    .create(AbsCbnSsoHttpApi::class.java)

            val call = api.login(AbsCbnSsoHttpApi.LoginBody(username, password))
            val response = call.execute()
            ProfileDocumentToSkyAccountId().apply(response.body()!!)
        }::get)

        this.username = username
        this.password = password

        this.invoiceApi = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(HtmlConverterFactory.create())
                .client(httpClient)
                .validateEagerly(true)
                .build()
                .create(MySkyInvoiceHttpApi::class.java)
    }

    override fun getDues(): List<Invoice> {
        return emptyList()
    }

    override fun getPaid(limit: Int): List<Invoice> {
        val currentYear = Year.now(ZoneOffset.ofHours(+8)).value // +8 = Philippines
        val response = invoiceApi.getPaymentHistory(accountId.get(), currentYear).execute()
        return PaymentHistoryDocumentToInvoices().apply(response.body()!!)
    }

}