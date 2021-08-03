package com.iqsoft.my96chat.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.models.Message
import com.iqsoft.my96chat.models.User
import kotlinx.android.synthetic.main.item_message.view.*

class MessageListAdapter(private val context: Context, private var list: ArrayList<Message>, private val myUser: User, private val otherUser: User):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.e("testt", list.size.toString())
        Log.e("testt", position.toString())
        Log.e("testt", list.toString())
        val message = list[position]
        if(holder is MyViewHolder){
            if(message.sender == myUser.id){
                holder.itemView.message_my_linearlayout.visibility = View.VISIBLE
                holder.itemView.message_my_text.text = message.text
                Glide.with(context)
                    .load(myUser.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_face_24)
                    .into(holder.itemView.message_my_image)
            }else{
                holder.itemView.message_other_linearlayout.visibility = View.VISIBLE
                holder.itemView.message_other_text.text = message.text
                Glide.with(context)
                    .load(otherUser.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_face_24)
                    .into(holder.itemView.message_other_image)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}