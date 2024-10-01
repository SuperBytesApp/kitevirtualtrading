package com.clone.kitevirtualtrading

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clone.kitevirtualtrading.databinding.ActivityTestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private val fluctuationInterval = 1000L // Adjust the interval in milliseconds



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val handler = Handler(Looper.getMainLooper())

        var isFluctuating = true
        var currentValue = 0.toDouble()



        val minv = "-2".toInt().toDouble()
        val maxv = 5.toDouble()

        binding.button.setOnClickListener{

            handler.postDelayed(object : Runnable {
                @SuppressLint("NotifyDataSetChanged")
                override fun run() {
                    if (!isFluctuating) {
                        // Stop fluctuation when max value is reached
                        return
                    }



                    // Generate a random fluctuation between -5 and 5
                    val fluctuation = Random.nextDouble(minv, maxv + 1)

                    // Update the currentValue with the fluctuation
                    currentValue += fluctuation

                    binding.test.text = currentValue.toString()



                    // Continue fluctuation
                    handler.postDelayed(this, fluctuationInterval)
                }
            }, fluctuationInterval)




        }


 binding.button2.setOnClickListener{

            handler.postDelayed(object : Runnable {
                @SuppressLint("NotifyDataSetChanged")
                override fun run() {
                    if (!isFluctuating) {
                        // Stop fluctuation when max value is reached
                        return
                    }



                    // Generate a random fluctuation between -5 and 5
                    val fluctuation = Random.nextDouble(minv, maxv + 1)

                    // Update the currentValue with the fluctuation
                    currentValue += fluctuation

                    binding.test2.text = currentValue.toString()



                    // Continue fluctuation
                    handler.postDelayed(this, fluctuationInterval)
                }
            }, fluctuationInterval)




        }



        var a = binding.test.text.toString().toDouble() + binding.test2.text.toString().toDouble()

        binding.total.text = a.toString()



    }
}

