package com.example.guardianteen

import FreeFallActivity
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
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper

class ChildScreenActivity : AppCompatActivity() {

    private lateinit var freeFallActivity: FreeFallActivity
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 101
    private lateinit var sendAlertButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_screen)

        freeFallActivity = FreeFallActivity(this) {
            showConfirmationDialog()

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
    private fun showConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Are you alright?")
            .setPositiveButton("Yes") { _, _ ->

            }
            .setNegativeButton("No") { _, _ ->
                sendFallAlertImmediately()            }
            .setCancelable(false)
            .create()

        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onFreeFallDetected()  // User did not respond, send the alert
            }
        }, 15000)
    }
    private fun onFreeFallDetected() {
        Log.d("FreeFallDetector", "Free Fall Detected!")
        getLastLocation(object : LocationCallback {
            override fun onLocationResult(locationString: String) {
                sendFreeFallAlert(locationString, "6560094b5a32bd73e8f4a19c", "Fall Detected")
            }
        })
    }
    private fun sendFallAlertImmediately() {
        getLastLocation(object : LocationCallback {
            override fun onLocationResult(locationString: String) {
                sendFreeFallAlert(locationString, "6560094b5a32bd73e8f4a19c", "Fall Detected")
            }
        })
    }

    private fun sendFreeFallAlert(location: String, childId: String, baseType: String) {
        val heartRate = freeFallActivity.lastHeartRate

        val type = if (heartRate >= 0) {
            "$baseType (heartrate: $heartRate)"
        } else {
            baseType
        }
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
        freeFallActivity.startListening()
    }

    override fun onPause() {
        super.onPause()
        freeFallActivity.stopListening()
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
    private fun generateReport() {
        getLastLocation(object : LocationCallback {
            override fun onLocationResult(locationString: String) {
                val fallDetected = freeFallActivity.lastHeartRate

                val reportData = JSONObject().apply {
                    put("childId", "6560094b5a32bd73e8f4a19c")
                    put("lastKnownLocation", locationString)
                    put("fallDetected", fallDetected)
                    put("reportGeneratedTime", System.currentTimeMillis())
                }

                sendReport(reportData)
            }
        })
    }

    private fun sendReport(reportData: JSONObject) {
        // Here you can choose to save the report locally, display it to the user,
        // or send it to a server.
        Log.d("ChildScreenActivity", "Report: $reportData")

        // Optionally, convert the report to a string and display in the UI, or send it to a server
        val reportString = reportData.toString(2)
        displayReport(reportString)
    }

    private fun displayReport(report: String) {
        // Code to update the UI with the report data
        // This is just a placeholder to show where you would display the report
        Toast.makeText(this, "Report: $report", Toast.LENGTH_LONG).show()
    }

    interface LocationCallback {
        fun onLocationResult(locationString: String)
    }
}