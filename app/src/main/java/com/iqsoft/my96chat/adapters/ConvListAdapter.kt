package com.iqsoft.my96chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.models.User
import kotlinx.android.synthetic.main.item_conv.view.*

class ConvListAdapter(private val context: Context, private var list:ArrayList<User>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mOnClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_conv, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = list[position]
        if(holder is MyViewHolder){
            holder.itemView.item_conv_name_text.text = user.name
            Glide.with(context)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_face_24)
                .into(holder.itemView.item_conv_image)
            holder.itemView.setOnClickListener{
                if(mOnClickListener!=null){
                    mOnClickListener!!.onClick(user, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        mOnClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(user: User, position: Int)
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}