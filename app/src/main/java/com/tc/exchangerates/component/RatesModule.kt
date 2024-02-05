package com.tc.exchangerates.component

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object RatesModule {

    @Provides
    fun providesRatesApi(): RatesApi = Retrofit.Builder()
        .baseUrl("https://api.coincap.io/v2/")
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build().create(RatesApi::class.java)
}
