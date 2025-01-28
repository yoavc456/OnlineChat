package database

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import messages.Message
import messages.MessageAction

class MongoDBManager : DatabaseManager {

    companion object {
        private var mongoDBManager: MongoDBManager? = null
        private const val URL: String = "mongodb://localhost:27017"
        private const val DATABASE_NAME = "chatDB2"
        //onlineChatDB

        fun getInstance(): MongoDBManager {
            if (mongoDBManager == null)
                mongoDBManager = MongoDBManager(URL, DATABASE_NAME)

            return mongoDBManager!!
        }
    }

    private val connection: MongoClient
    private val database: MongoDatabase

    private constructor(url: String, dbName: String) {
        connection = MongoClient.create(url)
        database = connection.getDatabase(dbName)
    }

    override suspend fun doesUserExist(username: String, password: String): Boolean {
        val collection = database.getCollection<User>("users")
        var user: User? = null

        val filter = Filters.eq("username", username)
        collection.find<User>(filter).collect {
            user = it
        }


        if (user == null)
            return false

        return user!!.password.equals(password)
    }

    override suspend fun doesUsernameExist(username: String): Boolean {
        val collection = database.getCollection<User>("users")

        var user: User? = null

        val filter = Filters.eq("username", username)
        collection.find<User>(filter).collect {
            user = it
        }

        return user != null
    }

    override suspend fun createUser(username: String, password: String): Boolean {
        if (doesUsernameExist(username))
            return false

        val collection = database.getCollection<User>("users")
        val user = User(username, password)
        collection.insertOne(user)

        return true

    }

    override suspend fun doesChatExist(chatname: String): Boolean {
        val collection = database.getCollection<Chat>("chats")

        var chat:Chat? = null

        val filter = Filters.eq("chatname", chatname)
        collection.find<Chat>(filter).collect {
            chat = it
        }

        return chat != null
    }

    override suspend fun createChat(chatname: String, adming: String): Boolean {
        if (doesChatExist(chatname))
            return false

        val collection = database.getCollection<Chat>("chats")

        val chat = Chat(chatname, adming, true, mutableListOf<String>())
        collection.insertOne(chat)

        return true
    }

    override suspend fun enterChat(chatname: String, username: String): Boolean {
        val collection = database.getCollection<Chat>("chats")

        var chat: Chat? = null

        val filter = Filters.eq("chatname", chatname)
        collection.find<Chat>(filter).collect {
            chat = it
        }

        if (chat == null)
            return false
        if (chat!!.open)
            return true
        if (chat!!.admin.equals(username))
            return true
        if (chat!!.members.contains(username))
            return true

        return false
    }

    override suspend fun saveMessage(chatname: String, sender: String, msg: String) {
        val collection = database.getCollection<MongodbChatMessage>("_" + chatname)

        val message = MongodbChatMessage(sender, msg)
        collection.insertOne(message)
    }

    override suspend fun loadMessages(chatname: String): List<Message> {
        val collection = database.getCollection<MongodbChatMessage>("_" + chatname)
        val result = mutableListOf<Message>()

        collection.find<MongodbChatMessage>().collect {
            result.add(Message(action = MessageAction.TEXT, message = it.msg, username = it.sender))
        }
        return result
    }

    override suspend fun getChatAdmin(chatname: String): String {
        val collection = database.getCollection<Chat>("chats")

        var result = ""

        val filter = Filters.eq("chatname", chatname)
        collection.find<Chat>(filter).collect {
            result = it.admin
        }

        return result
    }

    override suspend fun setChatPrivacy(action: MessageAction, chatname: String) {
        val collection = database.getCollection<Chat>("chats")

        val filter = Filters.eq("chatname", chatname)
        val update = Updates.set("open", action == MessageAction.PUBLIC_CHAT)
        collection.updateOne(filter, update)
    }

    override suspend fun addUserToChat(chatname: String, username: String) {
        val collection = database.getCollection<Chat>("chats")

        val filter = Filters.eq("chatname", chatname)

        val chat: Chat = collection.find<Chat>(filter).first()
        chat.members.add(username)

        val update = Updates.set("members", chat.members)
        collection.updateOne(filter, update)
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

data class User(
    val username: String,
    val password: String
)

data class Chat(
    val chatname: String,
    val admin: String,
    val open: Boolean,
    val members: MutableList<String>
)

data class MongodbChatMessage(
    val sender: String,
    val msg: String
)