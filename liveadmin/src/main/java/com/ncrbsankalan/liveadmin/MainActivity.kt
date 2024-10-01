package com.ncrbsankalan.liveadmin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ncrbsankalan.liveadmin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    var totaluser: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.angel.setOnClickListener {




        }





        binding.refresh.setOnClickListener {

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }



    }


    fun zcount() {

        val db = FirebaseFirestore.getInstance()

// Reference to your Firestore collection
        val collectionReference = db.collection("user")

// Use the get() method to fetch all documents in the collection
        collectionReference.get()
            .addOnSuccessListener { documents ->
                // Get the count of documents in the collection
                val count = documents.size()
                // Do something with the count, e.g., display it or use it in your app
                binding.kitecount.text = count.toString()

                totaluser += count
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                println("Error getting documents: ${e.message}")
            }

    }

    fun ucount() {

        val db = FirebaseFirestore.getInstance()

// Reference to your Firestore collection
        val collectionReference = db.collection("upstoxusers")

// Use the get() method to fetch all documents in the collection
        collectionReference.get()
            .addOnSuccessListener { documents ->
                // Get the count of documents in the collection
                val count = documents.size()
                // Do something with the count, e.g., display it or use it in your app
                binding.upstoxcount.text = count.toString()
                totaluser += count


            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                println("Error getting documents: ${e.message}")
            }

    }

    fun acount() {

        val db = FirebaseFirestore.getInstance()

// Reference to your Firestore collection
        val collectionReference = db.collection("angelusers")

// Use the get() method to fetch all documents in the collection
        collectionReference.get()
            .addOnSuccessListener { documents ->
                // Get the count of documents in the collection
                val count = documents.size()
                // Do something with the count, e.g., display it or use it in your app
                binding.angelcount.text = count.toString()
                totaluser += count

                binding.totaluser.text = totaluser.toString()

            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                println("Error getting documents: ${e.message}")
            }

    }


    fun gcount() {

        val db = FirebaseFirestore.getInstance()

// Reference to your Firestore collection
        val collectionReference = db.collection("usersgrow")

// Use the get() method to fetch all documents in the collection
        collectionReference.get()
            .addOnSuccessListener { documents ->
                // Get the count of documents in the collection
                val count = documents.size()
                // Do something with the count, e.g., display it or use it in your app
                binding.growcount.text = count.toString()
                totaluser += count

                binding.totaluser.text = totaluser.toString()

            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                println("Error getting documents: ${e.message}")
            }

    }

    fun kcount() {

        val db = FirebaseFirestore.getInstance()

// Reference to your Firestore collection
        val collectionReference = db.collection("kotakusers")

// Use the get() method to fetch all documents in the collection
        collectionReference.get()
            .addOnSuccessListener { documents ->
                // Get the count of documents in the collection
                val count = documents.size()
                // Do something with the count, e.g., display it or use it in your app
                binding.kotakcount.text = count.toString()
                totaluser += count

                binding.totaluser.text = totaluser.toString()

            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                println("Error getting documents: ${e.message}")
            }

    }


}