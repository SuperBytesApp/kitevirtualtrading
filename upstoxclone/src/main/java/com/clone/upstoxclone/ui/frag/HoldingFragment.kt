package com.clone.upstoxclone.ui.frag

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.clone.upstoxclone.HoldingAdapter
import com.clone.upstoxclone.R
import com.clone.upstoxclone.databinding.FragmentHoldingBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class HoldingFragment : Fragment() {


    lateinit var binding : FragmentHoldingBinding
    var itemList = mutableListOf<Item2>()
    private lateinit var databaseReference: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHoldingBinding.inflate(inflater, container, false)
        val root: View = binding.root



        val auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        val uid = currentUser?.uid


        databaseReference = FirebaseDatabase.getInstance().getReference("holdingData").child(uid.toString())


        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = HoldingAdapter(itemList,this)


        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.upstoxrefback)


        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.upstoxref),
            ContextCompat.getColor(requireContext(), R.color.upstoxref),
            ContextCompat.getColor(requireContext(), R.color.upstoxref)
        )

        binding.swipeRefreshLayout.setOnRefreshListener {

            Handler().postDelayed({


                shareddata()


                // Stop the refresh animation
                binding.swipeRefreshLayout.isRefreshing = false
            }, 500) // 1000 milliseconds = 1 second delay


        }


        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs3", Context.MODE_PRIVATE)


        binding.cardView2.setOnClickListener {
            showDialog()
        }

        shareddata()




        return root
    }






    fun loadrecycler(){




        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing list
                var itemcount = 0
                itemList.clear()

                // Iterate through the dataSnapshot and add items to the list
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item2::class.java)
                    itemcount++
                    if (item != null) {
                        itemList.add(item)

                        binding.positionhide.visibility = View.GONE
                        binding.allcount.text = "All ($itemcount)"

                    }
                }

                // Notify the adapter that the data has changed

                binding.recyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database errors
            }
        })




    }



    override fun onResume() {
        super.onResume()
        loadrecycler()
    }



    @SuppressLint("MissingInflatedId")
    private fun showDialog() {

        val invested = sharedPreferences.getString("invested", "")
        val current = sharedPreferences.getString("current", "")
        val plProfit = sharedPreferences.getString("plProfit", "")
        val plPercentProfit = sharedPreferences.getString("plPercentProfit", "")


        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_layout2, null)
        builder.setView(dialogView)

        val btnSave = dialogView.findViewById<Button>(R.id.btnDialogSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)

        val editInvested = dialogView.findViewById<EditText>(R.id.editInvested)
        val editCurrent = dialogView.findViewById<EditText>(R.id.editCurrent)
        val editPLProfit = dialogView.findViewById<EditText>(R.id.editPLProfit)
        val editPLPercentProfit = dialogView.findViewById<EditText>(R.id.editPLPercentProfit)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)




        var radio = ""
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected radio button by its ID
            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)

            // Check which radio button was clicked and perform an action
            when (selectedRadioButton.id) {
                R.id.radioProfit -> {
                    // Radio Button 1 is clicked
                    // Perform your action here
                    radio = "profit"

                }
                R.id.radioLoss -> {
                    // Radio Button 2 is clicked
                    // Perform your action here
                    radio = "loss"

                }
            }
        }









        if (invested != ""&& current != ""){

            editInvested.setText(invested)
            editCurrent.setText(current)
            editPLProfit.setText(plProfit)
            editPLPercentProfit.setText(plPercentProfit)

        }



        btnSave.setOnClickListener {
            val invested = editInvested.text.toString()
            val current = editCurrent.text.toString()
            val plProfit = editPLProfit.text.toString()
            val plPercentProfit = editPLPercentProfit.text.toString()
            val isProfit = radio

            // Save the data in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("invested", invested)
            editor.putString("current", current)
            editor.putString("plProfit", plProfit)
            editor.putString("plPercentProfit", plPercentProfit)
            editor.putString("isProfit", isProfit)
            editor.apply()

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }


    fun shareddata(){
        val invested = sharedPreferences.getString("invested", "")
        val current = sharedPreferences.getString("current", "")
        val plProfit = sharedPreferences.getString("plProfit", "")
        val plPercentProfit = sharedPreferences.getString("plPercentProfit", "")
        val isProfit = sharedPreferences.getString("isProfit","")

        if (invested != "" && current != ""){


            if (isProfit == "loss"){

                val textColor = ContextCompat.getColor(requireContext(), R.color.red2)
                val textColor2 = ContextCompat.getColor(requireContext(), R.color.lossbg)
                binding.totalreturn.setTextColor(textColor)
                binding.todayreturn.setTextColor(textColor)





            }else{

                val textColor = ContextCompat.getColor(requireContext(), R.color.green)
                val textColor2 = ContextCompat.getColor(requireContext(), R.color.profitbg)
                binding.totalreturn.setTextColor(textColor)
                binding.todayreturn.setTextColor(textColor)





            }



            var invest = formatIntWithDecimal(invested!!.toDouble())
            var curr = formatIntWithDecimal(current!!.toDouble())

            binding.numberTextView.text = invest
            binding.textView33.text = curr
            binding.totalreturn.text = plProfit
            binding.todayreturn.text = plPercentProfit






        }




    }

    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }



}