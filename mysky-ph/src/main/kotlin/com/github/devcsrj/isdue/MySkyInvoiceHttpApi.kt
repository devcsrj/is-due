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
package com.github.devcsrj.isdue

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.nio.charset.StandardCharsets

internal interface MySkyInvoiceHttpApi {

    @GET("/myaccounts/payment-history")
    @Headers("X-Requested-With: XMLHttpRequest")
    fun getPaymentHistory(@Query("accountnumber") accountNumber: String,
                          @Query("year") year: Int): Call<MySkyInvoices>

    @POST("/myaccounts")
    @Headers("X-Requested-With: XMLHttpRequest")
    fun getAccount(@Body body: AccountRequestBody): Call<MySkyAccount>

    class AccountRequestBody(private val accountNumber: String,
                             private val email: String) : RequestBody() {

        override fun writeTo(sink: BufferedSink?) {
            sink!!.apply {
                writeString("""
                    {
                        "accountNumbers": {
                            "SkyCableNumber": "$accountNumber"
                        },
                        "emails": [
                            "$email"
                        ]
                    }
                    """.trimIndent(), StandardCharsets.UTF_8)
            }
        }

        override fun contentType(): MediaType? {
            return MediaType.parse("application/json")
        }

    }
}