package com.ddona.appweather.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.ddona.appweather.R
import com.ddona.appweather.api.ApiWeather
import com.ddona.appweather.api.RetrofitUtils
import com.ddona.appweather.model.CurrentWeather
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NotificationService() : LifecycleService() {

    private val apiWeather: ApiWeather
    private var lat: Double = 0.0
    private var lon: Double = 0.0

    init {
        apiWeather = RetrofitUtils.createRetrofit()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        lat = intent?.getExtras()?.get("latitude") as Double
        lon = intent?.getExtras()?.get("longitude") as Double
        getWeatherLocation(lat, lon)
        return START_STICKY
    }

    @SuppressLint("CheckResult")
    fun getWeatherLocation(lat: Double, lon: Double) {
        apiWeather.getCurrentWeather(lat, lon)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    repeatCall(it)
                },
                {
                    Log.e("Error !!!", "----------------- " + it + " -----------------")
                }
            )
    }

    private fun repeatCall(data: CurrentWeather) {
        object : CountDownTimer(600000, 300000) {
            override fun onTick(millisUntilFinished: Long) {
                createNotificationChannel(data)
            }

            override fun onFinish() {
                repeatCall(data)
            }
        }.start()
    }

    private fun createNotificationChannel(data: CurrentWeather) {
        createChannel()
        var text = data.main.temp.toInt().toString() + "°C" + " - " + data.weather[0].description
        var title = data.name + "," + data.sys.country

        val builder = NotificationCompat.Builder(this, "notifyLemubit")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(1, builder)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notifyLemubit", "NO", importance)
            channel.description = "NO"
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}