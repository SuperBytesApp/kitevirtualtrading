package com.clone.kitevirtualtrading;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private var items: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTextView.text = items[position]
        }

        override fun getItemCount(): Int {
        return items.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTextView: TextView = itemView.findViewById(R.id.nameTextView)
        }



}
