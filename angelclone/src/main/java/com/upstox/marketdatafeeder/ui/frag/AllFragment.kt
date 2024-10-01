package com.upstox.marketdatafeeder.ui.frag

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.SharedPreferencesManager
import com.upstox.marketdatafeeder.UserData
import com.upstox.marketdatafeeder.databinding.FragmentAllBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AllFragment : Fragment() {

    lateinit var binding : FragmentAllBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAllBinding.inflate(inflater, container, false)
        val root: View = binding.root




        return root

    }





}