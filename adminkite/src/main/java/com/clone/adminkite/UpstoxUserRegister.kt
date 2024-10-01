package com.clone.adminkite

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.clone.adminkite.databinding.ActivityAdminPanelBinding
import com.clone.adminkite.databinding.ActivityUpstoxUserRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UpstoxUserRegister : AppCompatActivity() {

    lateinit var binding: ActivityUpstoxUserRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpstoxUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val validityDate = binding.validityDateEditText.text.toString()


            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User registration successful, get the user ID
                        val user = auth.currentUser
                        val userId = user?.uid

                        // Store user data in Firestore
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "validity" to validityDate,
                            "password" to password,
                            "id" to userId.toString(),
                            "type" to "upstox"


                        )

                        if (userId != null) {
                            firestore.collection("upstoxusers")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {


                                    Toast.makeText(this, "user created successfully", Toast.LENGTH_SHORT).show()
                                    // Data stored successfully
                                    // You can add more logic here

                                    binding.nameEditText.text.clear()
                                    binding.emailEditText.text.clear()
                                    binding.passwordEditText.text.clear()
                                    binding.validityDateEditText.text.clear()

                                }
                                .addOnFailureListener { e ->

                                    Toast.makeText(this, "${e.localizedMessage}", Toast.LENGTH_SHORT).show()

                                    // Handle errors while storing data
                                }
                        }
                    } else {
                        // User registration failed
                        // Handle the error, e.g., display an error message
                    }
                }
        }
    }
}
