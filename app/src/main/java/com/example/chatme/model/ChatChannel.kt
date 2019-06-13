package com.example.chatme.model

data class ChatChannel(val userIds: MutableList<String?>) {
    constructor(): this(mutableListOf())
}