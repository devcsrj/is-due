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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.devcsrj.retrofit2.HtmlConverterFactory
import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Suppliers
import devcsrj.okhttp3.logging.HttpLoggingInterceptor
import okhttp3.HttpUrl
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.math.BigDecimal
import java.net.CookieManager
import java.time.LocalDate
import java.time.Year
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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

        val mapper = ObjectMapper()
        val module = SimpleModule()
        module.addDeserializer(MySkyAccount::class.java, MySkyAccount.Deserializer())
        mapper.registerModule(module)
        this.invoiceApi = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(HtmlConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(httpClient)
                .validateEagerly(true)
                .build()
                .create(MySkyInvoiceHttpApi::class.java)
    }

    override fun getDueInvoices(): List<Invoice> {
        val call = invoiceApi.getAccount(MySkyInvoiceHttpApi.AccountRequestBody(accountId.get(), username))
        val body = call.execute().body() ?: return emptyList()
        return listOf(
                Invoice(body.id, body.amountDue, body.dueDate)
        )
    }

    override fun getPaidInvoices(limit: Int): List<Invoice> {
        val currentYear = Year.now(ZoneOffset.ofHours(+8)).value // +8 = Philippines
        val response = invoiceApi.getPaymentHistory(accountId.get(), currentYear).execute()
        return PaymentHistoryDocumentToInvoices().apply(response.body()!!)
    }

}

internal data class MySkyAccount(val id: String,
                                 val dueDate: LocalDate,
                                 val amountDue: BigDecimal) {

    internal class Deserializer : StdDeserializer<MySkyAccount>(MySkyAccount::class.java) {

        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): MySkyAccount {
            //The server returns a horrid response that looks like:
            /*{
                 "customerNameInfo": {
                 "customerName": "NAME IN ALL CAPS",
                 "customerAddress": "ADDRESS IN ALL CAPS"
             },
             "customerAccounts": [
                 {
                 "ACCOUNTNUMBER": "124121521",
                 "SERVICETYPE": "CABLE",
                 "SUBSCRIPTIONNAME": "",
                 "PACKAGEDESCRIPTION": "Free One SKY Cable Plan 250(Z)                    ",
                 "ACCOUNTSTATUS": "A",
                 "TOTALAMOUNTDUE": "0",
                 "AGEAMOUNT": "0",
                 "PAYMENTDUEDATE": "20180105",
                 "STATEMENTOFACCOUNTASOF": "20171215",
                 "BOXINFORMATION": [{
                         "SMARTCARDID": "1701015986          ",
                         "BOXRESOLUTION": "SD"
                     }]
                 }
             ],
             "ESOA": {
                 "Message": "",
                 "Status": 0,
                 "AccountNumber": {
                 "CABLE": "124121521"
             },
             "NewEmail": "your.email@gmail.com",
             "OldEmail": "your.email@gmail.com",
             "FirstName": "FIRSTNAME",
             "LastName": "LASTNAME",
             "CustomerNumber": "124211"
             },
             "ESOA_HASH": "4dc0e5962d87f23339339d5cf876f0afef351f9c"
             }*/

            // let's fetch only those that we need
            val node = p!!.codec.readTree<JsonNode>(p)

            // screaming properties
            val accountNumber = node["customerAccounts"][0]["ACCOUNTNUMBER"].asText()
            val dueDateStr = node["customerAccounts"][0]["PAYMENTDUEDATE"].asText()
            val amountStr = node["customerAccounts"][0]["TOTALAMOUNTDUE"].asText()

            val dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"))
            val amount = BigDecimal(amountStr)

            return MySkyAccount(accountNumber, dueDate, amount)
        }

    }
}
