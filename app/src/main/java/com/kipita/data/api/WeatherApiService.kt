package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Query

// Open-Meteo: completely free, no API key, real-time global weather
// Docs: https://open-meteo.com/en/docs
interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 7
    ): OpenMeteoResponseDto
}

data class OpenMeteoResponseDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "",
    val current: CurrentWeatherDto? = null,
    val daily: DailyWeatherDto? = null
)

data class CurrentWeatherDto(
    val time: String = "",
    val temperature_2m: Double = 0.0,
    val weather_code: Int = 0,
    val wind_speed_10m: Double = 0.0,
    val relative_humidity_2m: Int = 0
)

data class DailyWeatherDto(
    val time: List<String> = emptyList(),
    val temperature_2m_max: List<Double> = emptyList(),
    val temperature_2m_min: List<Double> = emptyList(),
    val weather_code: List<Int> = emptyList()
)

// WMO weather code to emoji mapping
fun Int.toWeatherEmoji(): String = when (this) {
    0 -> "☀️"
    1, 2 -> "🌤"
    3 -> "☁️"
    45, 48 -> "🌫"
    51, 53, 55, 61, 63, 65 -> "🌧"
    71, 73, 75, 77 -> "❄️"
    80, 81, 82 -> "🌦"
    85, 86 -> "🌨"
    95 -> "⛈"
    96, 99 -> "🌩"
    else -> "🌡"
}

fun Int.toWeatherDescription(): String = when (this) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Foggy"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rainy"
    71, 73, 75, 77 -> "Snowy"
    80, 81, 82 -> "Showers"
    95 -> "Thunderstorm"
    else -> "Variable"
}

// City coordinates for major travel destinations
object CityCoordinates {
    val coords = mapOf(
        "Tokyo" to Pair(35.6762, 139.6503),
        "Bali" to Pair(-8.3405, 115.0920),
        "Lisbon" to Pair(38.7169, -9.1395),
        "Chiang Mai" to Pair(18.7883, 98.9853),
        "Medellín" to Pair(6.2476, -75.5658),
        "Tallinn" to Pair(59.4370, 24.7536),
        "Barcelona" to Pair(41.3851, 2.1734),
        "Singapore" to Pair(1.3521, 103.8198),
        "Bangkok" to Pair(13.7563, 100.5018),
        "Mexico City" to Pair(19.4326, -99.1332),
        "Paris" to Pair(48.8566, 2.3522),
        "New York" to Pair(40.7128, -74.0060),
        "Dubai" to Pair(25.2048, 55.2708),
        "London" to Pair(51.5074, -0.1278),
        "Sydney" to Pair(-33.8688, 151.2093)
    )

    fun get(city: String): Pair<Double, Double>? = coords[city]
        ?: coords.entries.firstOrNull { it.key.contains(city, ignoreCase = true) }?.value
}
