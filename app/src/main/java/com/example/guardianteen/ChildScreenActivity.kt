package com.example.guardianteen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.widget.Button

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView



import FreeFallDetector
import android.net.Uri
import android.os.Environment
import android.os.Looper


class ChildScreenActivity : AppCompatActivity() {





    private lateinit var locationUpdateHandler: Handler
    private val LOCATION_UPDATE_INTERVAL: Long = 15000 // 15 seconds
    private var childId: String = ""

    private lateinit var childIdTextView: TextView









    private lateinit var freeFallDetector: FreeFallDetector
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 101
    private lateinit var sendAlertButton: Button
    private val REQUEST_VIDEO_CAPTURE = 123
    companion object {
        private const val HEALTH_MONITOR_REQUEST_CODE = 1
    }
    private var speedTextView: TextView? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    //HEALTH RESULTS
    private lateinit var heartRateTextView: TextView
    private lateinit var respRateTextView: TextView




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_screen);
        //Deepak start
        childIdTextView = findViewById(R.id.childIdTextView)
        freeFallDetector = FreeFallDetector(this) {
            showConfirmationDialog()




        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sendAlertButton = findViewById(R.id.sendAlertButton)

        // Assign the value from the intent to the class-level childId variable
        childId = intent.getStringExtra("childId") ?: ""
        childIdTextView.text = "Child ID: $childId"




        //sendAlertButton.setOnClickListener { sendAlert(childId) }
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
            intent.putExtra("childId", childId)
            startActivityForResult(intent, HEALTH_MONITOR_REQUEST_CODE)
        }

        sendAlertButton.setOnClickListener {
            getLastLocation(object : LocationCallback {
                override fun onLocationResult(locationString: String) {
                    val alertData = JSONObject().apply {
                        put("cid", childId)
                        put("type", "SOS")
                        put("description", "Emergency SOS pressed")
                        put("time", System.currentTimeMillis())
                        put("location", locationString)
                    }
                    Toast.makeText(this@ChildScreenActivity, locationString, Toast.LENGTH_LONG).show()
                    sendAlert(alertData)
                    showEmergencyRecordingDialog()
                }
            })
        }
    }



    override fun onResume() {
        super.onResume()
        startRepeatingLocationUpdates()
        updateHealthDataViews()
        freeFallDetector.startListening()
    }

    override fun onPause() {
        super.onPause()
        stopRepeatingLocationUpdates()
        freeFallDetector.stopListening()
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            locationRequestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startRepeatingLocationUpdates()
                }
            }
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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


        //Deepak end
        //HEALTH RES

    private fun showEmergencyRecordingDialog() {
        AlertDialog.Builder(this)
            .setTitle("Emergency Recording")
            .setMessage("Do you want to start emergency recording?")
            .setPositiveButton("Yes") { _, _ ->
                startRecording()
            }
            .setNegativeButton("No", null)
            .show()
    }




    private fun startRecording() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        // Set the video duration limit in seconds
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
        }
    }


        // Dummy data for the alert
//        val alertData = JSONObject().apply {
//            put("cid", childId)
//            put("type", "Dummy Alert Type")
//            put("time", System.currentTimeMillis())
//            put("location", "Dummy Location")
//
//        }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            val videoUri: Uri? = data?.data
            videoUri?.let { uri ->
                try {
                    val videoFile = createVideoFile()
                    saveVideoToFile(uri, videoFile)
                    Toast.makeText(this, "Video saved to ${videoFile.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createVideoFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        return File.createTempFile(
            "VIDEO_${timeStamp}_", /* prefix */
            ".mp4", /* suffix */
            storageDir /* directory */
        )
    }

    private fun saveVideoToFile(videoUri: Uri, destFile: File) {
        contentResolver.openInputStream(videoUri).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
    }

    private fun showConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Are you alright?")
            .setPositiveButton("Yes") { _, _ ->

            }
            .setNegativeButton("No") { _, _ ->
                onFreeFallDetected()            }
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
                sendFreeFallAlert(locationString, childId, "Fall Detected")
            }
        })
    }

    private fun sendFreeFallAlert(location: String, childId: String, type: String) {
        val alertData = JSONObject().apply {
            put("cid", childId)
            put("type", type)
            put("description", "Emergency Assistance Needed")
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

    private fun updateHealthDataViews() {
        val heartRate = HealthDataRepository.getInstance().getHeartRate()
        val respiratoryRate = HealthDataRepository.getInstance().getRespiratoryRate()

        heartRateTextView.text = "Heart Rate: $heartRate"
        respRateTextView.text = "Respiratory Rate: $respiratoryRate"
    }

    //SPEED Comp starts
    private fun showSpeedAlert(speed: Float) {
        Toast.makeText(this, "Speed limit exceeded! Current speed: ${speed} m/s", Toast.LENGTH_SHORT).show()

        sendSpeedAlert( childId, "Over-speeding")
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

    private fun sendSpeedAlert(childId: String, type: String) {
        val alertData = JSONObject().apply {
            put("cid", childId)
            put("type", type)
            put("description", "Speed Limit Crossed")
            put("time", System.currentTimeMillis())
            put("location", "N/A")
        }
        sendAlert(alertData)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null && locationListener != null) {
            locationManager!!.removeUpdates(locationListener!!)
        }
    }
    // Handle the permission result

    //SPEED Comp END
}

