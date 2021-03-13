package com.ddona.appweather.api

import com.ddona.appweather.model.CurrentWeather
import com.ddona.appweather.model.ListWeather
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

// api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&appid={API key}
// api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}

interface ApiWeather {

    @GET(value = "/data/2.5/weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String = "en",
        @Query("units") units: String = "metric",
        @Query("APPID") APPID: String = "6d585f62409a79c557b9e54543e3031f"
    ): Observable<CurrentWeather>

    @GET(value = "/data/2.5/forecast")
    fun getIntervalWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String = "en",
        @Query("units") units: String = "metric",
        @Query("cnt") cnt: Int = 7,
        @Query("APPID") APPID: String = "6d585f62409a79c557b9e54543e3031f"
    ): Observable<ListWeather>
}