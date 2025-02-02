package server.database.mongodb

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import messages.Message
import messages.MessageAction
import server.database.DatabaseManager
import server.database.DatabaseMessagesManager

class MongoDBMessagesManager(val database:MongoDatabase):DatabaseMessagesManager {
    override suspend fun saveMessage(chatname: String, sender: String, msg: String) {
        val collectionName = getChatMessagesCollectionName(chatname)
        val collection = database.getCollection<MongodbChatMessage>(collectionName)

        val message = MongodbChatMessage(sender, msg)
        collection.insertOne(message)
    }

    override suspend fun loadMessages(chatname: String): List<Message> {
        val collectionName = getChatMessagesCollectionName(chatname)
        val collection = database.getCollection<MongodbChatMessage>(collectionName)
        val result = mutableListOf<Message>()

        collection.find<MongodbChatMessage>().collect {
            result.add(Message(action = MessageAction.TEXT, message = it.msg, username = it.sender))
        }
        return result
    }

    private fun getChatMessagesCollectionName(chatname: String):String{
        val prefix = "_"
        return prefix + chatname
    }
}