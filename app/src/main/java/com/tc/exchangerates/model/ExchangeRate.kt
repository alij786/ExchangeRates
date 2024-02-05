package com.tc.exchangerates.model

data class ExchangeRate(
    val id: String,
    val symbol: String,
    val currencySymbol: String,
    val rateUsd: String,
    val type: String
)

data class ExchangeRateResponse(
    val data: List<ExchangeRate>
)
