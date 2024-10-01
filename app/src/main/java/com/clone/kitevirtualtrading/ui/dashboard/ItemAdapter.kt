package com.clone.kitevirtualtrading.ui.dashboard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.clone.kitevirtualtrading.Item
import com.clone.kitevirtualtrading.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.random.Random

class ItemAdapter(private val itemList: List<Item> , private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private val fluctuationInterval = 2000L // Adjust the interval in milliseconds


    interface ItemClickListener {
        fun onItemClick(item: Item, fluctuationText: String , sell : String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]


        if (item.radio == "mis"){

            holder.nrlm.visibility = View.GONE
            holder.mis.visibility = View.VISIBLE


        } else {

            holder.nrlm.visibility = View.VISIBLE
            holder.mis.visibility = View.GONE


        }


        if (item.monthly == "W"){

            holder.weekly.text = "w"


        } else {

            holder.weekly.text = "m"

        }



        holder.delete.setOnClickListener {

            var auth = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            val database = FirebaseDatabase.getInstance().getReference("myData").child(auth)
            database.child(item.id).removeValue()


        }


        val handler = Handler(Looper.getMainLooper())




        // buying


        if (item.type == "buy"){


            holder.name.text = item.text
            holder.last.text = item.last
            holder.qty.text = item.qty
            holder.avg.text = item.avg
            holder.ltp.text = item.ltp






            var isFluctuating = true
            var currentValue = item.ltp.toString().toDouble()
            val minValue = "-${item.ltpmin}"
            val maxValue = item.ltpmax


            val minv = "-1".toInt().toDouble()
            val maxv = 1.3


            holder.fluct.setOnClickListener {

                isFluctuating = false
                val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
                val nrmlbg = ContextCompat.getColor(holder.itemView.context, R.color.nrmlbg)
                val nrml = ContextCompat.getColor(holder.itemView.context, R.color.nrml)
                val misbg = ContextCompat.getColor(holder.itemView.context, R.color.misbg)
                val mis = ContextCompat.getColor(holder.itemView.context, R.color.mis)

                holder.nrlm.setBackgroundColor(nrmlbg)
                holder.nrlmt.setTextColor(nrml)
                holder.mis.setBackgroundColor(misbg)
                holder.mist.setTextColor(mis)

                holder.name.setTextColor(textColor)
                holder.last.setTextColor(textColor)
                holder.th.setTextColor(textColor)
                holder.qty.setTextColor(textColor)
                holder.avg.setTextColor(textColor)
                holder.ltp.setTextColor(textColor)





                val auth = FirebaseAuth.getInstance().currentUser!!.uid.toString()


                val databaseReference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("myData").child(auth)
                val updateData = HashMap<String, Any>()
                updateData["ltp"] = holder.ltp.text.toString()
                updateData["ltpmax"] = "${currentValue.toInt()}"
                updateData["bal"] = "y"

                val itemRef = databaseReference.child(item.id)
                itemRef.updateChildren(updateData)

            }




            CoroutineScope(Dispatchers.IO).launch {
                // Perform heavy computations or I/O-bound operations here
                // ...


                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }



                        if (item.bal == "y"){


                            isFluctuating = false

                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(holder.itemView.context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(holder.itemView.context, R.color.nrml)
                            val misbg = ContextCompat.getColor(holder.itemView.context, R.color.misbg)
                            val mis = ContextCompat.getColor(holder.itemView.context, R.color.mis)

                            holder.nrlm.setBackgroundColor(nrmlbg)
                            holder.nrlmt.setTextColor(nrml)
                            holder.mis.setBackgroundColor(misbg)
                            holder.mist.setTextColor(mis)
                            holder.name.setTextColor(textColor)
                            holder.last.setTextColor(textColor)
                            holder.th.setTextColor(textColor)
                            holder.avg.setTextColor(textColor)
                            holder.ltp.setTextColor(textColor)
                            holder.qty.setTextColor(textColor)

                            holder.name.text = item.text
                            holder.last.text = item.last
                            holder.qty.text = "0"

                            holder.avg.text = "0.00"
                            holder.ltp.text = item.ltp.toDouble().toString()

                            val fixvalue = item.ltp.toDouble() - item.avg.toDouble()

                            val f = fixvalue * item.qty.toInt()

                            if (item.ltp.toDouble() < item.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(holder.itemView.context, R.color.red)
                                holder.fluct.setTextColor(textColor)
                                val formattedValue = formatIntWithDecimal(f)
                                holder.fluct.text = "$formattedValue"

                                itemClickListener.onItemClick(item, formattedValue , "sell")

                            } else {

                                val textColor = ContextCompat.getColor(holder.itemView.context, R.color.green)
                                holder.fluct.setTextColor(textColor)
                                val formattedValue = formatIntWithDecimal(f)
                                holder.fluct.text = "+$formattedValue"

                                itemClickListener.onItemClick(item, holder.fluct.text.toString() , "buy")


                            }

                            return

                        }





                        val fixvalue = currentValue - item.avg.toDouble()

                        val f = fixvalue * holder.qty.text.toString().toInt()



                        // Update the UI with the new value
                        if (currentValue < item.avg.toDouble()) {

                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.red)
                            holder.fluct.setTextColor(textColor)
                            val formattedValue = formatIntWithDecimal(f)
                            holder.fluct.text = "$formattedValue"

                            val ltp2 = formatIntWithDecimal(currentValue)
                            holder.ltp.text = ltp2.toString()

                            itemClickListener.onItemClick(item, holder.fluct.text.toString() , "sell")

                        } else {

                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.green)
                            holder.fluct.setTextColor(textColor)
                            val formattedValue = formatIntWithDecimal(f)
                            holder.fluct.text = "+$formattedValue"
                            val ltp = formatIntWithDecimal(currentValue)

                            holder.ltp.text = ltp.toString()
                            itemClickListener.onItemClick(item, holder.fluct.text.toString() , "buy")


                        }



                        // Generate a random fluctuation between -5 and 5
                        val fluctuation = Random.nextDouble(minv, maxv)

                        // Update the currentValue with the fluctuation




                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached


                            val auth = FirebaseAuth.getInstance().currentUser!!.uid.toString()

                            val databaseReference: DatabaseReference =
                                FirebaseDatabase.getInstance().getReference("myData").child(auth)
                            val updateData = HashMap<String, Any>()
                            updateData["ltp"] = currentValue.toString()
                            updateData["bal"] = "y"

                            val itemRef = databaseReference.child(item.id)
                            itemRef.updateChildren(updateData)

                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(holder.itemView.context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(holder.itemView.context, R.color.nrml)
                            val misbg = ContextCompat.getColor(holder.itemView.context, R.color.misbg)
                            val mis = ContextCompat.getColor(holder.itemView.context, R.color.mis)

                            holder.nrlm.setBackgroundColor(nrmlbg)
                            holder.nrlmt.setTextColor(nrml)
                            holder.mis.setBackgroundColor(misbg)
                            holder.mist.setTextColor(mis)
                            holder.name.setTextColor(textColor)
                            holder.last.setTextColor(textColor)
                            holder.th.setTextColor(textColor)
                            holder.qty.setTextColor(textColor)
                            holder.avg.setTextColor(textColor)



                        }




                        currentValue += fluctuation


                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)



                // Update the UI on the main thread
                launch(Dispatchers.Main) {
                    // Update UI elements here
                    // This code will run on the main thread
                    // ...

//                    handler.postDelayed(object : Runnable {
//                        @SuppressLint("NotifyDataSetChanged")
//                        override fun run() {
//
//                    val updateData = HashMap<String, Any>()
//                    updateData["ltp"] = holder.ltp.text.toString()
//
//                    val auth = FirebaseAuth.getInstance().currentUser!!.uid
//                    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("myData").child(auth).child(item.id)
//                    databaseReference.updateChildren(updateData)
//
//                    // Continue fluctuation
//                    handler.postDelayed(this, 2000L)
//                }
//            }, 2000L)


                }
            }







        } else if (item.type == "sell") {

            // sell


            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.red)


            holder.name.text = item.text
            holder.last.text = item.last
            holder.qty.text = "-"+item.qty
            holder.qty.setTextColor(textColor)
            holder.avg.text = item.avg
            holder.ltp.text = item.avg.toDouble().toString()




            var isFluctuating = true
            var currentValue = item.ltp.toString().toDouble()
            val minValue = item.ltpmin
            val maxValue = item.ltpmax

            val minv = "-2".toInt().toDouble()
            val maxv = 1.5.toDouble()


            holder.fluct.setOnClickListener {

                isFluctuating = false
                val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
                val nrmlbg = ContextCompat.getColor(holder.itemView.context, R.color.nrmlbg)
                val nrml = ContextCompat.getColor(holder.itemView.context, R.color.nrml)
                val misbg = ContextCompat.getColor(holder.itemView.context, R.color.misbg)
                val mis = ContextCompat.getColor(holder.itemView.context, R.color.mis)

                holder.nrlm.setBackgroundColor(nrmlbg)
                holder.nrlmt.setTextColor(nrml)
                holder.mis.setBackgroundColor(misbg)
                holder.mist.setTextColor(mis)
                holder.name.setTextColor(textColor)
                holder.last.setTextColor(textColor)
                holder.th.setTextColor(textColor)
                holder.qty.setTextColor(textColor)
                holder.avg.setTextColor(textColor)
                holder.ltp.setTextColor(textColor)


                val auth = FirebaseAuth.getInstance().currentUser!!.uid.toString()


                val databaseReference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("myData").child(auth)
                val updateData = HashMap<String, Any>()
                updateData["ltp"] = holder.ltp.text.toString()
                updateData["ltpmin"] = "${currentValue.toInt()}"
                updateData["bal"] = "y"

                val itemRef = databaseReference.child(item.id)
                itemRef.updateChildren(updateData)

            }



            CoroutineScope(Dispatchers.IO).launch {
                // Perform heavy computations or I/O-bound operations here
                // ...

                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }


                        if (item.bal == "y"){


                            isFluctuating = false

                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(holder.itemView.context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(holder.itemView.context, R.color.nrml)
                            val misbg = ContextCompat.getColor(holder.itemView.context, R.color.misbg)
                            val mis = ContextCompat.getColor(holder.itemView.context, R.color.mis)

                            holder.nrlm.setBackgroundColor(nrmlbg)
                            holder.nrlmt.setTextColor(nrml)
                            holder.mis.setBackgroundColor(misbg)
                            holder.mist.setTextColor(mis)
                            holder.name.setTextColor(textColor)
                            holder.last.setTextColor(textColor)
                            holder.th.setTextColor(textColor)
                            holder.avg.setTextColor(textColor)
                            holder.ltp.setTextColor(textColor)
                            holder.qty.setTextColor(textColor)

                            holder.name.text = item.text
                            holder.last.text = item.last
                            holder.qty.text = "0"

                            holder.avg.text = "0.00"
                            holder.ltp.text = item.ltp.toDouble().toString()

                            val fixvalue =   item.avg.toDouble() - item.ltp.toDouble()

                            val f = fixvalue * item.qty.toInt().toDouble()

                            if (item.ltp.toDouble() > item.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(holder.itemView.context, R.color.red)
                                holder.fluct.setTextColor(textColor)
                                val formattedValue = formatIntWithDecimal(f.toDouble())
                                holder.fluct.text = "$formattedValue"
                                itemClickListener.onItemClick(item, holder.fluct.text.toString() , "sell")

                            }else{


                                val textColor = ContextCompat.getColor(holder.itemView.context, R.color.green)
                                holder.fluct.setTextColor(textColor)
                                val formattedValue = formatIntWithDecimal(f.toDouble())
                                holder.fluct.text = "+$formattedValue"

                                itemClickListener.onItemClick(item, holder.fluct.text.toString() , "buy")

                            }


                            return

                        }





                        val fixvalue = currentValue - item.avg.toDouble()

                        val f = fixvalue * holder.qty.text.toString().toInt()

                        // Update the UI with the new value
                        if (currentValue > item.avg.toDouble()) {



                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.red)
                            holder.fluct.setTextColor(textColor)
                            val formattedValue = formatIntWithDecimal(f)
                            holder.fluct.text = "$formattedValue"

                            val ltp2 = formatIntWithDecimal(currentValue)
                            holder.ltp.text = ltp2.toString()

                            itemClickListener.onItemClick(item, holder.fluct.text.toString() , "sell")

                        } else {



                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.green)
                            holder.fluct.setTextColor(textColor)
                            val formattedValue = formatIntWithDecimal(f)
                            holder.fluct.text = "+$formattedValue"
                            val ltp = formatIntWithDecimal(currentValue)

                            holder.ltp.text = ltp.toString()

                            itemClickListener.onItemClick(item, holder.fluct.text.toString() , "buy")

                        }




                        // Generate a random fluctuation between -5 and 5
                        val fluctuation = Random.nextDouble(minv, maxv)

                        // Update the currentValue with the fluctuation


                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                            isFluctuating = false

                            val textColor = ContextCompat.getColor(holder.itemView.context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(holder.itemView.context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(holder.itemView.context, R.color.nrml)
                            val misbg = ContextCompat.getColor(holder.itemView.context, R.color.misbg)
                            val mis = ContextCompat.getColor(holder.itemView.context, R.color.mis)

                            holder.nrlm.setBackgroundColor(nrmlbg)
                            holder.nrlmt.setTextColor(nrml)
                            holder.mis.setBackgroundColor(misbg)
                            holder.mist.setTextColor(mis)
                            holder.name.setTextColor(textColor)
                            holder.last.setTextColor(textColor)
                            holder.th.setTextColor(textColor)
                            holder.qty.setTextColor(textColor)
                            holder.avg.setTextColor(textColor)

                            val auth = FirebaseAuth.getInstance().currentUser!!.uid.toString()

                            val databaseReference: DatabaseReference =
                                FirebaseDatabase.getInstance().getReference("myData").child(auth)
                            val updateData = HashMap<String, Any>()
                            updateData["ltp"] = currentValue.toString()
                            updateData["bal"] = "y"

                            val itemRef = databaseReference.child(item.id)
                            itemRef.updateChildren(updateData)



                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            // Stop fluctuation when max value is reached
                        }




                        currentValue += fluctuation


                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)


                launch(Dispatchers.Main) {
                }

            }




        }


    }

