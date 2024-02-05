package com.tc.exchangerates.component

import com.tc.exchangerates.model.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET

interface RatesApi {
    @GET("rates")
    suspend fun getAllRates(): Response<ExchangeRateResponse>
}
