package com.tc.exchangerates.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tc.exchangerates.component.RatesApi
import com.tc.exchangerates.model.ExchangeRate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val ratesApi: RatesApi
) : ViewModel() {

    /*
    This section is a lot of boilerplate required to get the main functions to pass state
    and effects back to the view. The orbit-mvi library can get rid of most of this boilerplate,
    but it's here as an illustration of how this functionality is generally achieved.
     */
    private val _events: MutableSharedFlow<MainEvent> =
        MutableSharedFlow(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    @VisibleForTesting
    val events = _events.asSharedFlow()

    private val _state: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private val _effects: Channel<UIEffect> =
        Channel(capacity = Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects = _effects.receiveAsFlow()

    fun postEvent(event: MainEvent) {
        _events.tryEmit(event)
    }

    private fun setState(newState: MainUiState.() -> MainUiState) {
        _state.value = state.value.newState()
    }

    private fun setEffect(effect: () -> UIEffect) {
        _effects.trySend(effect())
    }

    private fun eventHandler(event: MainEvent) {
        when (event) {
            is MainEvent.GetRates -> getRates()
        }
    }

    private fun subscribeEvents() {
        viewModelScope.launch {
            events.collect(this@MainViewModel::eventHandler)
        }
    }

    init {
        subscribeEvents()
        getRates()
    }

    // This method is the basic idea of the Intent in Model-View-Intent architecture.
    // We access it using the events passed from the view, ensuring single point-of-entry
    // into the view model.
    private fun getRates() {
        viewModelScope.launch {
            setState { copy(loading = true, rates = emptyList()) }
            withContext(Dispatchers.IO) {
//                try {
                    val response = ratesApi.getAllRates()
                    if (response.isSuccessful) {
                        response.body()?.run {
                            setState { copy(loading = false, rates = data.sortedBy { it.symbol }) }
                        }
                        setEffect { UIEffect.CompleteMessage }
                    } else {
                        throw IOException("${response.code()}: ${response.message()}")
                    }
//                } catch (e: Throwable) {
//                    setState { copy(loading = false) }
//                    setEffect { UIEffect.ErrorMessage(e.message ?: "Unknown Error") }
//                }
            }
        }
    }
}

data class MainUiState(
    val loading: Boolean = false,
    val rates: List<ExchangeRate> = emptyList()
)

sealed class MainEvent {
    object GetRates : MainEvent()
}

sealed class UIEffect {
    object CompleteMessage : UIEffect()
    data class ErrorMessage(val message: String) : UIEffect()
}
