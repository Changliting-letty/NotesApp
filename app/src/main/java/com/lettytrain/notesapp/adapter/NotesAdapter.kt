package com.lettytrain.notesapp.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lettytrain.notesapp.R
import com.lettytrain.notesapp.entities.Notes
import kotlinx.android.synthetic.main.item_rv_notes.view.*
import java.text.FieldPosition

class NotesAdapter(val arrayList: List<Notes>):RecyclerView.Adapter<NotesAdapter.NotesViewHolder>(){

   inner class NotesViewHolder(view:View):RecyclerView.ViewHolder(view){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):NotesViewHolder{
       return  NotesViewHolder(
           LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes,parent,false))
    }
    override fun getItemCount()=arrayList.size

    override fun onBindViewHolder(holder:NotesViewHolder,position: Int){
        holder.itemView.tvTitle.text=arrayList[position].title
        holder.itemView.tvDesc.text=arrayList[position].noteText
        holder.itemView.tvDateTime.text=arrayList[position].dateTime

        if(arrayList[position].color!=null){
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(arrayList[position].color))
        }else holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(R.color.ColorLightBlack.toString()))
        if (arrayList[position].imgPath!=null){
                holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(arrayList[position].imgPath))
            holder.itemView.imgNote.visibility=View.VISIBLE
        }else{
            holder.itemView.imgNote.visibility=View.GONE
        }




    }
}