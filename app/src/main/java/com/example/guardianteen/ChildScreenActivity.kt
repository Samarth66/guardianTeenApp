package com.example.guardianteen

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ChildScreenActivity : AppCompatActivity() {

    private lateinit var sendAlertButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_screen)

        sendAlertButton = findViewById(R.id.sendAlertButton)
        val childId = intent.getStringExtra("childId") ?: ""

        sendAlertButton.setOnClickListener { sendAlert(childId) }
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
