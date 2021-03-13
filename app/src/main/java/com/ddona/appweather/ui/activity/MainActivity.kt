package com.ddona.appweather.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.ddona.appweather.MyApp
import com.ddona.appweather.R
import com.ddona.appweather.databinding.ActivityMainBinding
import com.ddona.appweather.model.CurrentWeather
import com.ddona.appweather.model.List
import com.ddona.appweather.broardcast.NotifyBroardcast
import com.ddona.appweather.service.WeatherService
import com.ddona.appweather.ui.adapter.WeatherAdapter
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), WeatherAdapter.IWaether,
    SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val PERMISSION_ID = 52
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var service: WeatherService? = null
    private var connect: ServiceConnection? = null
    private var listWeather = mutableListOf<List>()
    private lateinit var broadcast: NotifyBroardcast

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainContainer.isGone = true
        binding.progressbar.isVisible = true

        broadcast = NotifyBroardcast()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        checkedInternet()

        binding.rc.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rc.adapter = WeatherAdapter(this)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.sv.setOnQueryTextListener(this)
    }

    // search city name
    override fun onQueryTextSubmit(text: String?): Boolean {
        MyApp.weatherViewModel.getWeatherByName(text.toString())
        MyApp.weatherViewModel.getIntervalWeatherByName(text.toString())
        register()
        return true
    }

    override fun onQueryTextChange(text: String?): Boolean {
        MyApp.weatherViewModel.getWeatherByName(text.toString())
        MyApp.weatherViewModel.getIntervalWeatherByName(text.toString())
        register()
        return true
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(broadcast, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcast)
    }

    // get current location
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                    var location: Location = it.result
                    if (location == null) {
                        getNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                        createConnectService(latitude, longitude)
                        register()
                    }
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please Enable your Location service",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.tvAsk.isGone = true
                binding.mainContainer.isVisible = true
                getLastLocation()
            } else {
                binding.tvAsk.isVisible = true
                binding.mainContainer.isGone = true
                requestPermission()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            latitude = lastLocation.latitude
            longitude = lastLocation.longitude
            MyApp.weatherViewModel.getWeatherLocation(latitude, longitude)
            MyApp.weatherViewModel.getIntervalWeather(latitude, longitude)
        }
    }

    // connect service

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createConnectService(latitude: Double, longitude: Double) {
        connect = object : ServiceConnection {

            override fun onServiceDisconnected(name: ComponentName?) {
                print("onServiceDisconnected")
            }

            override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
                val myBinder = binder as WeatherService.MyBinder
                service = myBinder.service
                if (service!!.getListWeather().size == 0 && service!!.getDataWeather() == null) {
                    repeatCall(latitude, longitude)
                }
                binding.progressbar.isGone = true
            }
        }
        val intent = Intent()
        intent.setClass(this, WeatherService::class.java)
        bindService(intent, connect!!, Context.BIND_AUTO_CREATE)
    }

    private fun register() {
        MyApp.weatherViewModel.dataWeather.observe(this, Observer {
            updateUI(it)
            latitude = it.coord.lat
            longitude = it.coord.lon
        })

        MyApp.weatherViewModel.listDataWeather.observe(this, Observer {
            listWeather.clear()
            for (i in 0..it.list.size - 1) {
                listWeather.add(it.list[i])
            }
            binding.rc.adapter!!.notifyDataSetChanged()
        })
    }

    // update ui
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun updateUI(dataWeather: CurrentWeather) {
        val date = SimpleDateFormat(" HH:mm - dd/MM/yyyy")
            .format(Date(dataWeather.dt * 1000L))
        Glide.with(this)
            .load("http://openweathermap.org/img/wn/${dataWeather.weather[0].icon}@2x.png")
            .into(binding.icon)
        val sunrise = SimpleDateFormat(" HH:mm")
            .format(Date(dataWeather.sys.sunrise * 1000L))
        val sunset = SimpleDateFormat(" HH:mm")
            .format(Date(dataWeather.sys.sunset * 1000L))

        binding.address.text = dataWeather.name + "," + dataWeather.sys.country
        binding.updateAt.text = "Update at : " + date
        binding.descreption.text = dataWeather.weather[0].description
        binding.temp.text = dataWeather.main.temp.toInt().toString() + "°C"
        binding.sunrise.text = sunrise
        binding.sunset.text = sunset
        binding.ssw.text = dataWeather.wind.speed.toString()
        binding.rain.text = dataWeather.clouds.all.toString() + "%"
    }

    override fun getCount() = listWeather.size

    override fun getData(position: Int): List {
        return listWeather[position]
    }

    // refresh
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRefresh() {
        binding.swipeRefreshLayout.isRefreshing = true
        MyApp.weatherViewModel.getWeatherLocation(latitude, longitude)
        MyApp.weatherViewModel.getIntervalWeather(latitude, longitude)
        register()
        binding.swipeRefreshLayout.isRefreshing = false
        checkedInternet()
    }

    // check internet
    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkedInternet() {
        if (isOnline(this) == false) {
            binding.disconnect.isVisible = true
            binding.mainContainer.isGone = true
        } else {
            binding.disconnect.isGone = true
            binding.mainContainer.isVisible = true
        }
    }

    // data 5 phút/lần
    fun repeatCall(lat: Double?, lon: Double?) {
        object : CountDownTimer(600000, 300000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished / 1000 > 300 || millisUntilFinished / 1000 <= 300) {
                    MyApp.weatherViewModel.getWeatherLocation(latitude, longitude)
                    MyApp.weatherViewModel.getIntervalWeather(latitude, longitude)
                }
                register()
            }

            override fun onFinish() {
                repeatCall(lat, lon)
            }
        }.start()
    }
}