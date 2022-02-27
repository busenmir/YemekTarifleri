package com.busedemir.yemektarifleri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListeRecyclerAdapter(val yemekListesi : ArrayList<String>, val idlistesi : ArrayList<Int>) : RecyclerView.Adapter<ListeRecyclerAdapter.YemekVH>(){
    class YemekVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return YemekVH(view)
    }

    override fun onBindViewHolder(holder: YemekVH, position: Int) {

        holder.itemView.textView.text=yemekListesi[position]
        holder.itemView.setOnClickListener(){
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("recyclerdangeldim",idlistesi[position])
            Navigation.findNavController(it).navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return yemekListesi.size
    }
}

