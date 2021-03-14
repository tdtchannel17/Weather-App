package com.ddona.appweather.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ddona.appweather.api.ApiWeather
import com.ddona.appweather.api.RetrofitUtils
import com.ddona.appweather.model.CurrentWeather
import com.ddona.appweather.model.ListWeather
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class WeatherViewModel() : ViewModel() {
    private val apiWeather: ApiWeather
    val dataWeather: MutableLiveData<CurrentWeather>
    val listDataWeather: MutableLiveData<ListWeather>

    init {
        apiWeather = RetrofitUtils.createRetrofit()
        dataWeather = MutableLiveData()
        listDataWeather = MutableLiveData()
    }

    @SuppressLint("CheckResult")
    fun getWeatherLocation(lat: Double, lon: Double) {
        apiWeather.getCurrentWeather(lat, lon)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    dataWeather.value = it
                },
                {
                    Log.e("Error !!!", "----------------- " + it + " -----------------")
                }
            )
    }

    @SuppressLint("CheckResult")
    fun getIntervalWeather(lat: Double, lon: Double) {
        apiWeather.getIntervalWeather(lat, lon)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    listDataWeather.value = it
                },
                {
                    Log.e("Error !!!", "----------------- " + it + " -----------------")
                }
            )
    }

    // search name
    @SuppressLint("CheckResult")
    fun getWeatherByName(name: String): Disposable {
        val disposable = apiWeather.getWeatherByName(name)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    dataWeather.value = it
                },
                {
                    Log.e("Error !!!", "----------------- " + it + " -----------------")
                }
            )
        return disposable
    }

    @SuppressLint("CheckResult")
    fun getIntervalWeatherByName(name: String): Disposable {
        val disposable = apiWeather.getIntervalWeatherByName(name)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    listDataWeather.value = it
                    Log.d("listDataWeather :", "listDataWeather : " + listDataWeather.value)
                },
                {
                    Log.e("Error !!!", "----------------- " + it + " -----------------")
                }
            )
        return disposable
    }
}