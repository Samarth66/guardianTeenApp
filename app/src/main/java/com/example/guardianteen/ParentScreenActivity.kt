package com.example.guardianteen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ParentScreenActivity : AppCompatActivity() {

    private lateinit var childIdEditText: EditText
    private lateinit var addChildButton: Button
    private lateinit var setGeofenceButton: Button
    private lateinit var healthVitalCheckButton: Button

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
                Toast.makeText(this, "Failed to add child", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun handleSetGeofence() {
        // Implement Set Geofence logic
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun handleHealthVitalCheck() {
        // Implement Health Vital Check logic
    }
}
