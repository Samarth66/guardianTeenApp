package com.example.guardianteen

import FreeFallDetector
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            onFreeFallDetected()

        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        sendAlertButton = findViewById(R.id.sendAlertButton)
        val childId = intent.getStringExtra("childId") ?: ""

        sendAlertButton.setOnClickListener {
            getLastLocation(object : LocationCallback {
                override fun onLocationResult(locationString: String) {
                    val alertData = JSONObject().apply {
                        put("cid", childId)
                        put("type", "SOS")
                        put("time", System.currentTimeMillis())
                        put("location", locationString)
                    }
                    Toast.makeText(this@ChildScreenActivity, locationString, Toast.LENGTH_LONG).show()
                    sendAlert(alertData)
                }
            })
        }

    }

    private fun onFreeFallDetected() {
        Log.d("FreeFallDetector", "Free Fall Detected!")
        getLastLocation(object : LocationCallback {
            override fun onLocationResult(locationString: String) {
                sendFreeFallAlert(locationString, "6560094b5a32bd73e8f4a19c", "Fall Detected")
            }
        })
    }

    private fun sendFreeFallAlert(location: String, childId: String, type: String) {
        val alertData = JSONObject().apply {
            put("cid", childId)
            put("type", type)
            put("time", System.currentTimeMillis())
            put("location", location)
        }
        sendAlert(alertData)
    }

    private fun getLastLocation(callback: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationRequestCode
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val locationString = "$latitude, $longitude"
                    callback.onLocationResult(locationString)
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationRequestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLastLocation(object : LocationCallback {
                        override fun onLocationResult(locationString: String) {
                            Toast.makeText(this@ChildScreenActivity, locationString, Toast.LENGTH_LONG).show()
                        }
                    })
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        freeFallDetector.startListening()
    }

    override fun onPause() {
        super.onPause()
        freeFallDetector.stopListening()
    }

    private fun sendAlert(alertData: JSONObject) {
        Log.d("FreeFallDetector", "Free Fall Detected! 2")
        val url = "https://guardianteenbackend.onrender.com/create"

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

    interface LocationCallback {
        fun onLocationResult(locationString: String)
    }
}
