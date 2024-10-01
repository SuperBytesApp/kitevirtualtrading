package com.clone.kitevirtualtrading

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.clone.kitevirtualtrading.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction


class UserFragment : Fragment() {

    lateinit var binding : FragmentUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false)
        val root: View = binding.root
        databaseReference = FirebaseDatabase.getInstance().reference


        auth = FirebaseAuth.getInstance()
        binding.fund.setOnClickListener {

            val intent = Intent (activity, Fund_Activity::class.java)
            requireActivity().startActivity(intent)

        }

        binding.logout.setOnClickListener {

//
//            auth.signOut()
//

            val user = auth.currentUser
            user?.let { currentUser ->
                val uid = currentUser.uid
                val userRef = databaseReference.child("users").child(uid)

                userRef.child("isOnline").setValue("")


                Toast.makeText(requireContext(), "LogOut", Toast.LENGTH_SHORT).show()

                var intent = Intent(context,LoginActivity::class.java)
                requireActivity().startActivity(intent)
                requireActivity().finish()


            }

        }


        return root
    }

}