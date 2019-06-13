package com.example.chatme.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.chatme.R
import com.example.chatme.SignInActivity
import com.example.chatme.glide.GlideApp
import com.example.chatme.util.FireStoreUtil
import com.example.chatme.util.StorageUtil
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import java.io.ByteArrayOutputStream

class MyAccountFragment : Fragment() {

    private val RC_SELECT_IMAGE = 200
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_my_account, container, false)

        view.apply {
            imageView_profile_picture.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))
                }
                startActivityForResult(Intent.createChooser(intent,"Select Image"),RC_SELECT_IMAGE)
            }

            btn_save.setOnClickListener {
                if (:: selectedImageBytes.isInitialized) {
                    StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                        FireStoreUtil.updateCurrentUser(
                            editText_name.text.toString(),
                            editText_bio.text.toString(),
                            imagePath
                        )
                    }
                }else{
                    FireStoreUtil.updateCurrentUser(
                        editText_name.text.toString(),
                        editText_bio.text.toString(),
                        null
                    )
                }
                toast("Saving")
            }

            btn_sign_out.setOnClickListener {
                AuthUI.getInstance()
                    .signOut(this@MyAccountFragment.context!!)
                    .addOnCompleteListener {
                        startActivity(intentFor<SignInActivity>().newTask().clearTask())
                    }
            }
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SELECT_IMAGE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null) {

            val selectedImagePath = data.data
            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

            val outPutStream = ByteArrayOutputStream()
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outPutStream)

            selectedImageBytes = outPutStream.toByteArray()
            GlideApp.with(this)
                .load(selectedImageBytes)
                .into(imageView_profile_picture)

            pictureJustChanged = true
        }
    }

    override fun onStart() {
        super.onStart()
        FireStoreUtil.getCurrentUser { user ->
            if (this@MyAccountFragment.isVisible) {
                editText_name.setText(user.name)
                if (user.bio != null && user.bio.isNotEmpty()) {
                    editText_bio.setText(user.bio)
                }
                if (!pictureJustChanged && user.profilePicPath != null)
                    GlideApp.with(this)
                        .load(StorageUtil.pathToReference(user.profilePicPath))
                        .placeholder(R.drawable.ic_account)
                        .into(imageView_profile_picture)
            }
        }
    }
}