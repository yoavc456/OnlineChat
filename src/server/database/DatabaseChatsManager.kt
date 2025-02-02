package server.database

import messages.MessageAction

interface DatabaseChatsManager {
    suspend fun doesChatExist(chatname: String): Boolean
    suspend fun createChat(chatname: String, adming: String): Boolean
    suspend fun enterChat(chatname: String, username: String): Boolean
    suspend fun getChatAdmin(chatname: String): String
    suspend fun setChatPrivacy(action: MessageAction, chatname: String)
    suspend fun addUserToChat(chatname: String, username: String)
}