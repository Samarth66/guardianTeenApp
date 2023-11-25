package com.example.guardianteen


import FreeFallDetector
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject





class ChildScreenActivity : AppCompatActivity() {

    private lateinit var freeFallDetector: FreeFallDetector
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 101
    private lateinit var sendAlertButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_screen)

        freeFallDetector = FreeFallDetector(this) {
            Log.d("FreeFallDetector", "Free Fall Detected!")
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val sosButton: Button = findViewById(R.id.sosButton)
        sosButton.setOnClickListener {
            checkLocationPermissionAndGetLocation()
        }

        sendAlertButton = findViewById(R.id.sendAlertButton)
        val childId = intent.getStringExtra("childId") ?: ""

        sendAlertButton.setOnClickListener { sendAlert(childId) }
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationRequestCode
            )
            return
        }
        getLastLocation()
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationRequestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLastLocation()
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        freeFallDetector.startListening() // Start listening for free fall
    }

    override fun onPause() {
        super.onPause()
        freeFallDetector.stopListening() // Stop listening to save resources
    }

    private fun sendAlert(childId: String) {
        val url = "https://guardianteenbackend.onrender.com/create"

        // Dummy data for the alert
        val alertData = JSONObject().apply {
            put("cid", "6560094b5a32bd73e8f4a19c")
            put("type", "Dummy Alert Type")
            put("time", System.currentTimeMillis())
            put("location", "Dummy Location")
        }

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, alertData,
            { response ->
                Toast.makeText(this, "Alert sent successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Failed to send alert", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }
}













