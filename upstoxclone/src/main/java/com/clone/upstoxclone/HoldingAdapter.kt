package com.clone.upstoxclone

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.clone.upstoxclone.ui.frag.HoldingFragment
import com.clone.upstoxclone.ui.frag.Item2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class HoldingAdapter(private val userList: List<Item2>, private val itemClickListener: HoldingFragment) :
    RecyclerView.Adapter<HoldingAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.holding_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textView)
        private val qty: TextView = itemView.findViewById(R.id.textView22)
        private val avg: TextView = itemView.findViewById(R.id.textView26)
        private val ltp: TextView = itemView.findViewById(R.id.textView29)
        private val invested : TextView = itemView.findViewById(R.id.invested)
        private val profit : TextView = itemView.findViewById(R.id.textView27)
        private val percprofit : TextView = itemView.findViewById(R.id.totalp)
        private val ltpprofit : TextView = itemView.findViewById(R.id.boostp)
        private val editButton: ConstraintLayout = itemView.findViewById(R.id.itemid)

        @SuppressLint("MissingInflatedId")
        fun bind(user: Item2) {

            if (user.radio == "loss"){

                val textColor = ContextCompat.getColor(itemView.context, R.color.red2)
                profit.setTextColor(textColor)
                percprofit.setTextColor(textColor)
                ltpprofit.setTextColor(textColor)

            }else{

                val textColor = ContextCompat.getColor(itemView.context, R.color.green)
                profit.setTextColor(textColor)
                percprofit.setTextColor(textColor)
                ltpprofit.setTextColor(textColor)

            }



            nameTextView.text = user.text
            qty.text = user.qty
            avg.text = user.avg
            ltp.text = user.ltpmin

            profit.text = user.profit
            percprofit.text = user.percentprofit
            ltpprofit.text = user.ltpprofit



            var invest = user.avg.toDouble() * user.qty.toDouble()

            var inves = formatIntWithDecimal(invest)
            invested.text = inves




            // Handle item click to edit user data
            editButton.setOnClickListener {
                // Implement your logic to edit user data here
                val context = itemView.context
                val alertDialog = AlertDialog.Builder(context)
                val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_user_dialog, null)
                val newNameEditText = dialogView.findViewById<EditText>(R.id.newNameEditText)
                val newEmailEditText = dialogView.findViewById<EditText>(R.id.newEmailEditText)
                val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
                val newValidityEditText = dialogView.findViewById<EditText>(R.id.newValidityEditText)
                val profit : EditText = dialogView.findViewById(R.id.Profit)
                val percprofit : EditText = dialogView.findViewById(R.id.percentprofit)
                val ltpprofit : EditText = dialogView.findViewById(R.id.ltpprofit)


                val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup2)

                var radio = ""
                radioGroup.setOnCheckedChangeListener { group, checkedId ->
                    // Get the selected radio button by its ID
                    val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)

                    // Check which radio button was clicked and perform an action
                    when (selectedRadioButton.id) {
                        R.id.radioButton2A -> {
                            // Radio Button 1 is clicked
                            // Perform your action here
                            radio = "profit"

                        }
                        R.id.radioButton2B -> {
                            // Radio Button 2 is clicked
                            // Perform your action here
                            radio = "loss"

                        }
                    }
                }






                // Pre-fill the edit text fields with the current user data
                newNameEditText.setText(user.text)
                newEmailEditText.setText(user.qty)
                newPasswordEditText.setText(user.avg)
                newValidityEditText.setText(user.ltpmin)
                profit.setText(user.profit)
                percprofit.setText(user.percentprofit)
                ltpprofit.setText(user.ltpprofit)









                alertDialog.setView(dialogView).setPositiveButton("Save") { _, _ ->

                    val newName = newNameEditText.text.toString()
                    val newEmail = newEmailEditText.text.toString()
                    val newPassword = newPasswordEditText.text.toString()
                    val newValidity = newValidityEditText.text.toString()
                    val newpro = profit.text.toString()
                    val newperprofit = percprofit.text.toString()
                    val newltppro = ltpprofit.text.toString()


                    // Update the user data in Firestore
                    updateUserInRealtimeDatabase(user.id, newName, newEmail, newPassword, newValidity,newpro,newperprofit,newltppro,radio)

                }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }.setNeutralButton("Delete"){dialog,_ ->


                        var auth = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                        val database = FirebaseDatabase.getInstance().getReference("holdingData").child(auth)
                        database.child(user.id).removeValue()

                        dialog.cancel()
                    }
                    .show()
            }





        }

        private fun updateUserInRealtimeDatabase(userId: String, newName: String, newEmail: String, newPassword: String, newValidity: String,profit : String,percprofit: String,ltpprofit:String,radio:String) {

            var Auth= FirebaseAuth.getInstance().currentUser!!.uid

            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
            val userRef = databaseReference.child("holdingData").child(Auth).child(userId)

            // Create a map to represent the updated user data
            val updatedUserData = hashMapOf<String, Any>(
                "text" to newName,
                "qty" to newEmail,
                "avg" to newPassword,
                "ltpmin" to newValidity,
                "profit" to profit,
                "percentprofit" to percprofit,
                "ltpprofit" to ltpprofit,
                "radio" to radio
            )

            userRef.updateChildren(updatedUserData)
                .addOnSuccessListener {
                    // Successfully updated user data

                }
                .addOnFailureListener { e ->
                    // Handle any errors that occur during the update

                }
        }



    }

    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }


}
