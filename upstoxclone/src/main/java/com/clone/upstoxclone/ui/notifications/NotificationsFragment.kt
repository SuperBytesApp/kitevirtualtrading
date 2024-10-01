package com.clone.upstoxclone.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.clone.upstoxclone.R
import com.clone.upstoxclone.SharedPreferencesManager
import com.clone.upstoxclone.UserData
import com.clone.upstoxclone.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private val fluctuationInterval = 1500L // Adjust the interval in milliseconds
    val values = listOf(0.95,0.5,-0.35,-0.05,0.85,0.25,1.0, 1.15, -1.10,0.5,1.10, -0.05, 0.05 ) // Your list of values
    val minusvalues = listOf(-0.95,-0.5,-0.35,-0.05,-0.85,-0.25,1.0, -1.15, -1.10,0.5,-1.10, -0.05, 0.05) // Your list of values
    var todaypnl : Double = 0.0
    var todaypnl2 : Double = 0.0
    var todaypnl3 : Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        retrivedata(requireContext())
        itemcount()
        EditClose()

        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.upstoxrefback)


        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.upstoxref),
            ContextCompat.getColor(requireContext(), R.color.upstoxref),
            ContextCompat.getColor(requireContext(), R.color.upstoxref)
        )

        binding.swipeRefreshLayout.setOnRefreshListener {

            Handler().postDelayed({

                // Stop the refresh animation
                binding.swipeRefreshLayout.isRefreshing = false
            },500) // 1000 milliseconds = 1 second delay
        }


        return root
    }


    fun retrivedata(context: Context){


        val sharedPreferencesManager = SharedPreferencesManager(context) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()

        // running position delete

        binding.textView6.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData()
        }
        binding.textView66.setOnClickListener {
             val sharedPreferencesManager = SharedPreferencesManager(requireContext())
             sharedPreferencesManager.clearUserData2()
        }
        binding.textView666.setOnClickListener {
             val sharedPreferencesManager = SharedPreferencesManager(requireContext())
             sharedPreferencesManager.clearUserData3()
        }

        // closed position delete

        binding.textView66666.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData()
        }
        binding.textView66qw.setOnClickListener {
             val sharedPreferencesManager = SharedPreferencesManager(requireContext())
             sharedPreferencesManager.clearUserData2()
        }
        binding.textView666zx.setOnClickListener {
             val sharedPreferencesManager = SharedPreferencesManager(requireContext())
             sharedPreferencesManager.clearUserData3()
        }




        if (userData != null) {


            if (userData.type == "buy"){



                val textColor = ContextCompat.getColor(context, R.color.green)
                val background = ContextCompat.getColor(context, R.color.background4)


                binding.portfolioback.setBackgroundColor(background)
                binding.layout1.visibility = View.VISIBLE
                binding.positionhide.visibility = View.GONE



                binding.textView.text = userData.name
                binding.textView24.text = userData.last
                binding.textView26.text = userData.avg
                binding.textView22.text = "+${userData.qty}"
                binding.textView22.setTextColor(textColor)

                binding.textViewsda.text = userData.name
                binding.textView24sda.text = userData.last
                binding.textView22sda.setTextColor(textColor)


                var isFluctuating = true
                var currentValue = userData.avg.toDouble()
                val minValue = "-${userData.ltpmin}"
                val maxValue = userData.ltpmax


                val minv = "-1".toInt().toDouble()
                val maxv = 2.toDouble()

                val handler = Handler(Looper.getMainLooper())





                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }



                        if (userData.bal == "y"){


                            isFluctuating = false




                            val textColor = ContextCompat.getColor(context, R.color.text2)


                            binding.linearLayout3.visibility = View.VISIBLE
                            binding.layout11.visibility = View.VISIBLE
                            binding.layout1.visibility = View.GONE


                            binding.textView26sda.setTextColor(textColor)
                            binding.textView22sda.setTextColor(textColor)
                            binding.textView29sda.setTextColor(textColor)
                            binding.textView24sda.setTextColor(textColor)

                            binding.textView26sda.text = "0.00"
                            binding.textView22sda.text = "0"


                            val fixvalue = userData.ltp.toDouble() - userData.avg.toDouble()

                            val f = fixvalue * userData.qty.toInt()

                            var minus = userData.ltp.toDouble() - userData.avg.toDouble()

                            var devide = minus / userData.avg.toDouble()

                            val ltppercent =  devide * 100.0



                            if (userData.ltp.toDouble() < userData.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red2)
                                binding.textView27sda.setTextColor(textColor)
                                binding.ltpprecentsda.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)



                                if (userData.ltpp != ""){

                                    binding.textView29sda.text = userData.ltp
                                    binding.textView27sda.text = "${fluct}"
                                    binding.ltpprecentsda.text = "(${userData.ltpp}%)"

                                }else{

                                    binding.textView29sda.text = ltp
                                    binding.textView27sda.text = "${fluct}"
                                    binding.ltpprecentsda.text = "(${ltpper}%)"

                                }




                                todaypnl = f

                                if (userData2 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.red2)
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.setTextColor(textColor)

                                    val fin = formatIntWithDecimal(f)

                                    binding.tpl.text = fin
                                    binding.opl.text = fin



                                }



                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)
                                binding.textView27sda.setTextColor(textColor)
                                binding.ltpprecentsda.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                if (userData.ltpp != ""){

                                    binding.textView29sda.text = userData.ltp
                                    binding.textView27sda.text = "+${fluct}"
                                    binding.ltpprecentsda.text = "(+${userData.ltpp}%)"

                                }else{

                                    binding.textView29sda.text = ltp
                                    binding.textView27sda.text = "+${fluct}"
                                    binding.ltpprecentsda.text = "(+${ltpper}%)"
                                }


                                todaypnl = f

                                if (userData2 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.green)
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.setTextColor(textColor)

                                    val fin = formatIntWithDecimal(f)

                                    binding.tpl.text = "+$fin"
                                    binding.opl.text =  "+$fin"

                                }

                            }




                            return

                        }


                        val randomIndex = Random.nextInt(0, values.size)

                        // Get the random value from the list
                        val fluctuation = values[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData.avg.toDouble()

                        val f = fixvalue * userData.qty.toInt()

                        var minus = currentValue - userData.avg.toDouble()

                        var devide = minus / userData.avg.toDouble()

                        val ltppercent =  devide * 100.0


                        binding.layout1.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "buy", userData.avg, "","y",userData.last,"","","")
                            sharedPreferencesManager.saveData(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue < userData.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.red2)
                            binding.textView27.setTextColor(textColor)
                            binding.ltpprecent.setTextColor(textColor)
                            binding.tpl.setTextColor(textColor)
                            binding.opl.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)




                            binding.textView29.text = ltp
                            binding.textView27.text = "${fluct}"
                            binding.ltpprecent.text = "(${ltpper}%)"

                            todaypnl = f*1.0

                            if (userData2 == null){

                                val fin = formatIntWithDecimal(f)

                                binding.tpl.text = fin
                                binding.opl.text = fin

                            }


                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27.setTextColor(textColor)
                            binding.ltpprecent.setTextColor(textColor)
                            binding.tpl.setTextColor(textColor)
                            binding.opl.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)


                            binding.textView29.text = ltp
                            binding.textView27.text = "+${fluct}"
                            binding.ltpprecent.text = "(+${ltpper}%)"

                            todaypnl = f


                            if (userData2 == null){

                                val fin = formatIntWithDecimal(f)

                                binding.tpl.text = "+$fin"
                                binding.opl.text = "+$fin"

                            }


                        }


                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "buy", userData.avg, "","y",userData.last,"","","")
                            sharedPreferencesManager.saveData(userDataa)

                        }


                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)


            }


            if (userData.type == "sell"){


                val textColor = ContextCompat.getColor(context, R.color.red2)
                val background = ContextCompat.getColor(context, R.color.background4)


                binding.portfolioback.setBackgroundColor(background)
                binding.layout1.visibility = View.VISIBLE
                binding.positionhide.visibility = View.GONE



                binding.textView.text = userData.name
                binding.textView24.text = userData.last
                binding.textView26.text = userData.avg
                binding.textView22.text = "-${userData.qty}"
                binding.textView22.setTextColor(textColor)

                binding.textViewsda.text = userData.name
                binding.textView24sda.text = userData.last
                binding.textView22sda.setTextColor(textColor)


                var isFluctuating = true
                var currentValue = userData.avg.toDouble()
                val minValue = "-${userData.ltpmin}"
                val maxValue = userData.ltpmax


                val minv = "-2".toInt().toDouble()
                val maxv = 1.toDouble()

                val handler = Handler(Looper.getMainLooper())





                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }



                        if (userData.bal == "y"){


                            isFluctuating = false




                            val textColor = ContextCompat.getColor(context, R.color.text2)


                            binding.linearLayout3.visibility = View.VISIBLE
                            binding.layout11.visibility = View.VISIBLE
                            binding.layout1.visibility = View.GONE


                            binding.textView26sda.setTextColor(textColor)
                            binding.textView22sda.setTextColor(textColor)
                            binding.textView29sda.setTextColor(textColor)
                            binding.textView24sda.setTextColor(textColor)

                            binding.textView26sda.text = "0.00"
                            binding.textView22sda.text = "0"

                            val fixvalue = userData.ltp.toDouble() - userData.avg.toDouble()

                            val qty = userData.qty.toInt() * -1
                            val f = fixvalue * qty


                            var minus = userData.ltp.toDouble() - userData.avg.toDouble()

                            var devide = minus / userData.avg.toDouble()

                            val ltppercent =  devide * -100.0




                            if (userData.ltp.toDouble() > userData.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red2)
                                binding.textView27sda.setTextColor(textColor)
                                binding.ltpprecentsda.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                if (userData.ltpp != ""){

                                    binding.textView29sda.text = userData.ltp
                                    binding.textView27sda.text = "+${fluct}"
                                    binding.ltpprecentsda.text = "(${userData.ltpp}%)"

                                }else {

                                    binding.textView29sda.text = ltp
                                    binding.textView27sda.text = "${fluct}"
                                    binding.ltpprecentsda.text = "(${ltpper}%)"
                                }
                                todaypnl = f

                                if (userData2 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.red2)
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.setTextColor(textColor)

                                    val fin = formatIntWithDecimal(f)

                                    binding.tpl.text = fin
                                    binding.opl.text = fin

                                }



                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)
                                binding.textView27sda.setTextColor(textColor)
                                binding.ltpprecentsda.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                binding.textView29sda.text = ltp
                                binding.textView27sda.text = "+${fluct}"
                                binding.ltpprecentsda.text = "(+${ltpper}%)"

                                todaypnl = f

                                if (userData2 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.green)
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.setTextColor(textColor)

                                    val fin = formatIntWithDecimal(f)

                                    binding.tpl.text = "+$fin"
                                    binding.opl.text =  "+$fin"

                                }

                            }

                            return

                        }


                        val randomIndex = Random.nextInt(0, minusvalues.size)

                        // Get the random value from the list
                        val fluctuation = minusvalues[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData.avg.toDouble()

                        var qty =  userData.qty.toInt() * -1

                        val f = fixvalue * qty

                        var minus = currentValue - userData.avg.toDouble()

                        var devide = minus / userData.avg.toDouble()

                        val ltppercent =  devide * -100.0


                        binding.layout1.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "sell", userData.avg, "","y",userData.last,"","","")
                            sharedPreferencesManager.saveData(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue > userData.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.red2)
                            binding.textView27.setTextColor(textColor)
                            binding.ltpprecent.setTextColor(textColor)
                            binding.tpl.setTextColor(textColor)
                            binding.opl.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)




                            binding.textView29.text = ltp
                            binding.textView27.text = "${fluct}"
                            binding.ltpprecent.text = "(${ltpper}%)"

                            todaypnl = f*1.0

                            if (userData2 == null){

                                val fin = formatIntWithDecimal(f)

                                binding.tpl.text = fin
                                binding.opl.text = fin

                            }


                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27.setTextColor(textColor)
                            binding.ltpprecent.setTextColor(textColor)
                            binding.tpl.setTextColor(textColor)
                            binding.opl.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)


                            binding.textView29.text = ltp
                            binding.textView27.text = "+${fluct}"
                            binding.ltpprecent.text = "(+${ltpper}%)"

                            todaypnl = f


                            if (userData2 == null){

                                val fin = formatIntWithDecimal(f)

                                binding.tpl.text = "+$fin"
                                binding.opl.text = "+$fin"

                            }


                        }



                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()
                            isFluctuating = false

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "sell", userData.avg, "","y",userData.last,"","","")
                            sharedPreferencesManager.saveData(userDataa)




                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            // Stop fluctuation when max value is reached
                        }






                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)






            }


        }



        if (userData2 != null) {


            if (userData2.type == "buy"){


                val textColor = ContextCompat.getColor(context, R.color.green)
                val background = ContextCompat.getColor(context, R.color.background4)


                binding.portfolioback.setBackgroundColor(background)
                binding.layout2.visibility = View.VISIBLE
                binding.positionhide.visibility = View.GONE

                binding.textVieww.text = userData2.name
                binding.textView244.text = userData2.last
                binding.textView266.text = userData2.avg
                binding.textView222.text = "+${userData2.qty}"
                binding.textView222.setTextColor(textColor)

                binding.textViewwqw.text = userData2.name
                binding.textView244qw.text = userData2.last
                binding.textView222qw.setTextColor(textColor)


                var isFluctuating = true
                var currentValue = userData2.avg.toString().toDouble()
                val minValue = "-${userData2.ltpmin}"
                val maxValue = userData2.ltpmax


                val minv = "-1".toInt().toDouble()
                val maxv = 2.toDouble()

                val handler = Handler(Looper.getMainLooper())


                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }




                        if (userData2.bal == "y"){


                            isFluctuating = false

                            val textColor = ContextCompat.getColor(context, R.color.text2)

                            binding.linearLayout3.visibility = View.VISIBLE
                            binding.layout22.visibility = View.VISIBLE
                            binding.layout2.visibility = View.GONE


                            binding.textView266qw.setTextColor(textColor)
                            binding.textView222qw.setTextColor(textColor)
                            binding.textView299qw.setTextColor(textColor)
                            binding.textView244qw.setTextColor(textColor)

                            binding.textView266qw.text = "0.00"
                            binding.textView222qw.text = "0"

                            val fixvalue = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            val f = fixvalue * userData2.qty.toInt()

                            var minus = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            var devide = minus / userData2.avg.toDouble()

                            val ltppercent =  devide * 100.0




                            if (userData2.ltp.toDouble() < userData2.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red2)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)
                                binding.textView277qw.setTextColor(textColor)
                                binding.ltpprecent2qw.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                if (userData2.ltpp != ""){

                                     binding.textView299qw.text = userData2.ltp
                                     binding.textView277qw.text = "${fluct}"
                                     binding.ltpprecent2qw.text = "(${userData2.ltpp}%)"

                                }else{
                                    binding.textView299qw.text = ltp
                                    binding.textView277qw.text = "${fluct}"
                                    binding.ltpprecent2qw.text = "(${ltpper}%)"
                                }




                                todaypnl2 = f

                                if (userData3 == null){


                                    var minu = todaypnl - todaypnl2
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.tpl.text = fin
                                        binding.opl.text = fin
                                        binding.tpl.setTextColor(textColor)
                                        binding.opl.setTextColor(textColor)

                                    }else{

                                        binding.tpl.text = "+$fin"
                                        binding.opl.text = "+$fin"
                                        binding.tpl.setTextColor(textColor2)
                                        binding.opl.setTextColor(textColor2)

                                    }

                                }




                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)
                                val textColor2 = ContextCompat.getColor(context, R.color.red2)
                                binding.textView277qw.setTextColor(textColor)
                                binding.ltpprecent2qw.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)




                                if (userData2.ltpp != ""){

                                     binding.textView299qw.text = userData2.ltp
                                     binding.textView277qw.text = "+${fluct}"
                                     binding.ltpprecent2qw.text = "(+${userData2.ltpp}%)"

                                }else {

                                    binding.textView299qw.text = ltp
                                    binding.textView277qw.text = "+${fluct}"
                                    binding.ltpprecent2qw.text = "(+${ltpper}%)"

                                }




                                todaypnl2 = f



                                if (userData3 == null){


                                    var minu = todaypnl + todaypnl2
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.tpl.text = fin
                                        binding.tpl.setTextColor(textColor2)
                                        binding.opl.text = fin
                                        binding.opl.setTextColor(textColor2)

                                    }else{

                                        binding.tpl.text = "+$fin"
                                        binding.tpl.setTextColor(textColor)
                                        binding.opl.text = "+$fin"
                                        binding.opl.setTextColor(textColor)

                                    }

                                }






                            }

                            return

                        }



                        binding.layout2.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "buy", userData2.avg, "","y",userData2.last,"","","")
                            sharedPreferencesManager.saveData2(userDataa)

                        }



                        val randomIndex = Random.nextInt(0, values.size)

                        // Get the random value from the list
                        val fluctuation = values[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData2.avg.toDouble()

                        val f = fixvalue * userData2.qty.toInt()

                        var minus = currentValue - userData2.avg.toDouble()

                        var devide = minus / userData2.avg.toDouble()

                        val ltppercent =  devide * 100.0



                        // Update the UI with the new value
                        if (currentValue > userData2.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            val textColor2 = ContextCompat.getColor(context, R.color.red2)

                            binding.textView277.setTextColor(textColor)
                            binding.ltpprecent2.setTextColor(textColor)

                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)

                            binding.textView299.text = ltp
                            binding.textView277.text = "+${fluct}"
                            binding.ltpprecent2.text = "(+${ltpper}%)"

                            todaypnl2 = f




                            if (userData3 == null){


                                var minu = todaypnl + todaypnl2
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.text = fin
                                    binding.opl.setTextColor(textColor2)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.text = "+$fin"
                                    binding.opl.setTextColor(textColor)

                                }

                            }




                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.red2)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)
                            binding.textView277.setTextColor(textColor)
                            binding.ltpprecent2.setTextColor(textColor)


                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)




                            binding.textView299.text = ltp
                            binding.textView277.text = "${fluct}"
                            binding.ltpprecent2.text = "(${ltpper}%)"

                            todaypnl2 = f




                            if (userData3 == null){


                                var minu = todaypnl + todaypnl2
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.text = fin
                                    binding.opl.setTextColor(textColor)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.text = "+$fin"
                                    binding.opl.setTextColor(textColor2)

                                }

                            }




                        }

                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "buy", userData2.avg, "","y",userData2.last,"","","")
                            sharedPreferencesManager.saveData2(userDataa)

                        }


                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)


            }


            if (userData2.type == "sell"){



                val textColor = ContextCompat.getColor(context, R.color.red2)
                val background = ContextCompat.getColor(context, R.color.background4)


                binding.portfolioback.setBackgroundColor(background)
                binding.layout2.visibility = View.VISIBLE
                binding.positionhide.visibility = View.GONE

                binding.textVieww.text = userData2.name
                binding.textView244.text = userData2.last
                binding.textView266.text = userData2.avg
                binding.textView222.text = "-${userData2.qty}"
                binding.textView222.setTextColor(textColor)

                binding.textViewwqw.text = userData2.name
                binding.textView244qw.text = userData2.last
                binding.textView222qw.setTextColor(textColor)


                var isFluctuating = true
                var currentValue = userData2.avg.toString().toDouble()
                val minValue = "-${userData2.ltpmin}"
                val maxValue = userData2.ltpmax


                val minv = "-2".toInt().toDouble()
                val maxv = 1.toDouble()

                val handler = Handler(Looper.getMainLooper())


                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }




                        if (userData2.bal == "y"){


                            isFluctuating = false

                            val textColor = ContextCompat.getColor(context, R.color.text2)

                            binding.linearLayout3.visibility = View.VISIBLE
                            binding.layout22.visibility = View.VISIBLE
                            binding.layout2.visibility = View.GONE


                            binding.textView266qw.setTextColor(textColor)
                            binding.textView222qw.setTextColor(textColor)
                            binding.textView299qw.setTextColor(textColor)
                            binding.textView244qw.setTextColor(textColor)

                            binding.textView266qw.text = "0.00"
                            binding.textView222qw.text = "0"

                            val fixvalue = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            val qty = userData2.qty.toInt() * -1
                            val f = fixvalue * qty

                            var minus = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            var devide = minus / userData2.avg.toDouble()

                            val ltppercent =  devide * -100.0




                            if (userData2.ltp.toDouble() > userData2.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red2)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)
                                binding.textView277qw.setTextColor(textColor)
                                binding.ltpprecent2qw.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)



                                if (userData2.ltpp != ""){

                                     binding.textView299qw.text = userData2.ltp
                                     binding.textView277qw.text = "${fluct}"
                                     binding.ltpprecent2qw.text = "(${userData2.ltpp}%)"

                                }else {

                                    binding.textView299qw.text = ltp
                                    binding.textView277qw.text = "${fluct}"
                                    binding.ltpprecent2qw.text = "(${ltpper}%)"

                                }


                                todaypnl2 = f

                                if (userData3 == null){


                                    var minu = todaypnl - todaypnl2
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.tpl.text = fin
                                        binding.opl.text = fin
                                        binding.tpl.setTextColor(textColor)
                                        binding.opl.setTextColor(textColor)

                                    }else{

                                        binding.tpl.text = "+$fin"
                                        binding.opl.text = "+$fin"
                                        binding.tpl.setTextColor(textColor2)
                                        binding.opl.setTextColor(textColor2)

                                    }

                                }




                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)
                                val textColor2 = ContextCompat.getColor(context, R.color.red2)
                                binding.textView277qw.setTextColor(textColor)
                                binding.ltpprecent2qw.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)




                                if (userData2.ltpp != ""){

                                    binding.textView299qw.text = userData2.ltp
                                    binding.textView277qw.text = "+${fluct}"
                                    binding.ltpprecent2qw.text = "(+${userData2.ltpp}%)"

                                }else {

                                    binding.textView299qw.text = ltp
                                    binding.textView277qw.text = "+${fluct}"
                                    binding.ltpprecent2qw.text = "(+${ltpper}%)"

                                }



                                todaypnl2 = f



                                if (userData3 == null){


                                    var minu = todaypnl + todaypnl2
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.tpl.text = fin
                                        binding.tpl.setTextColor(textColor2)
                                        binding.opl.text = fin
                                        binding.opl.setTextColor(textColor2)

                                    }else{

                                        binding.tpl.text = "+$fin"
                                        binding.tpl.setTextColor(textColor)
                                        binding.opl.text = "+$fin"
                                        binding.opl.setTextColor(textColor)

                                    }

                                }






                            }

                            return

                        }



                        binding.layout2.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "sell", userData2.avg, "","y",userData2.last,"","","")
                            sharedPreferencesManager.saveData2(userDataa)

                        }



                        val randomIndex = Random.nextInt(0, minusvalues.size)

                        // Get the random value from the list
                        val fluctuation = minusvalues[randomIndex]

                        currentValue += fluctuation.toDouble()



                        val fixvalue = currentValue - userData2.avg.toDouble()
                        val qty = userData2.qty.toInt() * -1
                        val f = fixvalue * qty

                        var minus = currentValue - userData2.avg.toDouble()

                        var devide = minus / userData2.avg.toDouble()

                        val ltppercent =  devide * -100.0



                        // Update the UI with the new value
                        if (currentValue < userData2.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            val textColor2 = ContextCompat.getColor(context, R.color.red2)

                            binding.textView277.setTextColor(textColor)
                            binding.ltpprecent2.setTextColor(textColor)

                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)

                            binding.textView299.text = ltp
                            binding.textView277.text = "+${fluct}"
                            binding.ltpprecent2.text = "(+${ltpper}%)"

                            todaypnl2 = f




                            if (userData3 == null){


                                var minu = todaypnl + todaypnl2
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.text = fin
                                    binding.opl.setTextColor(textColor2)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.text = "+$fin"
                                    binding.opl.setTextColor(textColor)

                                }

                            }




                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.red2)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)
                            binding.textView277.setTextColor(textColor)
                            binding.ltpprecent2.setTextColor(textColor)


                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)




                            binding.textView299.text = ltp
                            binding.textView277.text = "${fluct}"
                            binding.ltpprecent2.text = "(${ltpper}%)"

                            todaypnl2 = f




                            if (userData3 == null){


                                var minu = todaypnl + todaypnl2
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.text = fin
                                    binding.opl.setTextColor(textColor)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.text = "+$fin"
                                    binding.opl.setTextColor(textColor2)

                                }

                            }




                        }

                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()
                            isFluctuating = false
                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "sell", userData2.avg, "","y",userData2.last,"","","")
                            sharedPreferencesManager.saveData2(userDataa)

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()

                        }


                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)


            }


        }



        if (userData3 != null) {

            if (userData3.type == "buy"){


                val textColor = ContextCompat.getColor(context, R.color.green)
                val background = ContextCompat.getColor(context, R.color.background4)


                binding.portfolioback.setBackgroundColor(background)
                binding.layout3.visibility = View.VISIBLE
                binding.positionhide.visibility = View.GONE

                binding.textViewww.text = userData3.name
                binding.textView2444.text = userData3.last
                binding.textView2666.text = userData3.avg
                binding.textView2222.text = "+${userData3.qty}"
                binding.textView2222.setTextColor(textColor)

                binding.textViewwwzx.text = userData3.name
                binding.textView2444zx.text = userData3.last
                binding.textView2222zx.setTextColor(textColor)


                var isFluctuating = true
                var currentValue = userData3.avg.toString().toDouble()
                val minValue = "-${userData3.ltpmin}"
                val maxValue = userData3.ltpmax


                val minv = "-1".toInt().toDouble()
                val maxv = 2.toDouble()

                val handler = Handler(Looper.getMainLooper())


                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }




                        if (userData3.bal == "y"){


                            isFluctuating = false

                            val textColor = ContextCompat.getColor(context, R.color.text2)

                            binding.linearLayout3.visibility = View.VISIBLE
                            binding.layout33.visibility = View.VISIBLE
                            binding.layout3.visibility = View.GONE


                            binding.textView2666zx.setTextColor(textColor)
                            binding.textView2222zx.setTextColor(textColor)
                            binding.textView2999zx.setTextColor(textColor)
                            binding.textView2444zx.setTextColor(textColor)

                            binding.textView2666zx.text = "0.00"
                            binding.textView2222zx.text = "0"

                            val fixvalue = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            val f = fixvalue * userData3.qty.toInt()

                            var minus = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            var devide = minus / userData3.avg.toDouble()

                            val ltppercent =  devide * 100.0




                            if (userData3.ltp.toDouble() < userData3.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red2)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)
                                binding.textView2777zx.setTextColor(textColor)
                                binding.ltpprecent3zx.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)





                                if (userData3.ltpp != ""){

                                     binding.textView2999zx.text = userData3.ltp
                                     binding.textView2777zx.text = "${fluct}"
                                     binding.ltpprecent3zx.text  = "(${userData3.ltpp}%)"

                                }else {

                                    binding.textView2999zx.text = ltp
                                    binding.textView2777zx.text = "${fluct}"
                                    binding.ltpprecent3zx.text = "(${ltpper}%)"

                                }



                                todaypnl3 = f*1.0



                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.opl.text = fin
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.setTextColor(textColor)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.opl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.setTextColor(textColor2)

                                }






                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)
                                val textColor2 = ContextCompat.getColor(context, R.color.red2)
                                binding.textView2777zx.setTextColor(textColor)
                                binding.ltpprecent3zx.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)





                                if (userData3.ltpp != ""){

                                         binding.textView2999zx.text = userData3.ltp
                                         binding.textView2777zx.text = "+${fluct}"
                                         binding.ltpprecent3zx.text  = "(+${userData3.ltpp}%)"

                                }else {

                                    binding.textView2999zx.text = ltp
                                    binding.textView2777zx.text = "+${fluct}"
                                    binding.ltpprecent3zx.text = "(+${ltpper}%)"

                                }

                                todaypnl3 = f



                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.text = fin
                                    binding.opl.setTextColor(textColor2)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.text = "+$fin"
                                    binding.opl.setTextColor(textColor)

                                }








                            }

                            return

                        }



                        binding.layout3.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "buy", userData3.avg, "","y",userData3.last,"","","")
                            sharedPreferencesManager.saveData3(userDataa)

                        }



                        val randomIndex = Random.nextInt(0, values.size)

                        // Get the random value from the list
                        val fluctuation = values[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData3.avg.toDouble()

                        val f = fixvalue * userData3.qty.toInt()

                        var minus = currentValue - userData3.avg.toDouble()

                        var devide = minus / userData3.avg.toDouble()

                        val ltppercent =  devide * 100.0



                        // Update the UI with the new value
                        if (currentValue > userData3.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView2777.setTextColor(textColor)
                            binding.ltpprecent3.setTextColor(textColor)

                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)



                            binding.textView2999.text = ltp
                            binding.textView2777.text = "+${fluct}"
                            binding.ltpprecent3.text = "(+${ltpper}%)"

                            todaypnl3 = f

                            var minu = todaypnl + todaypnl2 + todaypnl3

                            if (minu > 0.0){

                                binding.tpl.setTextColor(textColor)
                                binding.opl.setTextColor(textColor)

                            }

                            val fin = formatIntWithDecimal(minu.toDouble())

                            binding.tpl.text = "+$fin"
                            binding.opl.text = "+$fin"



                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.red2)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)
                            binding.textView2777.setTextColor(textColor)
                            binding.ltpprecent3.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)



                            binding.textView2999.text = ltp
                            binding.textView2777.text = "${fluct}"
                            binding.ltpprecent3.text = "(${ltpper}%)"

                            todaypnl3 = f



                            var minu = todaypnl - todaypnl2 - todaypnl3
                            val fin = formatIntWithDecimal(minu.toDouble())


                            if (minu < 0.0){

                                binding.tpl.text = fin
                                binding.tpl.setTextColor(textColor)
                                binding.opl.text = fin
                                binding.opl.setTextColor(textColor)

                            }else{

                                binding.tpl.text = "+$fin"
                                binding.tpl.setTextColor(textColor2)
                                binding.opl.text = "+$fin"
                                binding.opl.setTextColor(textColor2)

                            }





                        }



                        // Generate a random fluctuation between -5 and 5

                        // Update the currentValue with the fluctuation




                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached
                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "buy", userData3.avg, "","y",userData3.last,"","","")
                            sharedPreferencesManager.saveData3(userDataa)


                        }



