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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import devcsrj.okhttp3.logging.HttpLoggingInterceptor
import isdue.Invoice
import isdue.InvoiceApi
import isdue.memoize
import okhttp3.HttpUrl
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.math.BigDecimal
import java.net.CookieManager
import java.text.DecimalFormat
import java.time.Year
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.function.Supplier

class MySkyInvoiceApi(private val baseUrl: HttpUrl,
                      private val authUrl: HttpUrl,
                      private val username: String,
                      private val password: String) : InvoiceApi {

    private val httpClient: OkHttpClient
    private val invoiceApi: MySkyInvoiceHttpApi

    internal var profile: Supplier<AbsCbnProfile>

    init {
        val cookieManager = CookieManager()
        this.httpClient = OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(cookieManager))
                .addInterceptor(HttpLoggingInterceptor())
                .build()

        this.profile = Supplier {
            val api = Retrofit.Builder()
                    .baseUrl(authUrl)
                    .addConverterFactory(JspoonConverterFactory.create())
                    .client(httpClient)
                    .validateEagerly(true)
                    .build()
                    .create(AbsCbnHttpApi::class.java)

            val call = api.login(AbsCbnLoginBody(username, password))
            val response = try {
                call.execute()
            } catch (e: Exception) {
                var rootCause = e as Throwable
                while (rootCause.cause != null)
                    rootCause = rootCause.cause as Throwable
                throw rootCause
            }
            response.body()!!
        }.memoize()

        val mapper = ObjectMapper()
        val module = SimpleModule()
        module.addDeserializer(MySkyAccount::class.java, MySkyAccountDeserializer())
        mapper.registerModule(module)
        this.invoiceApi = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JspoonConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(httpClient)
                .validateEagerly(true)
                .build()
                .create(MySkyInvoiceHttpApi::class.java)
    }

    override fun getProviderName() = "mysky-ph"

    override fun getDueInvoices(): List<Invoice> {
        val call = invoiceApi.getAccount(MySkyInvoiceHttpApi.AccountRequestBody(
                profile.get().skyAccountId, username))
        val body = call.execute().body() ?: return emptyList()
        return listOf(
                Invoice(body.id, body.amountDue, body.dueDate)
        )
    }

    override fun getPaidInvoices(limit: Int): List<Invoice> {
        val currentYear = Year.now(ZoneOffset.ofHours(+8)).value // +8 = Philippines
        val result = invoiceApi
                .getPaymentHistory(profile.get().skyAccountId, currentYear)
                .execute()
                .body() ?: return emptyList()

        val format = DecimalFormat("0,000.00")
        format.isParseBigDecimal = true
        return result.items.map {
            val amount = format.parse(it.amount.substringAfter("P ")) as BigDecimal
            val date = it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            Invoice(it.id, amount, date)
        }
    }

}

