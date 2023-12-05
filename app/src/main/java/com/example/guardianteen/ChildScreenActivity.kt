package com.example.guardianteen

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class ChildScreenActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequestCode = 101
    private lateinit var locationUpdateHandler: Handler
    private val LOCATION_UPDATE_INTERVAL: Long = 15000 // 15 seconds
    private var childId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_screen)

        childId = intent.getStringExtra("childId") ?: ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }

    override fun onResume() {
        super.onResume()
        startRepeatingLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopRepeatingLocationUpdates()
    }

    private fun startRepeatingLocationUpdates() {
        locationUpdateHandler = Handler()
        locationUpdateHandler.postDelayed(object : Runnable {
            override fun run() {
                getLastLocation {
                    sendLocationUpdate(it)
                }
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL)
            }
        }, LOCATION_UPDATE_INTERVAL)
    }

    private fun stopRepeatingLocationUpdates() {
        locationUpdateHandler.removeCallbacksAndMessages(null)
    }

    private fun getLastLocation(callback: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationRequestCode)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val locationString = "${it.latitude}, ${it.longitude}"
                callback(locationString)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Call to superclass implementation


        when (requestCode) {
            locationRequestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startRepeatingLocationUpdates()
                }
            }
        }
    }

    private fun sendLocationUpdate(locationString: String) {
        val url = "https://guardianteenbackend.onrender.com/store-location" // Replace with your server endpoint
        val queue = Volley.newRequestQueue(this)

        val locationParts = locationString.split(", ")
        val coordinates = JSONArray().apply {
            put(locationParts[1].toDouble()) // Longitude
            put(locationParts[0].toDouble()) // Latitude
        }

        val locationData = JSONObject().apply {
            put("cid", childId)
            put("coordinates", coordinates)
            put("timestamp", System.currentTimeMillis())
        }

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, locationData,
            { response ->
                Log.d("ChildScreenActivity", "Location sent successfully")
            },
            { error ->
                Log.e("ChildScreenActivity", "Failed to send location", error)
            }
        )
        queue.add(jsonObjectRequest)
    }
}

