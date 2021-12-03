package com.lettytrain.notesapp.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.R
import com.lettytrain.notesapp.entities.Notes
import kotlinx.android.synthetic.main.item_rv_notes.view.*
import java.text.FieldPosition
import javax.microedition.khronos.egl.EGL10

class NotesAdapter() :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    var listener: OnItemClickListener? = null
    var arrayList = ArrayList<Notes>()

    class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes, parent, false)
        )
    }

    override fun getItemCount() = arrayList.size
    fun setData(arrNotesList: List<Notes>) {
        arrayList = arrNotesList as ArrayList<Notes>
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.itemView.tvTitle.text = arrayList[position].title
        holder.itemView.tvDesc.text = arrayList[position].noteText
        holder.itemView.tvDateTime.text = arrayList[position].updateTime

        if (arrayList[position].color != null && arrayList[position].color != "") {
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(arrayList[position].color))
        } else {
            holder.itemView.cardView.setCardBackgroundColor(
                MyApplication.context.resources.getColor(
                    R.color.ColorLightBlack
                )
            )
        }
        if (arrayList[position].imgPath != null) {
            holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(arrayList[position].imgPath))
            holder.itemView.imgNote.visibility = View.VISIBLE
        } else {
            holder.itemView.imgNote.visibility = View.GONE
        }

        holder.itemView.cardView.setOnClickListener {
            Log.d("notesAdapter", "notesId${arrayList[position].id!!}")
            listener!!.onClicked(arrayList[position].id!!)
        }
    }

    interface OnItemClickListener {
        fun onClicked(noteId: Int)
    }
}