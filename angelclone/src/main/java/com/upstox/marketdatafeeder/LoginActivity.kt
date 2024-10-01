package com.upstox.marketdatafeeder

import androidx.appcompat.app.AppCompatActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.upstox.marketdatafeeder.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth


    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {
                            // Login success
                            // You can navigate to another activity or perform other actions here

                            getUserOnlineStatus()

                        } else {


                            Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show()

                            // Login failed
                            // Handle the error, e.g., show a message to the user
                        }
                    }
            } else {
                // Handle empty fields

            }
        }


        // my angel code
//
//        binding.registerButton2.setOnClickListener {
//
//            binding.login.visibility = View.VISIBLE
//            binding.registerButton.visibility = View.GONE
//            binding.registerButton2.visibility = View.GONE
//
//        }
//
//        binding.registerButton.setOnClickListener {
//
//
//            val url = "https://t.me/zerodhacloneb"  // Replace with your desired URL
//
//            // Create an Intent to open a web browser
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//
//            // Start the web browser activity
//            startActivity(intent)
//
//        }




    }





    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    private fun getUserOnlineStatus() {
        val user = auth.currentUser

        user?.let { currentUser ->
            val uid = currentUser.uid
            val userRef = databaseReference.child("angelusers").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isOnline = dataSnapshot.child("isOnline").getValue(String::class.java)

                    val deviceId = getDeviceId(this@LoginActivity)

                    if (isOnline == deviceId) {
                        // User is already logged in on this device
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                        Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()
                    } else if (isOnline != null && isOnline.isNotEmpty()) {
                        // User is logged in on another device
                        Toast.makeText(this@LoginActivity, "This Account Already Logged In Other Device!", Toast.LENGTH_SHORT).show()
                    } else {
                        // User is not logged in on any device, log them in
                        userRef.child("isOnline").setValue(deviceId)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                        Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur during the read operation
                    println("Database Error: ${databaseError.message}")
                }
            })
        }
    }






}