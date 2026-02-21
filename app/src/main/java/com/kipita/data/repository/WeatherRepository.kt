package com.kipita.data.repository

import com.kipita.data.api.CityCoordinates
import com.kipita.data.api.WeatherApiService
import com.kipita.data.api.toWeatherDescription
import com.kipita.data.api.toWeatherEmoji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CityWeather(
    val city: String,
    val tempC: Double,
    val highC: Double,
    val lowC: Double,
    val emoji: String,
    val description: String,
    val humidity: Int,
    val windKmh: Double,
    val lastUpdated: String
)

class WeatherRepository(private val api: WeatherApiService) {

    private val cache = mutableMapOf<String, Pair<Long, CityWeather>>()
    private val cacheTtlMs = 15 * 60 * 1000L // 15 minutes

    suspend fun getWeather(city: String): CityWeather? = withContext(Dispatchers.IO) {
        val cached = cache[city.lowercase()]
        if (cached != null && System.currentTimeMillis() - cached.first < cacheTtlMs) {
            return@withContext cached.second
        }

        val coords = CityCoordinates.get(city) ?: return@withContext null
        runCatching {
            val response = api.getForecast(
                lat = coords.first,
                lon = coords.second
            )
            val current = response.current ?: return@withContext null
            val daily = response.daily
            val weather = CityWeather(
                city = city,
                tempC = current.temperature_2m,
                highC = daily?.temperature_2m_max?.firstOrNull() ?: current.temperature_2m + 2,
                lowC = daily?.temperature_2m_min?.firstOrNull() ?: current.temperature_2m - 4,
                emoji = current.weather_code.toWeatherEmoji(),
                description = current.weather_code.toWeatherDescription(),
                humidity = current.relative_humidity_2m,
                windKmh = current.wind_speed_10m,
                lastUpdated = current.time
            )
            cache[city.lowercase()] = Pair(System.currentTimeMillis(), weather)
            weather
        }.getOrNull()
    }
}
