package server.database.mongodb

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import messages.Message
import messages.MessageAction
import messages.server_msg.MessageInstruction
import messages.server_msg.ServerMessage
import server.database.DatabaseManager
import server.database.DatabaseMessagesManager

class MongoDBMessagesManager(val database:MongoDatabase):DatabaseMessagesManager {
    override suspend fun saveMessage(chatname: String, sender: String, msg: String) {
        val collectionName = getChatMessagesCollectionName(chatname)
        val collection = database.getCollection<MongodbChatMessage>(collectionName)

        val message = MongodbChatMessage(sender, msg)
        collection.insertOne(message)
    }

    override suspend fun loadMessages(chatname: String): List<ServerMessage> {
        val collectionName = getChatMessagesCollectionName(chatname)
        val collection = database.getCollection<MongodbChatMessage>(collectionName)
        val result = mutableListOf<ServerMessage>()

        collection.find<MongodbChatMessage>().collect {
            result.add(ServerMessage(instruction = MessageInstruction.PRINT, message = "${it.sender}: ${it.msg}"))
        }
        return result
    }

    private fun getChatMessagesCollectionName(chatname: String):String{
        val prefix = "_"
        return prefix + chatname
    }
}