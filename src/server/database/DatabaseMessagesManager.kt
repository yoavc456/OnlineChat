package server.database

interface DatabaseMessagesManager {
    suspend fun saveMessage(chatname: String, sender: String, msg: String)
    suspend fun loadMessages(chatname: String): List<messages.Message>
}