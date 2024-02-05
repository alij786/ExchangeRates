package com.tc.exchangerates

import com.tc.exchangerates.component.RatesApi
import com.tc.exchangerates.viewmodel.MainViewModel
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import pl.droidsonroids.testing.mockwebserver.FixtureDispatcher
import pl.droidsonroids.testing.mockwebserver.condition.HTTPMethod
import pl.droidsonroids.testing.mockwebserver.condition.PathQueryCondition
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class MainViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var ratesApi: RatesApi

    lateinit var sut: MainViewModel

    companion object {
        private val mockWebServer = MockWebServer()
        private val fixtureDispatcher = FixtureDispatcher()

        @BeforeClass
        @JvmStatic
        fun setupServer() {
            fixtureDispatcher.putResponse(PathQueryCondition("/rates", HTTPMethod.GET), "rates_200")
            fixtureDispatcher.setFallbackResponse("connection_failure")
            mockWebServer.dispatcher = fixtureDispatcher
        }

        @AfterClass
        @JvmStatic
        fun teardownServer() {
            fixtureDispatcher.shutdown()
        }
    }

    @Before
    fun setup() {
        ratesApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(OkHttpClient())
            .build()
            .create()
    }

    @Test
    fun `mainViewModel initialization starts api call`() {
        runTest {
            ratesApi.getAllRates().run {
                assertNotNull(body())
                with(requireNotNull(body())) {
                    assertEquals(169, data.size)
                    assertEquals("BBD", data.first().symbol)
                }
            }
        }
    }
}
