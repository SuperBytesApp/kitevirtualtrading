package com.clone.adminkite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.clone.adminkite.databinding.ActivityMainBinding
import com.clone.adminkite.databinding.ActivityUpstoxListAcitivityBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    var totaluser : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        zcount()
        ucount()
        acount()





        binding.zerodha.setOnClickListener {

            intent = Intent(this, AdminPanelActivity::class.java)
            startActivity(intent)

        }

         binding.upstox.setOnClickListener {

            intent = Intent(this, UpstoxUserRegister::class.java)
            startActivity(intent)

        }


        binding.angel.setOnClickListener {

            intent = Intent(this, ActivityAngelUserRegister::class.java)
            startActivity(intent)

        }





        binding.refresh.setOnClickListener {

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

        binding.userlist.setOnClickListener {

            intent = Intent(this, UserListActivity::class.java)
            startActivity(intent)

        }

    binding.userlist2.setOnClickListener {

            intent = Intent(this, UpstoxListAcitivity::class.java)
            startActivity(intent)

        }

  binding.userlist3.setOnClickListener {

            intent = Intent(this, AngelListAcitivity::class.java)
            startActivity(intent)

        }




    }


    fun zcount(){

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

    fun ucount(){

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

    fun acount(){

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





}