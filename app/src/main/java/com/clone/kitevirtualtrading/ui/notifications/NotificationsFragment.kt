package com.clone.kitevirtualtrading.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.clone.kitevirtualtrading.R
import com.clone.kitevirtualtrading.SharedPreferencesManager
import com.clone.kitevirtualtrading.UserData
import com.clone.kitevirtualtrading.databinding.FragmentNotificationsBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random

class NotificationsFragment : Fragment() {

    lateinit var binding: FragmentNotificationsBinding

    private val fluctuationInterval = 1500L // Adjust the interval in milliseconds
    val values = listOf(0.95,0.5,-0.35,-0.05,0.85,0.25,1.0, 1.15, -1.10,0.5,1.10, -0.05) // Your list of values
    val minusvalues = listOf(-0.95,-0.5,-0.35,-0.05,-0.85,-0.25,1.0, -1.15, -1.10,0.5,-1.10, -0.05) // Your list of values
    var todaypnl : Double = 0.0
    var todaypnl2 : Double = 0.0
    var todaypnl3 : Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        retrivedata(requireContext())


        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.zerodha),
            ContextCompat.getColor(requireContext(), R.color.zerodha),
            ContextCompat.getColor(requireContext(), R.color.zerodha))

        binding.swipeRefreshLayout.setOnRefreshListener {

            Handler().postDelayed({


                binding.swipeRefreshLayout.isRefreshing = false

            }, 500)


        }



        return root
    }


    fun retrivedata(context: Context){


        val sharedPreferencesManager = SharedPreferencesManager(context) // 'this' should be the context of your activity or fragment
        val userData = sharedPreferencesManager.getData()
        val userData2 = sharedPreferencesManager.getData2()
        val userData3 = sharedPreferencesManager.getData3()

        // running position delete

        binding.textView24.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData()
        }
        binding.textView24second.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData2()
        }
        binding.textView24Third.setOnClickListener {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())
            sharedPreferencesManager.clearUserData3()
        }




        if (userData != null) {


            if (userData.type == "buy"){



                binding.layout1.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
                binding.cardView2.visibility = View.VISIBLE
                binding.demo.visibility = View.GONE



                if (userData.nrml == "nrml"){ binding.nrml.visibility = View.VISIBLE }else{ binding.mis.visibility = View.VISIBLE }

                if (userData.monthly == "W"){ binding.count.text = "W" }else{ binding.countcard.visibility = View.GONE }



                binding.textView.text = userData.name
                binding.textView25.text = userData.last
                binding.textView26.text = userData.avg
                binding.textView22.text = userData.qty



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


                            if (userData.nrml == "nrml"){ binding.nrml.visibility = View.VISIBLE }else{ binding.mis.visibility = View.VISIBLE }

                            if (userData.monthly == "W"){ binding.count.text = "W" }else{ binding.countcard.visibility = View.GONE }




                            val textColor = ContextCompat.getColor(context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(context, R.color.nrml)
                            val misbg = ContextCompat.getColor(context, R.color.misbg)
                            val mis = ContextCompat.getColor(context, R.color.mis)

                            binding.nrml.setBackgroundColor(nrmlbg)
                            binding.nrmltext.setTextColor(nrml)
                            binding.mis.setBackgroundColor(misbg)
                            binding.mistext.setTextColor(mis)
                            binding.textView.setTextColor(textColor)
                            binding.textView25.setTextColor(textColor)
                            binding.textView20.setTextColor(textColor)
                            binding.textView26.setTextColor(textColor)
                            binding.textView29.setTextColor(textColor)
                            binding.textView22.setTextColor(textColor)

                            binding.textView.text = userData.name
                            binding.textView25.text = userData.last
                            binding.textView22.text = "0"

                            binding.textView26.text = "0.00"
                            binding.textView29.text = userData.ltp.toDouble().toString()

                            val fixvalue = userData.ltp.toDouble() - userData.avg.toDouble()

                            val f = fixvalue * userData.qty.toInt()

                            var minus = userData.ltp.toDouble() - userData.avg.toDouble()

                            var devide = minus / userData.avg.toDouble()

                            val ltppercent =  devide * 100.0




                            if (userData.ltp.toDouble() < userData.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red)


                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)

                                binding.textView29.text = ltp
                                binding.textView27.text = "${fluct}"

                                todaypnl = f



                                if (userData2 == null){

                                    val textColor = ContextCompat.getColor(context, R.color.red)
                                    binding.numberTextView.setTextColor(textColor)
                                    val fin = formatIntWithDecimal(f)

                                    binding.numberTextView.text = fin

                                }



                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)


                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                binding.textView29.text = ltp
                                binding.textView27.text = "+${fluct}"

                                todaypnl = f

                                if (userData2 == null){

                                    val textColor = ContextCompat.getColor(context, R.color.green)
                                    binding.numberTextView.setTextColor(textColor)
                                    val fin = formatIntWithDecimal(f)
                                    binding.numberTextView.text = "+$fin"

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
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "buy", userData.avg, "","y",userData.last,"","",userData.nrml,userData.monthly)
                            sharedPreferencesManager.saveData(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue < userData.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context,  R.color.red)
                            binding.textView27.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)





                            binding.textView29.text = ltp
                            binding.textView27.text = "${fluct}"

                            todaypnl = f*1.0

                            if (userData2 == null || userData3 == null){


                                val textColor = ContextCompat.getColor(context, R.color.red)
                                binding.numberTextView.setTextColor(textColor)
                                val fin = formatIntWithDecimal(f)

                                binding.numberTextView.text = fin

                            }


                            if (userData2 != null || userData3 != null){

                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }


                            }




                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)
                            val ltpper = formatIntWithDecimal(ltppercent)


                            binding.textView29.text = ltp
                            binding.textView27.text = "+${fluct}"

                            todaypnl = f


                            if (userData2 == null || userData3 == null){

                                val textColor = ContextCompat.getColor(context, R.color.green)
                                binding.numberTextView.setTextColor(textColor)
                                val fin = formatIntWithDecimal(f)
                                binding.numberTextView.text = "+$fin"

                            }


                            if (userData2 != null || userData3 != null){

                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }

                            }




                        }


                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "buy", userData.avg, "","y",userData.last,"","",userData.nrml,userData.monthly)
                            sharedPreferencesManager.saveData(userDataa)


                        }


                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)



