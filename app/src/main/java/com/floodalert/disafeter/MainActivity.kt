package com.floodalert.disafeter

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.floodalert.disafeter.model.WeatherData
import com.floodalert.disafeter.screens.AppScaffold
import com.floodalert.disafeter.theme.FloodWatcherTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    // Location Tracker Dependencies
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create notification channel
        createNotificationChannel()

        // Make full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set main app content
        setContent {
            FloodWatcherTheme {
                AppScaffold(mainViewModel)
            }
        }

        // Start Location Tracker
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(120)
            fastestInterval = TimeUnit.SECONDS.toMillis(60)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (result.lastLocation != null) mainViewModel.setUserLocation(result.lastLocation!!)
            }
        }
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Request User Location
        requestUserLocation()

        // Ask user to open GPS if GPS is turned off
        mainViewModel.requestLocation.observe(this) {
            if (it && !isLocationEnabled()) requestToEnableLocation()
        }

        mainViewModel.weatherData.observe(this) {
            startFloodWarningService(it)
        }

        mainViewModel.vibratePhone.observe(this) {
            if (it) {
                vibrator.cancel()
                vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    private fun requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            startLocationPermissionRequest()
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val requestMultiplePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.entries.all { it.value }) requestUserLocation()
        else Toast.makeText(this, "Location access is denied.", Toast.LENGTH_SHORT).show()
    }

    private fun startLocationPermissionRequest() {
        requestMultiplePermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Check if GPS is on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private fun requestToEnableLocation() {
        val settingRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val task: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this)
            .checkLocationSettings(settingRequest)

        task.addOnSuccessListener {
            requestUserLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun startFloodWarningService(weatherData: WeatherData) {
        val floodWarningIntent = Intent(this, NotificationService::class.java)
        floodWarningIntent.putExtra("temperature", weatherData.temperature)
        floodWarningIntent.putExtra("description", weatherData.desc)

        startService(floodWarningIntent)
    }

    companion object {
        const val REQUEST_CHECK_SETTINGS = 999
        const val CHANNEL_ID = "floodServiceChannel"
    }
}