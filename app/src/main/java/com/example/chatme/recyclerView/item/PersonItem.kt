package com.example.chatme.recyclerView.item

import android.content.Context
import com.example.chatme.R
import com.example.chatme.glide.GlideApp
import com.example.chatme.model.User
import com.example.chatme.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*

class PersonItem(val person: User,
                 val userId: String,
                 private val context: Context) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = person.name
        viewHolder.textView_bio.text = person.bio
        if (person.profilePicPath != null)
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(person.profilePicPath))
                .placeholder(R.drawable.ic_account)
                .into(viewHolder.imageView_profile_picture)
        else
            GlideApp.with(context)
                .load(R.drawable.ic_account)
                .into(viewHolder.imageView_profile_picture)
    }

    override fun getLayout() = R.layout.item_person
}