z


            }

            if (userData.type == "sell"){





                val textColor = ContextCompat.getColor(context, R.color.red)


                binding.layout1.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
                binding.cardView2.visibility = View.VISIBLE
                binding.demo.visibility = View.GONE


                if (userData.nrml == "nrml"){ binding.nrml.visibility = View.VISIBLE }else{ binding.mis.visibility = View.VISIBLE }

                if (userData.monthly == "W"){ binding.count.text = "W" }else{ binding.countcard.visibility = View.GONE }



                binding.textView.text = userData.name
                binding.textView25.text = userData.last
                binding.textView26.text = userData.avg
                binding.textView22.text = "-${userData.qty}"
                binding.textView22.setTextColor(textColor)




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

                            if (userData.nrml == "nrml"){ binding.nrml.visibility = View.VISIBLE }else{ binding.mis.visibility = View.VISIBLE }

                            if (userData.monthly == "W"){ binding.count.text = "W" }else{ binding.countcard.visibility = View.GONE }




                            val textColor = ContextCompat.getColor(context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(context, R.color.nrml)
                            val misbg = ContextCompat.getColor(context, R.color.misbg)
                            val mis = ContextCompat.getColor(context, R.color.mis)

                            binding.nrml.setBackgroundColor(nrmlbg)
                            binding.nrmltext.setTextColor(nrml)
                            binding.mis.setBackgroundColor(misbg)
                            binding.mistext.setTextColor(mis)
                            binding.textView.setTextColor(textColor)
                            binding.textView25.setTextColor(textColor)
                            binding.textView20.setTextColor(textColor)
                            binding.textView26.setTextColor(textColor)
                            binding.textView29.setTextColor(textColor)
                            binding.textView22.setTextColor(textColor)

                            binding.textView.text = userData.name
                            binding.textView25.text = userData.last
                            binding.textView22.text = "0"

                            binding.textView26.text = "0.00"
                            binding.textView29.text = userData.ltp.toDouble().toString()



                            val fixvalue = userData.ltp.toDouble() - userData.avg.toDouble()

                            val qty = userData.qty.toInt() * -1
                            val f = fixvalue * qty


                            var minus = userData.ltp.toDouble() - userData.avg.toDouble()

                            var devide = minus / userData.avg.toDouble()

                            val ltppercent =  devide * -100.0




                            if (userData.ltp.toDouble() > userData.avg.toDouble()) {

                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)

                                binding.textView29.text = ltp
                                binding.textView27.text = "${fluct}"

                                todaypnl = f

                                if (userData2 == null){

                                    val textColor = ContextCompat.getColor(context, R.color.red)
                                    binding.numberTextView.setTextColor(textColor)
                                    val fin = formatIntWithDecimal(f)
                                    binding.numberTextView.text = fin


                                }



                            } else {

                                val ltp = formatIntWithDecimal(userData.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                binding.textView29.text = ltp
                                binding.textView27.text = "+${fluct}"

                                todaypnl = f

                                if (userData2 == null){

                                    val textColor = ContextCompat.getColor(context, R.color.green)
                                    binding.numberTextView.setTextColor(textColor)
                                    val fin = formatIntWithDecimal(f)
                                    binding.numberTextView.text = "+$fin"

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
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "sell", userData.avg, "","y",userData.last,"","",userData.nrml,userData.monthly)
                            sharedPreferencesManager.saveData(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue > userData.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.red)
                            binding.textView27.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)




                            binding.textView29.text = ltp
                            binding.textView27.text = "${fluct}"

                            todaypnl = f*1.0

                            if (userData2 == null){

                                val textColor = ContextCompat.getColor(context, R.color.red)
                                binding.numberTextView.setTextColor(textColor)
                                val fin = formatIntWithDecimal(f)
                                binding.numberTextView.text = fin


                            }


                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)


                            binding.textView29.text = ltp
                            binding.textView27.text = "+${fluct}"

                            todaypnl = f


                            if (userData2 == null){

                                val textColor = ContextCompat.getColor(context, R.color.green)
                                binding.numberTextView.setTextColor(textColor)
                                val fin = formatIntWithDecimal(f)
                                binding.numberTextView.text = "+$fin"

                            }


                        }



                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()
                            isFluctuating = false

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData.qty, currentValue.toString(), userData.name, "sell", userData.avg, "","y",userData.last,"","",userData.nrml,userData.monthly)
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



                binding.layout2.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
                binding.cardView2.visibility = View.VISIBLE
                binding.demo.visibility = View.GONE



                if (userData2.nrml == "nrml"){ binding.nrmlsecond.visibility = View.VISIBLE }else{ binding.missecond.visibility = View.VISIBLE }

                if (userData2.monthly == "W"){ binding.countsecond.text = "W" }else{ binding.countcardsecond.visibility = View.GONE }



                binding.textViewsecond.text = userData2.name
                binding.textView25second.text = userData2.last
                binding.textView26second.text = userData2.avg
                binding.textView22second.text = userData2.qty



                var isFluctuating = true
                var currentValue = userData2.avg.toDouble()
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


                            if (userData2.nrml == "nrml"){ binding.nrmlsecond.visibility = View.VISIBLE }else{ binding.missecond.visibility = View.VISIBLE }

                            if (userData2.monthly == "W"){ binding.countsecond.text = "W" }else{ binding.countcardsecond.visibility = View.GONE }




                            val textColor = ContextCompat.getColor(context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(context, R.color.nrml)
                            val misbg = ContextCompat.getColor(context, R.color.misbg)
                            val mis = ContextCompat.getColor(context, R.color.mis)

                            binding.nrmlsecond.setBackgroundColor(nrmlbg)
                            binding.nrmltextsecond.setTextColor(nrml)
                            binding.missecond.setBackgroundColor(misbg)
                            binding.mistextsecond.setTextColor(mis)
                            binding.textViewsecond.setTextColor(textColor)
                            binding.textView25second.setTextColor(textColor)
                            binding.textView20second.setTextColor(textColor)
                            binding.textView26second.setTextColor(textColor)
                            binding.textView29second.setTextColor(textColor)
                            binding.textView22second.setTextColor(textColor)

                            binding.textViewsecond.text = userData2.name
                            binding.textView25second.text = userData2.last
                            binding.textView22second.text = "0"

                            binding.textView26second.text = "0.00"
                            binding.textView29second.text = userData2.ltp.toDouble().toString()

                            val fixvalue = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            val f = fixvalue * userData2.qty.toInt()

                            var minus = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            var devide = minus / userData2.avg.toDouble()

                            val ltppercent =  devide * 100.0




                            if (userData2.ltp.toDouble() < userData2.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red)


                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)

                                binding.textView29second.text = ltp
                                binding.textView27second.text = "${fluct}"

                                todaypnl2 = f


                                if (userData3 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.red)
                                    val textColor2 = ContextCompat.getColor(context, R.color.green)


                                    var minu = todaypnl - todaypnl2 - todaypnl3
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.numberTextView.setTextColor(textColor)
                                        binding.numberTextView.text = fin


                                    }else{

                                        binding.numberTextView.setTextColor(textColor2)
                                        binding.numberTextView.text = "+$fin"

                                    }


                                }




                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)


                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                binding.textView29second.text = ltp
                                binding.textView27second.text = "+${fluct}"

                                todaypnl2 = f


                                if (userData3 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.red)
                                    val textColor2 = ContextCompat.getColor(context, R.color.green)


                                    var minu = todaypnl + todaypnl2 + todaypnl3
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.numberTextView.setTextColor(textColor)
                                        binding.numberTextView.text = fin


                                    }else{

                                        binding.numberTextView.setTextColor(textColor2)
                                        binding.numberTextView.text = "+$fin"

                                    }


                                }


                            }

                            return

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


                        binding.layout2.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "buy", userData2.avg, "","y",userData2.last,"","",userData2.nrml,userData2.monthly)
                            sharedPreferencesManager.saveData2(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue < userData2.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context,  R.color.red)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)

                            binding.textView27second.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)





                            binding.textView29second.text = ltp
                            binding.textView27second.text = "${fluct}"

                            todaypnl2 = f*1.0



                            if (userData3 == null){
                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }


                            }


                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27second.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)


                            binding.textView29second.text = ltp
                            binding.textView27second.text = "+${fluct}"

                            todaypnl2 = f



                            if (userData3 == null){
                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }


                            }


                        }


                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "buy", userData2.avg, "","y",userData2.last,"","",userData2.nrml,userData2.monthly)
                            sharedPreferencesManager.saveData2(userDataa)


                        }


                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)






            }

            if (userData2.type == "sell"){





                val textColor = ContextCompat.getColor(context, R.color.red)


                binding.layout2.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
                binding.cardView2.visibility = View.VISIBLE
                binding.demo.visibility = View.GONE


                if (userData2.nrml == "nrml"){ binding.nrmlsecond.visibility = View.VISIBLE }else{ binding.missecond.visibility = View.VISIBLE }

                if (userData2.monthly == "W"){ binding.countsecond.text = "W" }else{ binding.countcardsecond.visibility = View.GONE }



                binding.textViewsecond.text = userData2.name
                binding.textView25second.text = userData2.last
                binding.textView26second.text = userData2.avg
                binding.textView22second.text = "-${userData2.qty}"
                binding.textView22second.setTextColor(textColor)




                var isFluctuating = true
                var currentValue = userData2.avg.toDouble()
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

                            if (userData2.nrml == "nrml"){ binding.nrmlsecond.visibility = View.VISIBLE }else{ binding.missecond.visibility = View.VISIBLE }

                            if (userData2.monthly == "W"){ binding.countsecond.text = "W" }else{ binding.countcardsecond.visibility = View.GONE }




                            val textColor = ContextCompat.getColor(context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(context, R.color.nrml)
                            val misbg = ContextCompat.getColor(context, R.color.misbg)
                            val mis = ContextCompat.getColor(context, R.color.mis)

                            binding.nrmlsecond.setBackgroundColor(nrmlbg)
                            binding.nrmltextsecond.setTextColor(nrml)
                            binding.missecond.setBackgroundColor(misbg)
                            binding.mistextsecond.setTextColor(mis)
                            binding.textViewsecond.setTextColor(textColor)
                            binding.textView25second.setTextColor(textColor)
                            binding.textView20second.setTextColor(textColor)
                            binding.textView26second.setTextColor(textColor)
                            binding.textView29second.setTextColor(textColor)
                            binding.textView22second.setTextColor(textColor)

                            binding.textViewsecond.text = userData2.name
                            binding.textView25second.text = userData2.last
                            binding.textView22second.text = "0"

                            binding.textView26second.text = "0.00"
                            binding.textView29second.text = userData2.ltp.toDouble().toString()



                            val fixvalue = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            val qty = userData2.qty.toInt() * -1
                            val f = fixvalue * qty


                            var minus = userData2.ltp.toDouble() - userData2.avg.toDouble()

                            var devide = minus / userData2.avg.toDouble()

                            val ltppercent =  devide * -100.0




                            if (userData2.ltp.toDouble() > userData2.avg.toDouble()) {

                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)

                                binding.textView29.text = ltp
                                binding.textView27.text = "${fluct}"

                                todaypnl2 = f


                                if (userData3 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.red)
                                    val textColor2 = ContextCompat.getColor(context, R.color.green)


                                    var minu = todaypnl - todaypnl2 - todaypnl3
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.numberTextView.setTextColor(textColor)
                                        binding.numberTextView.text = fin


                                    }else{

                                        binding.numberTextView.setTextColor(textColor2)
                                        binding.numberTextView.text = "+$fin"

                                    }


                                }




                            } else {

                                val ltp = formatIntWithDecimal(userData2.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)


                                binding.textView29second.text = ltp
                                binding.textView27second.text = "+${fluct}"

                                todaypnl2 = f


                                if (userData3 == null){
                                    val textColor = ContextCompat.getColor(context, R.color.red)
                                    val textColor2 = ContextCompat.getColor(context, R.color.green)


                                    var minu = todaypnl + todaypnl2 + todaypnl3
                                    val fin = formatIntWithDecimal(minu.toDouble())


                                    if (minu < 0.0){

                                        binding.numberTextView.setTextColor(textColor)
                                        binding.numberTextView.text = fin


                                    }else{

                                        binding.numberTextView.setTextColor(textColor2)
                                        binding.numberTextView.text = "+$fin"

                                    }


                                }


                            }

                            return

                        }


                        val randomIndex = Random.nextInt(0, minusvalues.size)

                        // Get the random value from the list
                        val fluctuation = minusvalues[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData2.avg.toDouble()

                        var qty =  userData2.qty.toInt() * -1

                        val f = fixvalue * qty

                        var minus = currentValue - userData2.avg.toDouble()

                        var devide = minus / userData2.avg.toDouble()

                        val ltppercent =  devide * -100.0


                        binding.layout2.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "sell", userData2.avg, "","y",userData2.last,"","",userData2.nrml,userData2.monthly)
                            sharedPreferencesManager.saveData2(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue > userData2.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.red)
                            binding.textView27second.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)




                            binding.textView29second.text = ltp
                            binding.textView27second.text = "${fluct}"

                            todaypnl2 = f*1.0


                            if (userData3 == null){
                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }


                            }



                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27second.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)


                            binding.textView29second.text = ltp
                            binding.textView27second.text = "+${fluct}"

                            todaypnl2 = f



                            if (userData3 == null){
                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }


                            }



                        }



                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()
                            isFluctuating = false

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData2.qty, currentValue.toString(), userData2.name, "sell", userData2.avg, "","y",userData2.last,"","",userData2.nrml,userData2.monthly)
                            sharedPreferencesManager.saveData2(userDataa)




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


        if (userData3 != null) {


            if (userData3.type == "buy"){



                binding.layout3.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
                binding.cardView2.visibility = View.VISIBLE
                binding.demo.visibility = View.GONE



                if (userData3.nrml == "nrml"){ binding.nrmlThird.visibility = View.VISIBLE }else{ binding.misThird.visibility = View.VISIBLE }

                if (userData3.monthly == "W"){ binding.countThird.text = "W" }else{ binding.countcardThird.visibility = View.GONE }



                binding.textViewThird.text = userData3.name
                binding.textView25Third.text = userData3.last
                binding.textView26Third.text = userData3.avg
                binding.textView22Third.text = userData3.qty



                var isFluctuating = true
                var currentValue = userData3.avg.toDouble()
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


                            if (userData3.nrml == "nrml"){ binding.nrmlThird.visibility = View.VISIBLE }else{ binding.misThird.visibility = View.VISIBLE }

                            if (userData3.monthly == "W"){ binding.countThird.text = "W" }else{ binding.countcardThird.visibility = View.GONE }




                            val textColor = ContextCompat.getColor(context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(context, R.color.nrml)
                            val misbg = ContextCompat.getColor(context, R.color.misbg)
                            val mis = ContextCompat.getColor(context, R.color.mis)

                            binding.nrmlThird.setBackgroundColor(nrmlbg)
                            binding.nrmltextThird.setTextColor(nrml)
                            binding.misThird.setBackgroundColor(misbg)
                            binding.mistextThird.setTextColor(mis)
                            binding.textViewThird.setTextColor(textColor)
                            binding.textView25Third.setTextColor(textColor)
                            binding.textView20Third.setTextColor(textColor)
                            binding.textView26Third.setTextColor(textColor)
                            binding.textView29Third.setTextColor(textColor)
                            binding.textView22Third.setTextColor(textColor)

                            binding.textViewThird.text = userData3.name
                            binding.textView25Third.text = userData3.last
                            binding.textView22Third.text = "0"

                            binding.textView26Third.text = "0.00"
                            binding.textView29Third.text = userData3.ltp.toDouble().toString()

                            val fixvalue = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            val f = fixvalue * userData3.qty.toInt()

                            var minus = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            var devide = minus / userData3.avg.toDouble()

                            val ltppercent =  devide * 100.0




                            if (userData3.ltp.toDouble() < userData3.avg.toDouble()) {

                                val textColor = ContextCompat.getColor(context, R.color.red)


                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)

                                binding.textView29Third.text = ltp
                                binding.textView27Third.text = "${fluct}"

                                todaypnl3 = f

                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin

                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }






                            } else {


                                val textColor = ContextCompat.getColor(context, R.color.green)


                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)
                                val ltpper = formatIntWithDecimal(ltppercent)


                                binding.textView29Third.text = ltp
                                binding.textView27Third.text = "+${fluct}"

                                todaypnl3 = f


                                val textColor2 = ContextCompat.getColor(context, R.color.red)



                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = "+$fin"

                                }





                            }

                            return

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


                        binding.layout3.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "buy", userData3.avg, "","y",userData3.last,"","",userData3.nrml,userData3.monthly)
                            sharedPreferencesManager.saveData3(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue < userData3.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context,  R.color.red)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)

                            binding.textView27Third.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)





                            binding.textView29Third.text = ltp
                            binding.textView27Third.text = "${fluct}"

                            todaypnl3 = f*1.0






                            var minu = todaypnl - todaypnl2 - todaypnl3
                            val fin = formatIntWithDecimal(minu.toDouble())


                            if (minu < 0.0){

                                binding.numberTextView.setTextColor(textColor)
                                binding.numberTextView.text = fin


                            }else{

                                binding.numberTextView.setTextColor(textColor2)
                                binding.numberTextView.text = "+$fin"

                            }





                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27Third.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)


                            binding.textView29Third.text = ltp
                            binding.textView27Third.text = "+${fluct}"

                            todaypnl3 = f




                            val textColor3 = ContextCompat.getColor(context, R.color.red)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)


                            var minu = todaypnl + todaypnl2 + todaypnl3
                            val fin = formatIntWithDecimal(minu.toDouble())


                            if (minu < 0.0){

                                binding.numberTextView.setTextColor(textColor3)
                                binding.numberTextView.text = fin


                            }else{

                                binding.numberTextView.setTextColor(textColor2)
                                binding.numberTextView.text = "+$fin"

                            }




                        }


                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()

                        } else if (currentValue > maxValue.toDouble()) {
                            currentValue = maxValue.toDouble()
                            isFluctuating = false // Stop fluctuation when max value is reached

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "buy", userData3.avg, "","y",userData3.last,"","",userData3.nrml,userData3.monthly)
                            sharedPreferencesManager.saveData3(userDataa)

                        }


                        // Continue fluctuation
                        handler.postDelayed(this, fluctuationInterval)
                    }
                }, fluctuationInterval)






            }

            if (userData3.type == "sell"){





                val textColor = ContextCompat.getColor(context, R.color.red)


                binding.layout3.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
                binding.cardView2.visibility = View.VISIBLE
                binding.demo.visibility = View.GONE


                if (userData3.nrml == "nrml"){ binding.nrmlThird.visibility = View.VISIBLE }else{ binding.misThird.visibility = View.VISIBLE }

                if (userData3.monthly == "W"){ binding.countThird.text = "W" }else{binding.countcardThird.visibility = View.GONE }



                binding.textViewThird.text = userData3.name
                binding.textView25Third.text = userData3.last
                binding.textView26Third.text = userData3.avg
                binding.textView22Third.text = "-${userData3.qty}"
                binding.textView22Third.setTextColor(textColor)




                var isFluctuating = true
                var currentValue = userData3.avg.toDouble()
                val minValue = "-${userData3.ltpmin}"
                val maxValue = userData3.ltpmax



                // continue this

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

                            if (userData3.nrml == "nrml"){ binding.nrmlThird.visibility = View.VISIBLE }else{ binding.misThird.visibility = View.VISIBLE }

                            if (userData3.monthly == "W"){ binding.countThird.text = "W" }else{ binding.countcardThird.visibility = View.GONE }




                            val textColor = ContextCompat.getColor(context, R.color.text)
                            val nrmlbg = ContextCompat.getColor(context, R.color.nrmlbg)
                            val nrml = ContextCompat.getColor(context, R.color.nrml)
                            val misbg = ContextCompat.getColor(context, R.color.misbg)
                            val mis = ContextCompat.getColor(context, R.color.mis)

                            binding.nrmlThird.setBackgroundColor(nrmlbg)
                            binding.nrmltextThird.setTextColor(nrml)
                            binding.misThird.setBackgroundColor(misbg)
                            binding.mistextThird.setTextColor(mis)
                            binding.textViewThird.setTextColor(textColor)
                            binding.textView25Third.setTextColor(textColor)
                            binding.textView20Third.setTextColor(textColor)
                            binding.textView26Third.setTextColor(textColor)
                            binding.textView29Third.setTextColor(textColor)
                            binding.textView22Third.setTextColor(textColor)

                            binding.textViewThird.text = userData3.name
                            binding.textView25Third.text = userData3.last
                            binding.textView22Third.text = "0"

                            binding.textView26Third.text = "0.00"
                            binding.textView29Third.text = userData3.ltp.toDouble().toString()



                            val fixvalue = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            val qty = userData3.qty.toInt() * -1
                            val f = fixvalue * qty


                            var minus = userData3.ltp.toDouble() - userData3.avg.toDouble()

                            var devide = minus / userData3.avg.toDouble()

                            val ltppercent =  devide * -100.0




                            if (userData3.ltp.toDouble() > userData3.avg.toDouble()) {

                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)

                                binding.textView29Third.text = ltp
                                binding.textView27Third.text = "${fluct}"

                                todaypnl3 = f



                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl - todaypnl2 - todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }







                            } else {

                                val ltp = formatIntWithDecimal(userData3.ltp.toDouble())
                                val fluct = formatIntWithDecimal(f)


                                binding.textView29Third.text = ltp
                                binding.textView27Third.text = "+${fluct}"

                                todaypnl3 = f



                                val textColor = ContextCompat.getColor(context, R.color.red)
                                val textColor2 = ContextCompat.getColor(context, R.color.green)


                                var minu = todaypnl + todaypnl2 + todaypnl3
                                val fin = formatIntWithDecimal(minu.toDouble())


                                if (minu < 0.0){

                                    binding.numberTextView.setTextColor(textColor)
                                    binding.numberTextView.text = fin


                                }else{

                                    binding.numberTextView.setTextColor(textColor2)
                                    binding.numberTextView.text = "+$fin"

                                }



                            }

                            return

                        }


                        val randomIndex = Random.nextInt(0, minusvalues.size)

                        // Get the random value from the list
                        val fluctuation = minusvalues[randomIndex]

                        currentValue += fluctuation.toDouble()




                        val fixvalue = currentValue - userData3.avg.toDouble()

                        var qty =  userData3.qty.toInt() * -1

                        val f = fixvalue * qty

                        var minus = currentValue - userData3.avg.toDouble()

                        var devide = minus / userData3.avg.toDouble()

                        val ltppercent =  devide * -100.0


                        binding.layout3.setOnClickListener {

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "sell", userData3.avg, "","y",userData3.last,"","",userData3.nrml,userData3.monthly)
                            sharedPreferencesManager.saveData3(userDataa)

                        }



                        // Update the UI with the new value
                        if (currentValue > userData3.avg.toDouble()) {


                            val textColor = ContextCompat.getColor(context, R.color.red)
                            binding.textView27Third.setTextColor(textColor)



                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)




                            binding.textView29Third.text = ltp
                            binding.textView27Third.text = "${fluct}"

                            todaypnl3 = f*1.0



                            val textColor3 = ContextCompat.getColor(context, R.color.red)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)


                            var minu = todaypnl - todaypnl2 - todaypnl3
                            val fin = formatIntWithDecimal(minu.toDouble())


                            if (minu < 0.0){

                                binding.numberTextView.setTextColor(textColor3)
                                binding.numberTextView.text = fin


                            }else{

                                binding.numberTextView.setTextColor(textColor2)
                                binding.numberTextView.text = "+$fin"

                            }





                        } else {


                            val textColor = ContextCompat.getColor(context, R.color.green)
                            binding.textView27Third.setTextColor(textColor)




                            val ltp = formatIntWithDecimal(currentValue)
                            val fluct = formatIntWithDecimal(f)


                            binding.textView27Third.text = ltp
                            binding.textView27Third.text = "+${fluct}"

                            todaypnl3 = f



                            val textColor3 = ContextCompat.getColor(context, R.color.red)
                            val textColor2 = ContextCompat.getColor(context, R.color.green)


                            var minu = todaypnl + todaypnl2 + todaypnl3
                            val fin = formatIntWithDecimal(minu.toDouble())


                            if (minu < 0.0){

                                binding.numberTextView.setTextColor(textColor3)
                                binding.numberTextView.text = fin


                            }else{

                                binding.numberTextView.setTextColor(textColor2)
                                binding.numberTextView.text = "+$fin"

                            }



                        }



                        // Ensure currentValue stays within the desired range
                        if (currentValue < minValue.toDouble()) {
                            currentValue = minValue.toDouble()
                            isFluctuating = false

                            val sharedPreferencesManager = SharedPreferencesManager(requireContext()) // 'this' should be the context of your activity or fragment
                            val userDataa = UserData(userData3.qty, currentValue.toString(), userData3.name, "sell", userData3.avg, "","y",userData3.last,"","",userData3.nrml,userData3.monthly)
                            sharedPreferencesManager.saveData3(userDataa)


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



    }





    fun formatIntWithDecimal(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val formatter = DecimalFormat("#,##,##0.00", symbols)
        return formatter.format(value.toDouble())
    }






}
