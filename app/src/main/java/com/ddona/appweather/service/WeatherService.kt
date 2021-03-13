package com.ddona.appweather.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.ddona.appweather.MyApp
import com.ddona.appweather.model.CurrentWeather
import com.ddona.appweather.model.List

class WeatherService : LifecycleService() {
    private var dataWeather: CurrentWeather? = null
    private var listDataWeather = mutableListOf<List>()

    fun getDataWeather() = dataWeather
    fun getListWeather() = listDataWeather

    override fun onCreate() {
        super.onCreate()
        MyApp.weatherViewModel.dataWeather.observe(this, Observer {
            dataWeather = it
        })
        MyApp.weatherViewModel.listDataWeather.observe(this, Observer {
            listDataWeather.clear()
            listDataWeather.addAll(it.list)
        })
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return MyBinder(this)

    }

    class MyBinder(val service: WeatherService) : Binder()
}