package com.example.guardianteen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ChildScreenActivity : AppCompatActivity() {

    companion object {
        private const val HEALTH_MONITOR_REQUEST_CODE = 1
    }
    //SPEED Comp
    private var speedTextView: TextView? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    //HEALTH RESULTS
    private lateinit var heartRateTextView: TextView
    private lateinit var respRateTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_screen);
        //HEALTH RES
        heartRateTextView = findViewById(R.id.heartRateTextView)
        respRateTextView = findViewById(R.id.respRateTextView)
        //SPEED Comp starts
        speedTextView = findViewById(R.id.speedTextView)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { location -> // Speed in meters/second
            val speed = location.speed
            speedTextView?.text = "Speed: $speed m/s" // Safe call
            if (speed > 5) {
                showSpeedAlert()
            }
        }
        // Request permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            // Start listening to location updates
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener!!
            )
        }
    //SPEED Comp ends
        //Button for heaLTH
        val btnOpenHealthMonitor = findViewById<Button>(R.id.btnOpenHealthMonitor)
        btnOpenHealthMonitor.setOnClickListener {
            val intent = Intent(this@ChildScreenActivity, HealthRateCalculator::class.java)
            startActivityForResult(intent, HEALTH_MONITOR_REQUEST_CODE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == HEALTH_MONITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val heartRate = it.getIntExtra("heartRate", 0)
                val respRate = it.getIntExtra("respRate", 0)

                // Update your UI here with the received heart rate and respiratory rate
            }
        }
    }

    //SPEED Comp starts
    private fun showSpeedAlert() {
        // Example using a Toast message
        Toast.makeText(this, "Speed limit exceeded!", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null && locationListener != null) {
            locationManager!!.removeUpdates(locationListener!!)
        }
    }
    // Handle the permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0f,
                    locationListener!!
                )
            }
        }
    }
    //SPEED Comp END
}