package com.upstox.marketdatafeeder.ui.frag

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.upstox.marketdatafeeder.HoldingAdapter
import com.upstox.marketdatafeeder.Item2
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.databinding.FragmentHoldingBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class HoldingFragment : Fragment() {


    lateinit var binding: FragmentHoldingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHoldingBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }


}
