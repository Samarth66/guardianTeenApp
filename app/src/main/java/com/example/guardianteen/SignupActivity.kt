package com.example.guardianteen

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class SignupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var userTypeSpinner: Spinner
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        userTypeSpinner = findViewById(R.id.userTypeSpinner)
        signupButton = findViewById(R.id.signupButton)

        setupSpinner()
        signupButton.setOnClickListener { handleSignup() }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("parent", "child")
        )
        userTypeSpinner.adapter = adapter
    }

    private fun handleSignup() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val userType = userTypeSpinner.selectedItem.toString()

        val queue = Volley.newRequestQueue(this)
        val url = "https://guardianteenbackend.onrender.com/signup"

        val userData = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
            put("userType", userType)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, userData,
            { response ->
                Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show()
            },
            { error ->
                if (error.networkResponse?.statusCode == 400) {
                    Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show()
                }
            }
        )

        queue.add(jsonObjectRequest)
    }
}