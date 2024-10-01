package com.clone.upstoxclone.ui.frag

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.clone.upstoxclone.R
import com.clone.upstoxclone.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class UserFragment : Fragment() {

    lateinit var binding : FragmentUserBinding
    private lateinit var addFundButton: CardView
    private lateinit var balanceTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false)
        val root: View = binding.root


        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentBalance = sharedPreferences.getFloat("balance", 0.0f)
        updateBalanceTextView(currentBalance)

        binding.cardView4.setOnClickListener {
            showAddBalanceDialog()
        }






//        binding.logout.setOnClickListener {
//
////
////            auth.signOut()
////
//
//            val user = auth.currentUser
//            user?.let { currentUser ->
//                val uid = currentUser.uid
//                val userRef = databaseReference.child("users").child(uid)
//
//                userRef.child("isOnline").setValue("")
//
//
//                Toast.makeText(requireContext(), "LogOut", Toast.LENGTH_SHORT).show()
//
//                var intent = Intent(context,LoginActivity::class.java)
//                requireActivity().startActivity(intent)
//                requireActivity().finish()
//
//
//            }

//        }


        return root
    }



    private fun showAddBalanceDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Balance (INR)")

        val input = EditText(requireContext())
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
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        var formattedBalance = formatter.format(balance.toDouble())
        binding.textView31.text = "₹$formattedBalance"
        binding.textView32.text = "₹$formattedBalance"
        binding.availbalance.text = "₹$formattedBalance"
        binding.avaitrade.text = "₹$formattedBalance"
    }


}