//    private fun saveLTPToSharedPreferences(id: String, ltpValue: String) {
//        val editor = sharedPreferences.edit()
//        editor.putString(id, ltpValue)
//        editor.apply()
//    }
//
//    fun getLTPFromSharedPreferences(id: String): String? {
//        return sharedPreferences.getString(id, null)
//    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textView)
        val last: TextView = itemView.findViewById(R.id.textView25)
        val qty: TextView = itemView.findViewById(R.id.textView22)
        val avg: TextView = itemView.findViewById(R.id.textView26)
        val ltp: TextView = itemView.findViewById(R.id.textView29)
        val nrlm : LinearLayout = itemView.findViewById(R.id.nrml)
        val mis : LinearLayout = itemView.findViewById(R.id.mis)
        val nrlmt : TextView = itemView.findViewById(R.id.nrmltext)
        val mist : TextView = itemView.findViewById(R.id.mistext)
        val th : TextView = itemView.findViewById(R.id.textView20)
        val weekly : TextView = itemView.findViewById(R.id.count)

        val delete : TextView = itemView.findViewById(R.id.textView24)

        val fluct: TextView = itemView.findViewById(R.id.textView27)
    }


    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }

//
//    fun data(id : String , c : String) {
//
//
//// Initialize Firebase Authentication
//        val auth = FirebaseAuth.getInstance()
//
//// Get the current user
//        val currentUser = auth.currentUser
//
//// Check if the user is authenticated
//        if (currentUser != null) {
//            val uid = currentUser.uid
//
//            // Reference to the database location where you want to update the field
//            val databaseReference: DatabaseReference =
//                FirebaseDatabase.getInstance().getReference("myData").child(uid)
//
//            // Define the new value you want to set for the field
//            val newValue = c
//
//            // Update the specific field value in the database
//            val fieldToUpdate = "ltp" // Replace with the actual field name you want to update
//            val itemKey = id.toString() // Replace with the key of the item you want to update
//
//            // Construct a map to update the specific field
//            val updateData = HashMap<String, Any>()
//            updateData[fieldToUpdate] = newValue
//
//            // Update the field in the specified location
//            val itemRef = databaseReference.child(itemKey)
//            itemRef.updateChildren(updateData)
//
//        }
//    }
//

}
