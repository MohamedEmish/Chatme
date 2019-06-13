package com.example.chatme.service

import com.example.chatme.util.FireStoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFireBaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val newRegistrationToken = FirebaseInstanceId.getInstance().token

        if (FirebaseAuth.getInstance().currentUser != null)
            addTokenToFireStore(newRegistrationToken)
    }

    companion object {
        fun addTokenToFireStore(newRegistrationToken: String?) {
            if (newRegistrationToken == null) throw NullPointerException("FCM token is null.")

            FireStoreUtil.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                FireStoreUtil.setFCMRegistrationTokens(tokens)
            }
        }
    }
}
