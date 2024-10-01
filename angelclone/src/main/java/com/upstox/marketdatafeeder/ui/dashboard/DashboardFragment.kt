package com.upstox.marketdatafeeder.ui.dashboard

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.upstox.marketdatafeeder.R
import com.upstox.marketdatafeeder.SharedPreferencesManager
import com.upstox.marketdatafeeder.UserData
import com.upstox.marketdatafeeder.databinding.FragmentDashboardBinding
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import com.upstox.marketdatafeeder.pref.WachlistData
import com.upstox.marketdatafeeder.pref.WatchlistSharedPreferencesManager
import com.upstox.marketdatafeeder.ui.frag.OptionsFragment
import com.upstox.marketdatafeeder.ui.frag.WatchlistFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Suppress("DEPRECATION")
class DashboardFragment : Fragment()  {


    lateinit var binding: FragmentDashboardBinding



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Load the blink animation
        val blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink_anim)

        // Apply the animation to the TextView
//        binding.newblink.startAnimation(blinkAnimation)

        loadYourFragment()
        binding.ivFilters.setOnClickListener {
            val context = context
            val alertDialog = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(context).inflate(R.layout.watchlist_add, null)
            val newNameEditText = dialogView.findViewById<EditText>(R.id.etName)
            val mlast = dialogView.findViewById<EditText>(R.id.etLastName)
            val nse = dialogView.findViewById<EditText>(R.id.etNSE)
            val id = dialogView.findViewById<EditText>(R.id.etMarketId)
            val id2 = dialogView.findViewById<EditText>(R.id.etMarketId2)
            val lot = dialogView.findViewById<EditText>(R.id.etLOT)

            var type = ""

            val radioGroup: RadioGroup = dialogView.findViewById(R.id.indexradioGroup)
            // Set a listener to respond to the RadioButton clicks
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedRadioButton: RadioButton = dialogView.findViewById(checkedId)

                // Handle the selected RadioButton
                when (selectedRadioButton.id) {
                    R.id.radioIndex -> {
                        // Code to handle Index Data selection
                        type = "1"
                    }
                    R.id.radioFO -> {
                        // Code to handle F&O Data selection
                        type = "2"
                    }

                    R.id.radioFON -> {
                        // Code to handle F&O Data selection
                        type = "3"
                    }

                }
            }


            alertDialog.setView(dialogView).setPositiveButton("Save") { _, _ ->

                val newName = newNameEditText.text.toString()
                val last = mlast.text.toString()
                val mnse = nse.text.toString()
                val mid = id.text.toString()
                val mid2 = id2.text.toString()
                val mlot = lot.text.toString()

                val sharedPreferencesManager = WatchlistSharedPreferencesManager(requireContext())

                // Get existing watchlist
                val existingWatchlist = sharedPreferencesManager.getWatchlist().toMutableList()

                // Add a new item to the watchlist
                existingWatchlist.add(WachlistData(newName, last, mnse, mid,type,mid2,mlot))

                // Save the updated watchlist
                sharedPreferencesManager.saveWatchlist(existingWatchlist)
            }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }.show()
        }



        binding.tvdummyOptionsTitle.setOnClickListener {
            loadOptionFragment()
        }


        binding.tvdummyWatchListTitle.setOnClickListener {
            loadYourFragment()
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadYourFragment() {
        // Implement the logic to load your fragment here
        // Example: Replace the current fragment with the new one
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.watchlistfrag, WatchlistFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        binding.tvdummyWatchListTitle.setTextColor(resources.getColor(R.color.title_black))

        // Set the font family using the font resource
        val customTypeface = resources.getFont(R.font.app_font_medium)
        binding.tvdummyWatchListTitle.typeface = customTypeface



        binding.tvdummyOptionsTitle.setTextColor(resources.getColor(R.color.title_black))

        // Set the font family using the font resource
        val customTypeface2 = resources.getFont(R.font.app_font_regular)
        binding.tvdummyOptionsTitle.typeface = customTypeface2



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadOptionFragment() {
        // Implement the logic to load your fragment here
        // Example: Replace the current fragment with the new one
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.watchlistfrag, OptionsFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()


         binding.tvdummyOptionsTitle.setTextColor(resources.getColor(R.color.title_black))

        // Set the font family using the font resource
        val customTypeface = resources.getFont(R.font.app_font_medium)
        binding.tvdummyOptionsTitle.typeface = customTypeface



         binding.tvdummyWatchListTitle.setTextColor(resources.getColor(R.color.title_black))

        // Set the font family using the font resource
        val customTypeface2 = resources.getFont(R.font.app_font_regular)
        binding.tvdummyWatchListTitle.typeface = customTypeface2




    }


}