package com.example.guardianteen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray
import android.text.method.ScrollingMovementMethod
import android.util.Log



class ParentScreenActivity : AppCompatActivity() {

    private lateinit var childIdEditText: EditText
    private lateinit var addChildButton: Button
    private lateinit var setGeofenceButton: Button
    private lateinit var healthVitalCheckButton: Button

    private val parentId: String? = null

    private lateinit var socket: Socket
    private lateinit var alertsLayout: LinearLayout
    private lateinit var refreshAlertsButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_screen)

        childIdEditText = findViewById(R.id.childIdEditText)
        addChildButton = findViewById(R.id.addChildButton)
        setGeofenceButton = findViewById(R.id.setGeofenceButton)
        healthVitalCheckButton = findViewById(R.id.healthVitalCheckButton)


        val parentId = intent.getStringExtra("parentId") ?: ""
        val parentName = intent.getStringExtra("parentName") ?: ""

        addChildButton.setOnClickListener { handleAddChild(parentId) }
        setGeofenceButton.setOnClickListener { handleSetGeofence() }
        healthVitalCheckButton.setOnClickListener { handleHealthVitalCheck() }


        alertsLayout = findViewById(R.id.alertsLayout)


        refreshAlertsButton = findViewById(R.id.refreshAlertsButton)
        refreshAlertsButton.setOnClickListener {
            val parentId = intent.getStringExtra("parentId") ?: ""
            fetchAlerts(parentId)
        }

        fetchAlerts(parentId)

        healthVitalCheckButton.setOnClickListener {
            sendHealthAlert(parentId)
        }

    }




    override fun onNewIntent(intent: Intent) {
        Log.d("NEWW", "From: new message")
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's current intent with the new one

        // Now you can retrieve extras from the new intent
        val parentId = intent.getStringExtra("parentId") ?: ""
        val newAlertReceived = intent.getBooleanExtra("newAlertReceived", false)
        if (newAlertReceived) {
            fetchAlerts(parentId)
        }
    }


    private fun fetchAlerts(parentId: String) {
        val url = "https://guardianteenbackend.onrender.com/fetch-alerts?pid=$parentId"

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val alerts = response.getJSONArray("alerts")
                displayAlerts(alerts)
            },
            { error ->
                Toast.makeText(this, "Failed to fetch alerts", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun displayAlerts(alerts: JSONArray) {
        val alertsLayout: LinearLayout = findViewById(R.id.alertsLayout)
        alertsLayout.removeAllViews() // Clear previous views

        for (i in 0 until alerts.length()) {
            val alert = alerts.getJSONObject(i)
            val childId = alert.getString("cid")
            val type = alert.getString("type")
            val description = alert.getString("description")
            val location = alert.getString("location")
            val time = alert.getString("time")

            val textView = TextView(this).apply {
                text = "Child: $childId\nType: $type\nDescription: $description\nLocation: $location\nTime: $time\n\n"
                setOnClickListener {
                    val locationParts = location.split(",")
                    if (locationParts.size == 2) {
                        val latitude = locationParts[0].toDoubleOrNull()
                        val longitude = locationParts[1].toDoubleOrNull()
                        if (latitude != null && longitude != null) {
                            openMapWithLocation(latitude, longitude)
                        }
                    }
                }
                textSize = 16f
                background = ContextCompat.getDrawable(context, android.R.drawable.edit_text)

                // Set padding (left, top, right, bottom)
                val paddingInPixels = 16 // example padding value
                setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels)
            }

            alertsLayout.addView(textView)
        }
    }

    private fun openMapWithLocation(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "No Map application found", Toast.LENGTH_SHORT).show()
        }
    }




    private fun sendHealthAlert(parentId: String) {
        val url = "https://guardianteenbackend.onrender.com/health-alert"

        // Dummy data for the health alert
        val healthAlertData = JSONObject().apply {
            put("pid", parentId)
            put("type", "Health Check")
            put("description", "Parent requesting health check")
            put("time", System.currentTimeMillis())
            put("location", "N/A")
        }

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, healthAlertData,
            { response ->
                Toast.makeText(this, "Health alert sent successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Failed to send health alert", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }


    private fun handleAddChild(parentId: String) {
        val childId = childIdEditText.text.toString()
        val url = "https://guardianteenbackend.onrender.com/add_child"

        val userData = JSONObject().apply {
            put("pid", parentId)
            put("cid", childId)
        }

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, userData,
            { response ->
                Toast.makeText(this, "Child added successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Please enter correct ChildId ", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun handleSetGeofence() {
        // Implement Set Geofence logic
        val parentId = intent.getStringExtra("parentId") ?: ""
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("parentId", parentId);
        startActivity(intent)
    }

    private fun handleHealthVitalCheck() {
        // Implement Health Vital Check logic
    }
}
