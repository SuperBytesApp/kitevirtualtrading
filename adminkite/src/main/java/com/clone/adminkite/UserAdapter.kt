package com.clone.adminkite

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class UserAdapter(private val userList: List<User> ,var activity: Int ) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
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
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val passwordTextView: TextView = itemView.findViewById(R.id.passwordTextView)
        private val validityTextView: TextView = itemView.findViewById(R.id.validityTextView)
        private val editButton: ConstraintLayout = itemView.findViewById(R.id.itemid)

        @SuppressLint("MissingInflatedId")
        fun bind(user: User) {
            nameTextView.text = user.name
            emailTextView.text = user.email
            passwordTextView.text = user.password
            validityTextView.text = user.validity

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
                val kite = dialogView.findViewById<LinearLayout>(R.id.editkite)
                val upstox = dialogView.findViewById<LinearLayout>(R.id.editup)
                val angel = dialogView.findViewById<LinearLayout>(R.id.editang)

                // Pre-fill the edit text fields with the current user data
                newNameEditText.setText(user.name)
                newEmailEditText.setText(user.email)
                newPasswordEditText.setText(user.password)
                newValidityEditText.setText(user.validity)


                if (activity == 1){
                    kite.visibility = View.VISIBLE
                }else if (activity == 2){
                    upstox.visibility = View.VISIBLE
                }else if (activity == 3){
                    angel.visibility = View.VISIBLE
                }else{
                    return@setOnClickListener
                }

                alertDialog.setView(dialogView)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = newNameEditText.text.toString()
                        val newEmail = newEmailEditText.text.toString()
                        val newPassword = newPasswordEditText.text.toString()
                        val newValidity = newValidityEditText.text.toString()

                        // Update the user data in Firestore
                        updateUserInFirestore(user, newName, newEmail, newPassword, newValidity,activity)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    } .setNeutralButton("Reset Login") { dialog, _ ->

                        resetpassword(user,activity)

                        dialog.cancel()
                    }
                    .show()
            }
        }

        private fun updateUserInFirestore(user: User, newName: String, newEmail: String, newPassword: String, newValidity: String,id:Int) {


            if (id == 1){

                firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("user").document(user.id)

                val updatedUser = User( newName, newEmail, newPassword, newValidity,user.id)

                userRef.set(updatedUser)
                    .addOnSuccessListener {
                        // Successfully updated user data
                        Toast.makeText(itemView.context, "User data updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors that occur during the update
                        Toast.makeText(itemView.context, "Error updating user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }



            }

            if (id == 2){


                firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("upstoxusers").document(user.id)

                val updatedUser = User( newName, newEmail, newPassword, newValidity,user.id)

                userRef.set(updatedUser)
                    .addOnSuccessListener {
                        // Successfully updated user data

                        Toast.makeText(itemView.context, "User data updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors that occur during the update
                        Toast.makeText(itemView.context, "Error updating user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }




            }

            if (id == 3){


                firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("angelusers").document(user.id)

                val updatedUser = User( newName, newEmail, newPassword, newValidity,user.id)

                userRef.set(updatedUser)
                    .addOnSuccessListener {
                        // Successfully updated user data

                        Toast.makeText(itemView.context, "User data updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors that occur during the update
                        Toast.makeText(itemView.context, "Error updating user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }




            }


            if(id <= 0 || id > 3){

                Toast.makeText(itemView.context, "Data Not Update/nPlease Restart Application", Toast.LENGTH_SHORT).show()

            }





        }

        private fun resetpassword(user: User,id:Int) {


            if (id == 1){


                val database = FirebaseDatabase.getInstance()

                val usersRef: DatabaseReference = database.getReference("users")

                usersRef.child(user.id).child("isOnline").setValue("")


                Toast.makeText(itemView.context, "Success", Toast.LENGTH_SHORT).show()

            }

            if (id == 2){


                val database = FirebaseDatabase.getInstance()

                val usersRef: DatabaseReference = database.getReference("upstoxusers")

                usersRef.child(user.id).child("isOnline").setValue("")



                Toast.makeText(itemView.context, "Success", Toast.LENGTH_SHORT).show()


            }

            if (id == 3){


                val database = FirebaseDatabase.getInstance()

                val usersRef: DatabaseReference = database.getReference("angelusers")

                usersRef.child(user.id).child("isOnline").setValue("")


                Toast.makeText(itemView.context, "Success", Toast.LENGTH_SHORT).show()


            }


            if(id <= 0 || id == 4){

                Toast.makeText(itemView.context, "Data Not Update\nPlease Restart Application", Toast.LENGTH_SHORT).show()

            }





        }



    }
}
