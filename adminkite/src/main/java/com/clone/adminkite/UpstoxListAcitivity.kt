package com.clone.adminkite

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clone.adminkite.databinding.ActivityUpstoxListAcitivityBinding
import com.google.firebase.firestore.FirebaseFirestore

class UpstoxListAcitivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpstoxListAcitivityBinding
    private val userList = mutableListOf<User>()
    private lateinit var userAdapter: UserAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpstoxListAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList,2)
        binding.recyclerView.adapter = userAdapter

        // Initialize Firebase Firestore
        val db = FirebaseFirestore.getInstance()

        // Retrieve user data from Firestore
        db.collection("upstoxusers")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle errors here
            }
    }
}
