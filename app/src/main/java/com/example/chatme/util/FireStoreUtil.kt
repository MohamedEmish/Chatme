package com.example.chatme.util

import android.content.Context
import android.util.Log
import com.example.chatme.model.*
import com.example.chatme.recyclerView.item.ImageMessageItem
import com.example.chatme.recyclerView.item.PersonItem
import com.example.chatme.recyclerView.item.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item

object FireStoreUtil {
    private val fileStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = fileStoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw  NullPointerException("UID is Null")}"
        )

    private val chatChannelCollectionRef = fileStoreInstance.collection("chatChannels")

    fun initCurrentUserIfFirstTime(onComplete : () -> Unit){
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if(!documentSnapshot.exists()){
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                    "", null, mutableListOf())
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }else{
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name: String = "" , bio: String = "", profilePicPath: String? = null){
        val userFieldMap = mutableMapOf<String,Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (name.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicPath != null) userFieldMap["profilePicPath"] = profilePicPath

        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

    fun addUsersListener(context: Context, onListen : (List<Item>) -> Unit): ListenerRegistration {
        return fileStoreInstance.collection("users")
            .addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "USER LISTENER ERROR", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid){
                        items.add(PersonItem(it.toObject(User ::class.java)!!,it.id,context))
                    }
                }
                onListen(items)
            }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(otherUserId: String,
                               onComplete: (channelId: String)-> Unit){
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val newChannel = chatChannelCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId,otherUserId)))

                currentUserDocRef
                    .collection("engagedChatChannels")
                    .document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id))

                fileStoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId!!)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }

    fun addChatMessagrListener(channelId: String,
                               context: Context,
                               onListen: (List<Item>) -> Unit): ListenerRegistration{
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "CHAT MESSAGE LISTENER ERROR", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it["type"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!,context))
                    else
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!,context))
                    return@forEach
                }
                onListen(items)
            }
    }

    fun sendMessage(message: Message,channelId: String){
        chatChannelCollectionRef.document(channelId)
            .collection("messages")
            .add(message)
    }

    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit){
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User :: class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>){
        currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
    }
}