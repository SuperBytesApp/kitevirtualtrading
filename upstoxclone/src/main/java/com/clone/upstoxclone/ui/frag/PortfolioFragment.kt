package com.clone.upstoxclone.ui.frag

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
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.clone.upstoxclone.R
import com.clone.upstoxclone.SharedPreferencesManager
import com.clone.upstoxclone.UserData
import com.clone.upstoxclone.databinding.FragmentPortfolioBinding
import com.clone.upstoxclone.ui.notifications.NotificationsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PortfolioFragment : Fragment() {


    lateinit var binding : FragmentPortfolioBinding
    lateinit var hDataRef: DatabaseReference
    lateinit var database: FirebaseDatabase
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
        hDataRef = database.getReference("holdingData").child(uid.toString())
        databaseReference2 = FirebaseDatabase.getInstance().getReference("holdingData").child(uid.toString())

        binding.prot.setOnClickListener {

            showAddItemDialog()

        }

        itemcount()

        loadfrag()


        binding.textView3.setOnClickListener {

       loadfrag()

        }

        binding.holding.setOnClickListener {

            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            val targetFragment = HoldingFragment()

            // Replace the current fragment with the target fragment
            transaction.replace(R.id.portfoliofrag, targetFragment)
            transaction.addToBackStack(null) // Add to back stack to handle back navigation
            transaction.commit()

            val newColor = ContextCompat.getColor(requireContext(), R.color.white)
            val newColor2 = ContextCompat.getColor(requireContext(), R.color.zerodha3)
            binding.holding.setTextColor(newColor)
            binding.textView5.setBackgroundColor(newColor)
            binding.textView5.visibility = View.VISIBLE

            binding.textView3.setTextColor(newColor2)
            binding.textView4.visibility = View.GONE


        }



        binding.holding2.setOnClickListener {

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

        val newColor = ContextCompat.getColor(requireContext(), R.color.white)
        val newColor2 = ContextCompat.getColor(requireContext(), R.color.zerodha3)
        binding.textView3.setTextColor(newColor)
        binding.textView4.setBackgroundColor(newColor)
        binding.textView4.visibility = View.VISIBLE

        binding.holding.setTextColor(newColor2)
        binding.textView5.visibility = View.GONE
//        binding.countcard.setCardBackgroundColor(newColor)
//        binding.countcard2.setCardBackgroundColor(newColor3)


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



//
//
//        var radio = ""
//        var radio2 = ""
//        radioGroup.setOnCheckedChangeListener { group, checkedId ->
//            // Get the selected radio button by its ID
//            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)
//
//            // Check which radio button was clicked and perform an action
//            when (selectedRadioButton.id) {
//                R.id.radioButton2A -> {
//                    // Radio Button 1 is clicked
//                    // Perform your action here
//                    radio = "nrml"
//
//                }
//                R.id.radioButton2B -> {
//                    // Radio Button 2 is clicked
//                    // Perform your action here
//                    radio = "mis"
//
//                }
//            }
//        }
//
//        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
//            // Get the selected radio button by its ID
//            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)
//
//            // Check which radio button was clicked and perform an action
//            when (selectedRadioButton.id) {
//                R.id.radioButton1A -> {
//                    // Radio Button 1 is clicked
//                    // Perform your action here
//                    radio2 = "W"
//
//                }
//                R.id.radioButton1B -> {
//                    // Radio Button 2 is clicked
//                    // Perform your action here
//                    radio2 = "M"
//
//                }
//            }
//        }
//
//
//
//






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
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,"")
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
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,"")
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
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,"")
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
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,"")
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
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,"")
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
                    val userData = UserData(qty, "", name, "buy", avg, "","",last,ltpmin,ltpmax,"")
                    sharedPreferencesManager.saveData3(userData)

                    Toast.makeText(context, "Successfully Add Position", Toast.LENGTH_SHORT).show()


                    dialog.dismiss()


                }



            }




        }


        dialog.show()
    }



    override fun onResume() {
        super.onResume()

        reload()


        loadrecycler()


    }


    fun loadrecycler(){




        databaseReference2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing list
                var itemcount = 0

                // Iterate through the dataSnapshot and add items to the list
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item2::class.java)
                    itemcount++
                    if (item != null) {

                        binding.holding.text = "Holdings ($itemcount)"


                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database errors
            }
        })




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

        binding.textView3.text = " Positions ($itemcount)"

    }


    fun reload(){

        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()




        if (userData != null){


            binding.imageView6.visibility = View.VISIBLE
            binding.sensibull.visibility = View.VISIBLE


            if (userData.bal == "y"){
                binding.imageView6.visibility = View.GONE
                binding.sensibull.visibility = View.GONE
            }


        }

        if (userData2 != null){

            binding.imageView6.visibility = View.VISIBLE
            binding.sensibull.visibility = View.VISIBLE


            if (userData2.bal == "y"){
                binding.imageView6.visibility = View.GONE
                binding.sensibull.visibility = View.GONE
            }



        }

        if (userData3 != null){


            binding.imageView6.visibility = View.VISIBLE
            binding.sensibull.visibility = View.VISIBLE



            if (userData3.bal == "y"){
                binding.imageView6.visibility = View.GONE
                binding.sensibull.visibility = View.GONE
            }



        }



    }


}