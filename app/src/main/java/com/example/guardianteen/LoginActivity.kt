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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.guardianteen.ChildScreenActivity
import com.example.guardianteen.ParentScreenActivity
import com.example.guardianteen.SignupActivity
import org.json.JSONObject

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
                navigateBasedOnUserType(response, userType)
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


    private fun navigateBasedOnUserType(response: JSONObject, userType: String) {
        try {

            val id = response.getString("id") // Get id as a string
            val name = response.getString("name")


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


    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }
}
