package com.example.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.roundToInt

data class WeatherInfo(
    val temperatureCelsius: Int = 18,
    val cityName: String = "STANDORT",
    val weatherCode: Int = 0,
    val conditionDescription: String = "Klar",
    val isGpsLocation: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

class LocationWeatherService(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownCoordinates(): Pair<Double, Double>? {
        if (!hasLocationPermission() || locationManager == null) return null

        var bestLocation: Location? = null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.time > bestLocation.time) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LocationWeatherService", "Error reading provider $provider: ${e.message}")
            }
        }

        return bestLocation?.let { Pair(it.latitude, it.longitude) }
    }

    @Suppress("DEPRECATION")
    private fun resolveCityName(lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                addr.locality
                    ?: addr.subAdminArea
                    ?: addr.adminArea
                    ?: addr.featureName
            } else null
        } catch (e: Exception) {
            Log.w("LocationWeatherService", "Geocoder failed: ${e.message}")
            null
        }
    }

    suspend fun fetchWeather(fallbackCity: String): WeatherInfo = withContext(Dispatchers.IO) {
        val coords = getLastKnownCoordinates()
        val isGps = coords != null

        val targetLat: Double
        val targetLon: Double
        val cityName: String

        if (coords != null) {
            targetLat = coords.first
            targetLon = coords.second
            cityName = resolveCityName(targetLat, targetLon) ?: fallbackCity
        } else {
            // Geocode fallback city using Open-Meteo Geocoding API
            val geocoded = geocodeCity(fallbackCity)
            if (geocoded != null) {
                targetLat = geocoded.first
                targetLon = geocoded.second
                cityName = geocoded.third
            } else {
                // Default coordinates (Linz / Central Europe)
                targetLat = 48.3064
                targetLon = 14.2858
                cityName = fallbackCity.ifBlank { "LINZ" }
            }
        }

        // Fetch Live Weather from Open-Meteo
        val weatherRes = queryOpenMeteo(targetLat, targetLon)
        if (weatherRes != null) {
            WeatherInfo(
                temperatureCelsius = weatherRes.first,
                cityName = cityName.uppercase(),
                weatherCode = weatherRes.second,
                conditionDescription = mapWeatherCode(weatherRes.second),
                isGpsLocation = isGps,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            WeatherInfo(
                temperatureCelsius = 20,
                cityName = cityName.uppercase(),
                weatherCode = 0,
                conditionDescription = "Online",
                isGpsLocation = isGps,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    private fun geocodeCity(cityName: String): Triple<Double, Double, String>? {
        return try {
            val encoded = URLEncoder.encode(cityName.trim(), "UTF-8")
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=de")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val results = json.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val first = results.getJSONObject(0)
                    val lat = first.getDouble("latitude")
                    val lon = first.getDouble("longitude")
                    val name = first.optString("name", cityName)
                    Triple(lat, lon, name)
                } else null
            } else null
        } catch (e: Exception) {
            Log.w("LocationWeatherService", "Geocoding error: ${e.message}")
            null
        }
    }

    private fun queryOpenMeteo(lat: Double, lon: Double): Pair<Int, Int>? {
        return try {
            val urlStr = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val current = json.optJSONObject("current")
                if (current != null) {
                    val temp = current.getDouble("temperature_2m").roundToInt()
                    val code = current.optInt("weather_code", 0)
                    Pair(temp, code)
                } else null
            } else null
        } catch (e: Exception) {
            Log.w("LocationWeatherService", "Open-Meteo query error: ${e.message}")
            null
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Klar"
            1 -> "Meist sonnig"
            2 -> "Teils wolkig"
            3 -> "Bedeckt"
            45, 48 -> "Nebel"
            51, 53, 55 -> "Nieselregen"
            61, 63, 65 -> "Regen"
            71, 73, 75, 77 -> "Schnee"
            80, 81, 82 -> "Schauer"
            85, 86 -> "Schneeschauer"
            95, 96, 99 -> "Gewitter"
            else -> "Heiter"
        }
    }
}
