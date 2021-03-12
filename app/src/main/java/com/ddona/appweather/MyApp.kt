package com.ddona.appweather

import android.app.Application
import com.ddona.appweather.viewmodel.WeatherViewModel

class MyApp : Application() {
    companion object {
        lateinit var weatherViewModel: WeatherViewModel
    }

    override fun onCreate() {
        super.onCreate()
        weatherViewModel = WeatherViewModel()
    }
}