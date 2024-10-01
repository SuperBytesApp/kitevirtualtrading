package com.upstox.marketdatafeeder.ui.frag

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.upstox.marketdatafeeder.Item2
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.SharedPreferencesManager
import com.upstox.marketdatafeeder.databinding.FragmentPortfolioBinding

class PortfolioFragment : Fragment() {


    lateinit var binding: FragmentPortfolioBinding
    lateinit var hDataRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    private lateinit var databaseReference2: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadfrag()

        return root
    }


    fun loadfrag() {


        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        val targetFragment = AllFragment()

        // Replace the current fragment with the target fragment
        transaction.replace(R.id.portfrag, targetFragment)
        transaction.addToBackStack(null) // Add to back stack to handle back navigation
        transaction.commit()

        val newColor = ContextCompat.getColor(requireContext(), R.color.body_grey)
        val newColor2 = ContextCompat.getColor(requireContext(), R.color.primary_blue)
        binding.textView3.setTextColor(newColor)
        binding.equity.visibility = View.GONE

        binding.textView2.setTextColor(newColor2)
        binding.view.visibility = View.VISIBLE


    }


}