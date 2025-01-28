package database

import messages.MessageAction

interface DatabaseManager {
    suspend fun doesUserExist(username: String, password: String): Boolean
    suspend fun doesUsernameExist(username: String): Boolean
    suspend fun createUser(username: String, password: String): Boolean
    suspend fun doesChatExist(chatname: String): Boolean
    suspend fun createChat(chatname: String, adming: String): Boolean
    suspend fun enterChat(chatname: String, username: String): Boolean
    suspend fun saveMessage(chatname: String, sender: String, msg: String)
    suspend fun loadMessages(chatname: String): List<messages.Message>
    suspend fun getChatAdmin(chatname: String): String
    suspend fun setChatPrivacy(action: MessageAction, chatname: String)
    suspend fun addUserToChat(chatname: String, username: String)
    suspend fun createDatabase(dbName: String)
    suspend fun deleteDatabase(dbName: String)
    fun close()
}