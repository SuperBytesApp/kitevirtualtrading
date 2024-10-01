package com.clone.kitevirtualtrading

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.clone.kitevirtualtrading.databinding.ActivityFundBinding
import java.util.Locale

class Fund_Activity : AppCompatActivity() {

    lateinit var binding: ActivityFundBinding
    private lateinit var addFundButton: CardView
    private lateinit var balanceTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFundButton = findViewById(R.id.cardView3)
        balanceTextView = findViewById(R.id.numberTextView)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentBalance = sharedPreferences.getFloat("balance", 0.0f)
        updateBalanceTextView(currentBalance)

        addFundButton.setOnClickListener {
            showAddBalanceDialog()
        }
    }

    private fun showAddBalanceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Balance (INR)")

        val input = EditText(this)
        input.hint = "Enter the amount"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val amount = input.text.toString().toFloatOrNull()
            if (amount != null) {
                updateBalance(amount)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updateBalance(amount: Float) {
        val currentBalance = sharedPreferences.getFloat("balance", 0.0f)
        val newBalance = amount

        val editor = sharedPreferences.edit()
        editor.putFloat("balance", newBalance)
        editor.apply()

        updateBalanceTextView(newBalance)
    }

    private fun updateBalanceTextView(balance: Float) {
        val formattedBalance = String.format(Locale.US, "₹%.2f", balance)
        balanceTextView.text = "$formattedBalance"
        binding.textView19.text = "$formattedBalance"
        binding.f3.text = "$formattedBalance"
    }
}