package com.clone.kitevirtualtrading.ui

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
import com.clone.kitevirtualtrading.Item
import com.clone.kitevirtualtrading.Item2
import com.clone.kitevirtualtrading.R
import com.clone.kitevirtualtrading.adapter.HoldingAdapter
import com.clone.kitevirtualtrading.databinding.FragmentHoldingBinding
import com.clone.kitevirtualtrading.ui.dashboard.ItemAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HoldingFragment : Fragment() , ItemAdapter.ItemClickListener {


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



        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.zerodha),
            ContextCompat.getColor(requireContext(), R.color.zerodha),
            ContextCompat.getColor(requireContext(), R.color.zerodha)
        )

        binding.swipeRefreshLayout.setOnRefreshListener {

            Handler().postDelayed({




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

    override fun onItemClick(item: Item, fluctuationText: String, sell: String) {
        TODO("Not yet implemented")
    }





    fun loadrecycler(){




        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing list
                itemList.clear()

                // Iterate through the dataSnapshot and add items to the list
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item2::class.java)
                    if (item != null) {
                        itemList.add(item)

//                        binding.demo.visibility = View.GONE
//                        binding.cardView2.visibility = View.VISIBLE
//                        binding.cardView.visibility = View.VISIBLE
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



    private fun showDialog() {

        val invested = sharedPreferences.getString("invested", "")
        val current = sharedPreferences.getString("current", "")
        val plProfit = sharedPreferences.getString("plProfit", "")
        val plPercentProfit = sharedPreferences.getString("plPercentProfit", "")
        val dayPLProfit = sharedPreferences.getString("dayPLProfit", "")
        val dayPLPercentProfit = sharedPreferences.getString("dayPLPercentProfit", "")


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
        val editDayPLProfit = dialogView.findViewById<EditText>(R.id.editDayPLProfit)
        val editDayPLPercentProfit = dialogView.findViewById<EditText>(R.id.editDayPLPercentProfit)
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
            editDayPLProfit.setText(dayPLProfit)
            editDayPLPercentProfit.setText(dayPLPercentProfit)

        }



        btnSave.setOnClickListener {
            val invested = editInvested.text.toString()
            val current = editCurrent.text.toString()
            val plProfit = editPLProfit.text.toString()
            val plPercentProfit = editPLPercentProfit.text.toString()
            val dayPLProfit = editDayPLProfit.text.toString()
            val dayPLPercentProfit = editDayPLPercentProfit.text.toString()
            val isProfit = radio

            // Save the data in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("invested", invested)
            editor.putString("current", current)
            editor.putString("plProfit", plProfit)
            editor.putString("plPercentProfit", plPercentProfit)
            editor.putString("dayPLProfit", dayPLProfit)
            editor.putString("dayPLPercentProfit", dayPLPercentProfit)
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
        val dayPLProfit = sharedPreferences.getString("dayPLProfit", "")
        val dayPLPercentProfit = sharedPreferences.getString("dayPLPercentProfit", "")
        val isProfit = sharedPreferences.getString("isProfit","")

        if (invested != "" && current != ""){


            if (isProfit == "loss"){

                val textColor = ContextCompat.getColor(requireContext(), R.color.red2)
                val textColor2 = ContextCompat.getColor(requireContext(), R.color.lossbg)
                binding.textView35.setTextColor(textColor)
                binding.percprofitpl.setTextColor(textColor)
                binding.dayprofit.setTextColor(textColor)
                binding.textView37.setTextColor(textColor)


                binding.percprofitpl.setBackgroundColor(textColor2)





            }else{

                val textColor = ContextCompat.getColor(requireContext(), R.color.green)
                val textColor2 = ContextCompat.getColor(requireContext(), R.color.profitbg)
                binding.textView35.setTextColor(textColor)
                binding.percprofitpl.setTextColor(textColor)
                binding.dayprofit.setTextColor(textColor)
                binding.textView37.setTextColor(textColor)

                binding.percprofitpl.setBackgroundColor(textColor2)





            }


            binding.numberTextView.text = invested
            binding.textView33.text = current
            binding.textView35.text = plProfit
            binding.percprofitpl.text = plPercentProfit
            binding.dayprofit.text = dayPLProfit
            binding.textView37.text = dayPLPercentProfit





        }




    }



}