package com.kipita.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.repository.LiveWeather
import com.kipita.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    fun refresh(lat: Double = 40.7128, lon: Double = -74.0060) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { weatherRepository.getCurrent(lat, lon) }
                .onSuccess { wx -> _state.value = WeatherUiState(loading = false, current = wx) }
                .onFailure {
                    _state.value = _state.value.copy(loading = false, error = "Weather unavailable")
                    errorLogger.log("WeatherViewModel.refresh", it)
                }
        }
    }
}

data class WeatherUiState(
    val loading: Boolean = false,
    val current: LiveWeather? = null,
    val error: String? = null
)
