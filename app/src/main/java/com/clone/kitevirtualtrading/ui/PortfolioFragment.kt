package com.clone.kitevirtualtrading.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.clone.kitevirtualtrading.Item
import com.clone.kitevirtualtrading.Item2
import com.clone.kitevirtualtrading.R
import com.clone.kitevirtualtrading.SharedPreferencesManager
import com.clone.kitevirtualtrading.UserData
import com.clone.kitevirtualtrading.databinding.FragmentPortfolioBinding
import com.clone.kitevirtualtrading.ui.notifications.NotificationsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PortfolioFragment : Fragment() {


    lateinit var binding : FragmentPortfolioBinding
    lateinit var database: FirebaseDatabase
    lateinit var myDataRef: DatabaseReference
    lateinit var hDataRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReference2: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val auth = FirebaseAuth.getInstance()

// Check if a user is currently authenticated
        val currentUser = auth.currentUser

        // The user is authenticated, and you can retrieve the UID
        val uid = currentUser?.uid

        database = FirebaseDatabase.getInstance()
        myDataRef = database.getReference("myData").child(uid.toString())
        hDataRef = database.getReference("holdingData").child(uid.toString())
        databaseReference = FirebaseDatabase.getInstance().getReference("myData").child(uid.toString())
        databaseReference2 = FirebaseDatabase.getInstance().getReference("holdingData").child(uid.toString())

        loadfrag()


        binding.textView4.setOnClickListener {

       loadfrag()

        }

        binding.textView2.setOnClickListener {

            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            val targetFragment = HoldingFragment()

            // Replace the current fragment with the target fragment
            transaction.replace(R.id.portfoliofrag, targetFragment)
            transaction.addToBackStack(null) // Add to back stack to handle back navigation
            transaction.commit()

              val newColor = ContextCompat.getColor(requireContext(), R.color.zerodha2)
              val newColor2 = ContextCompat.getColor(requireContext(), R.color.zerodha3)
            val newColor3 = ContextCompat.getColor(requireContext(), R.color.zerodha4)

            binding.textView2.setTextColor(newColor)
              binding.textView38.setTextColor(newColor)
              binding.textView38.visibility = View.VISIBLE
              binding.textView4.setTextColor(newColor2)
              binding.textView5.visibility = View.GONE
            binding.countcard.setCardBackgroundColor(newColor3)
            binding.countcard2.setCardBackgroundColor(newColor)


        }

        binding.textView.setOnClickListener {


            showAddItemDialog()

        }

        binding.holding.setOnClickListener {

            holding()

        }


        return root
    }




    fun loadfrag(){



        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        val targetFragment = NotificationsFragment()

        // Replace the current fragment with the target fragment
        transaction.replace(R.id.portfoliofrag, targetFragment)
        transaction.addToBackStack(null) // Add to back stack to handle back navigation
        transaction.commit()

        val newColor = ContextCompat.getColor(requireContext(), R.color.zerodha2)
        val newColor2 = ContextCompat.getColor(requireContext(), R.color.zerodha3)
        val newColor3 = ContextCompat.getColor(requireContext(), R.color.zerodha4)
        binding.textView4.setTextColor(newColor)
        binding.textView5.setTextColor(newColor)
        binding.textView5.visibility = View.VISIBLE
        binding.textView2.setTextColor(newColor2)
        binding.textView38.visibility = View.GONE
        binding.countcard.setCardBackgroundColor(newColor)
        binding.countcard2.setCardBackgroundColor(newColor3)


    }





    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        var sub = dialogView.findViewById<Button>(R.id.submitButton)
        var sell = dialogView.findViewById<Button>(R.id.sell)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup2)
        val radioGroup2 = dialogView.findViewById<RadioGroup>(R.id.radioGroup1)
        val check1 = dialogView.findViewById<CheckBox>(R.id.onepostion)
        val check2 = dialogView.findViewById<CheckBox>(R.id.twopostion)
        val check3 = dialogView.findViewById<CheckBox>(R.id.treepostion)
        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()




        if (userData != null){

            check1.isChecked = true
            check1.isClickable = false

        }

        if (userData2 != null){

            check2.isChecked = true
            check2.isClickable = false


        }

        if (userData3 != null){

            check3.isChecked = true
            check3.isClickable = false

        }





        var radio = ""  // nrml / mis
        var radio2 = "" // weekly / monthly
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected radio button by its ID
            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)

            // Check which radio button was clicked and perform an action
            when (selectedRadioButton.id) {
                R.id.radioButton2A -> {
                    // Radio Button 1 is clicked
                    // Perform your action here
                    radio = "nrml"

                }
                R.id.radioButton2B -> {
                    // Radio Button 2 is clicked
                    // Perform your action here
                    radio = "mis"

                }
            }
        }

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected radio button by its ID
            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)

            // Check which radio button was clicked and perform an action
            when (selectedRadioButton.id) {
                R.id.radioButton1A -> {
                    // Radio Button 1 is clicked
                    // Perform your action here
                    radio2 = "W"

                }
                R.id.radioButton1B -> {
                    // Radio Button 2 is clicked
                    // Perform your action here
                    radio2 = "M"

                }
            }
        }










        sub.setOnClickListener {




            if (check1.isChecked && userData == null ){



                val text1 = dialogView.findViewById<EditText>(R.id.editText)
                val text2 = dialogView.findViewById<EditText>(R.id.editText2)
                val text3 = dialogView.findViewById<EditText>(R.id.editText3)

                val text4 = dialogView.findViewById<EditText>(R.id.editText34)
                val text5 = dialogView.findViewById<EditText>(R.id.editText4)
                val text6 = dialogView.findViewById<EditText>(R.id.ltpmax)


                var name = text1.text.toString()
                var last = text2.text.toString()
                var qty = text3.text.toString()
                val avg = text4.text.toString()
                val ltpmin = text5.text.toString()
                val ltpmax = text6.text.toString()



                if (name.isNotEmpty() && last.isNotEmpty() && avg.isNotEmpty() && qty.isNotEmpty()) {


                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,radio,radio2)
                    sharedPreferencesManager.saveData(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            }else if (check2.isChecked && userData2 == null){

                val text1 = dialogView.findViewById<EditText>(R.id.editText)
                val text2 = dialogView.findViewById<EditText>(R.id.editText2)
                val text3 = dialogView.findViewById<EditText>(R.id.editText3)

                val text4 = dialogView.findViewById<EditText>(R.id.editText34)
                val text5 = dialogView.findViewById<EditText>(R.id.editText4)
                val text6 = dialogView.findViewById<EditText>(R.id.ltpmax)


                var name = text1.text.toString()
                var last = text2.text.toString()
                var qty = text3.text.toString()
                val avg = text4.text.toString()
                val ltpmin = text5.text.toString()
                val ltpmax = text6.text.toString()



                if (name.isNotEmpty() && last.isNotEmpty() && avg.isNotEmpty() && qty.isNotEmpty()) {


                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,radio,radio2)
                    sharedPreferencesManager.saveData2(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            } else if (check3.isChecked && userData3 == null){

                val text1 = dialogView.findViewById<EditText>(R.id.editText)
                val text2 = dialogView.findViewById<EditText>(R.id.editText2)
                val text3 = dialogView.findViewById<EditText>(R.id.editText3)

                val text4 = dialogView.findViewById<EditText>(R.id.editText34)
                val text5 = dialogView.findViewById<EditText>(R.id.editText4)
                val text6 = dialogView.findViewById<EditText>(R.id.ltpmax)


                var name = text1.text.toString()
                var last = text2.text.toString()
                var qty = text3.text.toString()
                val avg = text4.text.toString()
                val ltpmin = text5.text.toString()
                val ltpmax = text6.text.toString()



                if (name.isNotEmpty() && last.isNotEmpty() && avg.isNotEmpty() && qty.isNotEmpty()) {


                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,radio,radio2)
                    sharedPreferencesManager.saveData3(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            }




        }


        sell.setOnClickListener {



            if (check1.isChecked && userData == null ){


                val text1 = dialogView.findViewById<EditText>(R.id.editText)
                val text2 = dialogView.findViewById<EditText>(R.id.editText2)
                val text3 = dialogView.findViewById<EditText>(R.id.editText3)

                val text4 = dialogView.findViewById<EditText>(R.id.editText34)
                val text5 = dialogView.findViewById<EditText>(R.id.editText4)
                val text6 = dialogView.findViewById<EditText>(R.id.ltpmax)


                var name = text1.text.toString()
                var last = text2.text.toString()
                var qty = text3.text.toString()
                val avg = text4.text.toString()
                val ltpmin = text5.text.toString()
                val ltpmax = text6.text.toString()



                if (name.isNotEmpty() && last.isNotEmpty() && avg.isNotEmpty() && qty.isNotEmpty()) {


                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = UserData(qty, "", name, "sell", avg, "","",last,ltpmin,ltpmax,radio,radio2)
                    sharedPreferencesManager.saveData(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            }else if (check2.isChecked && userData2 == null){

                val text1 = dialogView.findViewById<EditText>(R.id.editText)
                val text2 = dialogView.findViewById<EditText>(R.id.editText2)
                val text3 = dialogView.findViewById<EditText>(R.id.editText3)

                val text4 = dialogView.findViewById<EditText>(R.id.editText34)
                val text5 = dialogView.findViewById<EditText>(R.id.editText4)
                val text6 = dialogView.findViewById<EditText>(R.id.ltpmax)


                var name = text1.text.toString()
                var last = text2.text.toString()
                var qty = text3.text.toString()
                val avg = text4.text.toString()
                val ltpmin = text5.text.toString()
                val ltpmax = text6.text.toString()



                if (name.isNotEmpty() && last.isNotEmpty() && avg.isNotEmpty() && qty.isNotEmpty()) {


                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = UserData(qty, "", name, "sell", avg, "","",last,ltpmin,ltpmax,radio,radio2)
                    sharedPreferencesManager.saveData2(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            } else if (check3.isChecked && userData3 == null){

                val text1 = dialogView.findViewById<EditText>(R.id.editText)
                val text2 = dialogView.findViewById<EditText>(R.id.editText2)
                val text3 = dialogView.findViewById<EditText>(R.id.editText3)

                val text4 = dialogView.findViewById<EditText>(R.id.editText34)
                val text5 = dialogView.findViewById<EditText>(R.id.editText4)
                val text6 = dialogView.findViewById<EditText>(R.id.ltpmax)


                var name = text1.text.toString()
                var last = text2.text.toString()
                var qty = text3.text.toString()
                val avg = text4.text.toString()
                val ltpmin = text5.text.toString()
                val ltpmax = text6.text.toString()



                if (name.isNotEmpty() && last.isNotEmpty() && avg.isNotEmpty() && qty.isNotEmpty()) {


                    val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                    val userData = UserData(qty, "", name, "sell", avg, "","",last,ltpmin,ltpmax,radio,radio2)
                    sharedPreferencesManager.saveData3(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            }




        }


        dialog.show()
    }






    @SuppressLint("MissingInflatedId")
 private fun holding() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.holding_dilog_layout, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        var sub = dialogView.findViewById<Button>(R.id.submitButton)







        sub.setOnClickListener {


            val text1 = dialogView.findViewById<EditText>(R.id.newNameEditText)
//            val text2 = dialogView.findViewById<EditText>(R.id.editText2)
            val text3 = dialogView.findViewById<EditText>(R.id.newEmailEditText)

            val text4 = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
            val text5 = dialogView.findViewById<EditText>(R.id.newValidityEditText)

            val profit = dialogView.findViewById<EditText>(R.id.Profit)

            val percprofit = dialogView.findViewById<EditText>(R.id.percentprofit)

            val ltpprofit = dialogView.findViewById<EditText>(R.id.ltpprofit)


            var name = text1.text.toString()
//            var last = text2.text.toString()
            var qty = text3.text.toString()
            val avg = text4.text.toString()
            val ltpmin = text5.text.toString()

            val prof = profit.text.toString()
            val percentpro = percprofit.text.toString()
            val ltppro = ltpprofit.text.toString()


            val newItemRef = hDataRef.push()

            val pushId = newItemRef.key

            if (name.isNotEmpty() &&  qty.isNotEmpty()) {
                val newItem = Item2(name, " last", qty, avg, ltpmin, "ltpmax", "0", "radio.trim()", "buy", pushId, avg,prof,percentpro,ltppro)

                newItemRef.setValue(newItem)

                Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                dialog.dismiss()
            }



        }




        dialog.show()
    }



    fun itemcount(){

        var itemcount = 0

        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()

        if (userData != null){
            itemcount += 1
        }

        if (userData2 != null){
            itemcount += 1
        }

        if (userData3 != null){
            itemcount += 1
        }

        if (itemcount <= 0){
            return
        }

        binding.countcard.visibility = View.VISIBLE
        binding.count.text = "$itemcount"

    }



    private fun getitemcount() {

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing list

                var itemCount = 0
                // Iterate through the dataSnapshot and add items to the list

                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)


                    if (item != null) {

                        itemCount++

                        if (itemCount > 0){


                            binding.countcard.visibility = View.VISIBLE
                            binding.count.text = "$itemCount"


                        }


                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database errors
            }
        })

   databaseReference2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing list

                var itemCount = 0
                // Iterate through the dataSnapshot and add items to the list

                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)


                    if (item != null) {

                        itemCount++

                        if (itemCount > 0){


                            binding.countcard2.visibility = View.VISIBLE
                            binding.count2.text = "$itemCount"


                        }


                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database errors
            }
        })


    }

    override fun onResume() {
        super.onResume()
        itemcount()
        getitemcount()
    }





}