package com.example.guardianteen
import android.util.Log
import kotlinx.coroutines.*
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.URL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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
        locationListener = LocationListener { location ->
            CoroutineScope(Dispatchers.Main).launch {

                val speed = location.speed
                speedTextView?.text = "Speed: $speed m/s"
                if (speed > 0.5) {
                    showSpeedAlert(speed)
                }
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
    override fun onResume() {
        super.onResume()
        updateHealthDataViews()
    }

    private fun updateHealthDataViews() {
        val heartRate = HealthDataRepository.getInstance().getHeartRate()
        val respiratoryRate = HealthDataRepository.getInstance().getRespiratoryRate()

        heartRateTextView.text = "Heart Rate: $heartRate"
        respRateTextView.text = "Respiratory Rate: $respiratoryRate"
    }

    //SPEED Comp starts
    private fun showSpeedAlert(speed: Float) {
        Toast.makeText(this, "Speed limit exceeded! Current speed: ${speed} m/s", Toast.LENGTH_SHORT).show()

        // Vibrate the phone
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Check if the device has a vibrator
            val vibrationPattern = longArrayOf(0, 500, 100, 500) // Wait 0ms, Vibrate 500ms, Wait 100ms, Vibrate 500ms
            // Vibrate with the given pattern, -1 means don't repeat
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // New vibrate method for newer API levels
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                // Deprecated method for older API levels
                vibrator.vibrate(vibrationPattern, -1)
            }
        }
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