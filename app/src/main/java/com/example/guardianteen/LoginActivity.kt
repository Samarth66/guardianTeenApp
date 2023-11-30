package com.example.guardianteen
import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.tooling.preview.Preview
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.guardianteen.ChildScreenActivity
import com.example.guardianteen.ParentScreenActivity
import com.example.guardianteen.SignupActivity
import org.json.JSONObject
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.os.Handler
import android.os.Looper
import android.app.PendingIntent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


import android.os.Build
import kotlin.math.sqrt


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var userTypeSpinner: Spinner
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        userTypeSpinner = findViewById(R.id.userTypeSpinner)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)


        setupSpinner()
        loginButton.setOnClickListener { handleLogin() }
        signupButton.setOnClickListener { navigateToSignup() }
    }

    class MyFirebaseMessagingService : FirebaseMessagingService() {

        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            // Check if the message contains a notification payload.
            Log.d("FCM", "From: ${remoteMessage.from}")

            // Check if the message contains data
            if (remoteMessage.data.isNotEmpty()) {
                Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            }

            // Check if the message contains a notification payload
            remoteMessage.notification?.let {
                Log.d("FCM", "Message Notification Body: ${it.body}")
                Log.d("FCM", "Message Notification Title: ${it.title}")
            }
            remoteMessage.notification?.let {
                sendNotification(it.title ?: "New Message", it.body ?: "You have a new message.")
            }
        }

        private fun sendNotification(title: String, messageBody: String) {
            val intent = Intent(applicationContext, ParentScreenActivity::class.java)

            intent.putExtra("newAlertReceived", true)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            Log.d("NEWW", "notification send")

            val pendingIntent = PendingIntent.getActivity(

                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )

            val channelId = getString(R.string.default_notification_channel_id)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }



        override fun onNewToken(token: String) {
            // TODO: Send the new FCM registration token to your backend.
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("parent", "child")
        )
        userTypeSpinner.adapter = adapter
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val userType = userTypeSpinner.selectedItem.toString()



        val queue = Volley.newRequestQueue(this)
        val url = "https://guardianteenbackend.onrender.com/login"

        val userData = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("userType", userType)
        }



        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, userData,
            Response.Listener { response ->
                navigateBasedOnUserType(response, userType, email)
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> Toast.makeText(this, "Please Signup!", Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this, "Please enter correct credentials", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle the error when NetworkResponse is null
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }




    private fun navigateBasedOnUserType(response: JSONObject, userType: String, email: String) {
        try {

            val id = response.getString("id") // Get id as a string
            val name = response.getString("name")


            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    // Get the device token
                    if (token != null) {
                        // Store the device token in your database or use it as needed
                        Log.d("DeviceToken", "Device Token: $token")
                        updateDeviceToken(email, token)
                    } else {
                        Log.e("DeviceToken", "Device token is null")
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors that may occur during token retrieval
                    Log.e("DeviceToken", "Failed to get device token: $e")
                }
            Log.d("LoginSuccess", "User type: $userType, ID: $id, Name: $name")
            val intent = when (userType) {
                "parent" -> Intent(this, ParentScreenActivity::class.java).apply {
                    putExtra("parentId", id)
                    putExtra("parentName", name)
                }
                "child" -> Intent(this, ChildScreenActivity::class.java).apply {
                    putExtra("childId", id)
                    putExtra("childName", name)
                }
                else -> {
                    Log.e("LoginError", "Unexpected user type: $userType")
                    return  // Or handle an unexpected userType
                }
            }


            startActivity(intent)
        } catch (e: Exception) {
            Log.e("LoginError", "Error navigating based on user type: ${e.message}")
        }
    }

    private fun updateDeviceToken(email: String, deviceToken: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://guardianteenbackend.onrender.com/updateDeviceToken" // Replace with your API endpoint

        val tokenData = JSONObject().apply {
            put("email", email)
            put("deviceToken", deviceToken)
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, tokenData,
            Response.Listener<JSONObject> { response ->
                Log.d("TokenUpdateSuccess", "Device token updated successfully")
            },
            Response.ErrorListener { error ->
                Log.e("TokenUpdateError", "Error updating device token: ${error.message}")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }




    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }
}

class FreeFallActivity(private val context: Context, private val    onFreeFallDetected: () -> Unit) :
    SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    var lastHeartRate: Int = -1 // Public to be accessed by the activity

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this context
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> detectFreeFall(it)
                Sensor.TYPE_HEART_RATE -> handleHeartRate(it)
            }
        }
    }

    private fun detectFreeFall(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt((x * x + y * y + z * z).toDouble())

        val freeFallThreshold = 2.0 // Adjust this threshold based on your needs

        if (magnitude < freeFallThreshold) {
            onFreeFallDetected()
        }
    }

    private fun handleHeartRate(event: SensorEvent) {
        lastHeartRate = event.values[0].toInt() // Store the latest heart rate
        Log.d("FreeFallDetector", "Heart Rate updated: $lastHeartRate")

    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}