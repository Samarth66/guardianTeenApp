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
import FreeFallDetector
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.net.URL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ChildScreenActivity : AppCompatActivity() {
    private lateinit var freeFallDetector: FreeFallDetector
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 101
    private lateinit var sendAlertButton: Button
    private val REQUEST_VIDEO_CAPTURE = 123
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
        //Deepak start
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
                    showEmergencyRecordingDialog()
                }
            })
        }
        //Deepak end
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

    override fun onResume() {
        super.onResume()
        updateHealthDataViews()
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