//                    binding.tpl.text = todaypnl

                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)



            }

            if (userData3.type == "sell"){


                val textColor = ContextCompat.getColor(context, R.color.red2)
                val background = ContextCompat.getColor(context, R.color.background4)


                binding.portfolioback.setBackgroundColor(background)
                binding.layout3.visibility = View.VISIBLE
                binding.positionhide.visibility = View.GONE

                binding.textViewww.text = userData3.name
                binding.textView2444.text = userData3.last
                binding.textView2666.text = userData3.avg
                binding.textView2222.text = "-${userData3.qty}"
                binding.textView2222.setTextColor(textColor)

                binding.textViewwwzx.text = userData3.name
                binding.textView2444zx.text = userData3.last
                binding.textView2222zx.setTextColor(textColor)


                var isFluctuating = true
                var currentValue = userData3.avg.toString().toDouble()
                val minValue = "-${userData3.ltpmin}"
                val maxValue = userData3.ltpmax


                val minv = "-2".toInt().toDouble()
                val maxv = 1.toDouble()

                val handler = Handler(Looper.getMainLooper())


                handler.postDelayed(object : Runnable {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (!isFluctuating) {
                            // Stop fluctuation when max value is reached
                            return
                        }




                        if (userData3.bal == "y"){


                            isFluctuating = false

                            val textColor = ContextCompat.getColor(context, R.color.text2)

                            binding.linearLayout3.visibility = View.VISIBLE
                            binding.layout33.visibility = View.VISIBLE
                            binding.layout3.visibility = View.GONE


                            binding.textView2666zx.setTextColor(textColor)
                            binding.textView2222zx.setTextColor(textColor)
                            binding.textView2999zx.setTextColor(textColor)
                            binding.textView2444zx.setTextColor(textColor)

                            binding.textView2666zx.text = "0.00"
                            binding.textView2222zx.text = "0"

                            val fixvalue = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            var qty =  userData3.qty.toInt() * -1
                            val f = fixvalue * qty

                            var minus = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            var devide = minus / userData3.avg.toDouble()

                            val ltppercent =  devide * -100.0




                            if (userData3.ltp.toDouble() > userData3.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red2)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)
                                binding.textView2777zx.setTextColor(textColor)
                                binding.ltpprecent3zx.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)



                                if (userData3.ltpp != ""){

                                        binding.textView2999zx.text = userData3.ltp
                                        binding.textView2777zx.text = "${fluct}"
                                        binding.ltpprecent3zx.text  = "(${userData3.ltpp}%)"

                                }else {

                                    binding.textView2999zx.text = ltp
                                    binding.textView2777zx.text = "${fluct}"
                                    binding.ltpprecent3zx.text = "(${ltpper}%)"

                                }


                                todaypnl3 = f*1.0



                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.opl.text = fin
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.setTextColor(textColor)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.opl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.setTextColor(textColor2)

                                }






                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)
                                val textColor2 = ContextCompat.getColor(context, R.color.red2)
                                binding.textView2777zx.setTextColor(textColor)
                                binding.ltpprecent3zx.setTextColor(textColor)


                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)




                                if (userData3.ltpp != ""){

                                    binding.textView2999zx.text = userData3.ltp
                                    binding.textView2777zx.text = "+${fluct}"
                                    binding.ltpprecent3zx.text = "(+${userData3.ltpp}%)"

                                }else {

                                    binding.textView2999zx.text = ltp
                                    binding.textView2777zx.text = "+${fluct}"
                                    binding.ltpprecent3zx.text = "(+${ltpper}%)"
                                }


                                todaypnl3 = f



                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.tpl.text = fin
                                    binding.tpl.setTextColor(textColor2)
                                    binding.opl.text = fin
                                    binding.opl.setTextColor(textColor2)

                                }else{

                                    binding.tpl.text = "+$fin"
                                    binding.tpl.setTextColor(textColor)
                                    binding.opl.text = "+$fin"
                                    binding.opl.setTextColor(textColor)

                                }








                            }

                            return

                        }



                        binding.layout3.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "sell", userData3.avg, "","y",userData3.last,"","","")
                            sharedPreferencesManager.saveData3(userDataa)

                        }



                        val randomIndex = Random.nextInt(0, minusvalues.size)

                        // Get the random value from the list
                        val fluctuation = minusvalues[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData3.avg.toDouble()

                        var qty =  userData3.avg.toDouble() * -1

                        val f = fixvalue * qty


                        var minus = currentValue - userData3.avg.toDouble()

                        var devide = minus / userData3.avg.toDouble()

                        val ltppercent =  devide * -100.0



                        // Update the UI with the new value
                        if (currentValue < userData3.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView2777.setTextColor(textColor)
                            binding.ltpprecent3.setTextColor(textColor)

                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)



                            binding.textView2999.text = ltp
                            binding.textView2777.text = "+${fluct}"
                            binding.ltpprecent3.text = "(+${ltpper}%)"

                            todaypnl3 = f

                            var minu = todaypnl + todaypnl2 + todaypnl3

                            if (minu > 0.0){

                                binding.tpl.setTextColor(textColor)
                                binding.opl.setTextColor(textColor)

                            }

                            val fin = formatIntWithDecimal(minu.toDouble())

                            binding.tpl.text = "+$fin"
                            binding.opl.text = "+$fin"



                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.red2)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)
                            binding.textView2777.setTextColor(textColor)
                            binding.ltpprecent3.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)



                            binding.textView2999.text = ltp
                            binding.textView2777.text = "${fluct}"
                            binding.ltpprecent3.text = "(${ltpper}%)"

                            todaypnl3 = f



                            var minu = todaypnl - todaypnl2 - todaypnl3
                            val fin = formatIntWithDecimal(minu.toDouble())


                            if (minu < 0.0){

                                binding.tpl.text = fin
                                binding.tpl.setTextColor(textColor)
                                binding.opl.text = fin
                                binding.opl.setTextColor(textColor)

                            }else{

                                binding.tpl.text = "+$fin"
                                binding.tpl.setTextColor(textColor2)
                                binding.opl.text = "+$fin"
                                binding.opl.setTextColor(textColor2)

                            }





                        }



                        // Generate a random fluctuation between -5 and 5

                        // Update the currentValue with the fluctuation




                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached
                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "sell", userData3.avg, "","y",userData3.last,"","","")
                            sharedPreferencesManager.saveData3(userDataa)


                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()

                        }



