package server.database.mongodb

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import messages.Message
import messages.MessageAction
import server.database.DatabaseManager

object MongoDBManager : DatabaseManager {

    private const val URL: String = "mongodb://localhost:27017"
    private const val DATABASE_NAME = "chatDB2"

    private val connection: MongoClient = MongoClient.create(URL)
    private val database: MongoDatabase = connection.getDatabase(DATABASE_NAME)

    private val usersManager = MongoDBUsersManager(database)
    private val chatsManager = MongoDBChatsManager(database)
    private val messagesManager = MongoDBMessagesManager(database)

    override suspend fun doesUserExist(username: String, password: String): Boolean {
        return usersManager.doesUserExist(username, password)
    }

    override suspend fun doesUsernameExist(username: String): Boolean {
        return usersManager.doesUsernameExist(username)
    }

    override suspend fun createUser(username: String, password: String): Boolean {
        return usersManager.createUser(username, password)
    }

    override suspend fun doesChatExist(chatname: String): Boolean {
        return chatsManager.doesChatExist(chatname)
    }

    override suspend fun createChat(chatname: String, adming: String): Boolean {
        return chatsManager.createChat(chatname, adming)
    }

    override suspend fun enterChat(chatname: String, username: String): Boolean {
        return chatsManager.enterChat(chatname, username)
    }

    override suspend fun getChatAdmin(chatname: String): String {
        return chatsManager.getChatAdmin(chatname)
    }

    override suspend fun setChatPrivacy(action: MessageAction, chatname: String) {
        chatsManager.setChatPrivacy(action, chatname)
    }

    override suspend fun addUserToChat(chatname: String, username: String) {
        chatsManager.addUserToChat(chatname, username)
    }

    override suspend fun saveMessage(chatname: String, sender: String, msg: String) {
        messagesManager.saveMessage(chatname, sender, msg)
    }

    override suspend fun loadMessages(chatname: String): List<Message> {
        return messagesManager.loadMessages(chatname)
    }

    override suspend fun createDatabase(dbName: String) {
        val usersCollection: MongoCollection<User> = connection.getDatabase(dbName).getCollection<User>("users")
        val usernameIndexOptions = IndexOptions().unique(true)
        usersCollection.createIndex(Indexes.ascending("username"), usernameIndexOptions)

        val chatsCollection: MongoCollection<Chat> = connection.getDatabase(dbName).getCollection<Chat>("chats")
        val chatnameIndexOptions = IndexOptions().unique(true)
        chatsCollection.createIndex(Indexes.ascending("chatname"), chatnameIndexOptions)
    }

    override suspend fun deleteDatabase(dbName: String) {
        val database: MongoDatabase = connection.getDatabase(dbName)
        database.drop()
    }

    override fun close() {
        connection.close()
        println("Close Mongo")
    }

}