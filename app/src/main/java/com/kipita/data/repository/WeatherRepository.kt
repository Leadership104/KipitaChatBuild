package com.kipita.data.repository

import com.kipita.data.api.WeatherApiService
import com.kipita.data.api.toWeatherDescription
import com.kipita.data.api.toWeatherEmoji
import javax.inject.Inject

data class LiveWeather(
    val emoji: String,
    val description: String,
    val temperatureC: Double,
    val windKmh: Double,
    val humidity: Int
)

class WeatherRepository @Inject constructor(
    private val api: WeatherApiService
) {
    suspend fun getCurrent(lat: Double, lon: Double): LiveWeather {
        val dto = api.getForecast(lat = lat, lon = lon)
        val current = dto.current ?: error("Missing weather payload")
        return LiveWeather(
            emoji = current.weather_code.toWeatherEmoji(),
            description = current.weather_code.toWeatherDescription(),
            temperatureC = current.temperature_2m,
            windKmh = current.wind_speed_10m,
            humidity = current.relative_humidity_2m
        )
    }
}