//                    binding.tpl.text = todaypnl

                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)




            }

        }





    }



    @SuppressLint("InflateParams")
    fun EditClose(){

        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()

        binding.layout11.setOnClickListener {

            val alertDialog = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(context).inflate(R.layout.position_edit, null)
            val ltp = dialogView.findViewById<EditText>(R.id.newNameEditText)
            val ltpp = dialogView.findViewById<EditText>(R.id.newEmailEditText)

            ltp.setText(userData!!.ltp)
            ltpp.setText(userData.ltpp)

            val qty = userData.qty
            val name = userData.name
            val type = userData.type
            val avg = userData.avg
            val profit = userData.profit
            val bal = userData.bal
            val last = userData.last
            val ltpmin = userData.ltpmin
            val ltpmax = userData.ltpmax


            alertDialog.setView(dialogView).setPositiveButton("Save") { _, _ ->

                val userData = UserData(qty, ltp.text.toString(), name,type, avg,profit,bal,last,ltpmin,ltpmax,ltpp.text.toString())
                sharedPreferencesManager.saveData(userData)

                Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show()



            }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }.show()

        }

        binding.layout22.setOnClickListener {

            val alertDialog = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(context).inflate(R.layout.position_edit, null)
            val ltp = dialogView.findViewById<EditText>(R.id.newNameEditText)
            val ltpp = dialogView.findViewById<EditText>(R.id.newEmailEditText)

            ltp.setText(userData2!!.ltp)
            ltpp.setText(userData2.ltpp)

            val qty = userData2.qty
            val name = userData2.name
            val type = userData2.type
            val avg = userData2.avg
            val profit = userData2.profit
            val bal = userData2.bal
            val last = userData2.last
            val ltpmin = userData2.ltpmin
            val ltpmax = userData2.ltpmax


            alertDialog.setView(dialogView).setPositiveButton("Save") { _, _ ->

                val userData = UserData(qty, ltp.text.toString(), name,type, avg,profit,bal,last,ltpmin,ltpmax,ltpp.text.toString())
                sharedPreferencesManager.saveData2(userData)

                Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show()



            }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }.show()

        }

        binding.layout33.setOnClickListener {

            val alertDialog = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(context).inflate(R.layout.position_edit, null)
            val ltp = dialogView.findViewById<EditText>(R.id.newNameEditText)
            val ltpp = dialogView.findViewById<EditText>(R.id.newEmailEditText)

            ltp.setText(userData3!!.ltp)
            ltpp.setText(userData3.ltpp)

            val qty = userData3.qty
            val name = userData3.name
            val type = userData3.type
            val avg = userData3.avg
            val profit = userData3.profit
            val bal = userData3.bal
            val last = userData3.last
            val ltpmin = userData3.ltpmin
            val ltpmax = userData3.ltpmax


            alertDialog.setView(dialogView).setPositiveButton("Save") { _, _ ->

                val userData = UserData(qty, ltp.text.toString(), name,type, avg,profit,bal,last,ltpmin,ltpmax,ltpp.text.toString())
                sharedPreferencesManager.saveData3(userData)

                Toast.makeText(context, "Successfully Saved", Toast.LENGTH_SHORT).show()



            }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }.show()

        }




    }



    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }


    fun itemcount(){

        var itemcount = 0
        var closecount = 0

        val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()

        if (userData != null){
            itemcount += 1
            if (userData.bal == "y"){
               closecount += 1
            }

        }

        if (userData2 != null){
            itemcount += 1
            if (userData2.bal == "y"){
                closecount += 1
            }
        }

        if (userData3 != null){
            itemcount += 1
            if (userData3.bal == "y"){
                closecount += 1
            }
        }




        if (itemcount <= 0){
            return
        }


        binding.regularcount.text = "    Regular ($itemcount)    "

        binding.textView5.text = "Closed ($closecount)"

    }


}