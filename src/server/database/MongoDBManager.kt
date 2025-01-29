package server.database

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import messages.Message
import messages.MessageAction

object MongoDBManager : DatabaseManager {

    private const val URL: String = "mongodb://localhost:27017"
    private const val DATABASE_NAME = "chatDB2"
    //onlineChatDB

    private val connection: MongoClient = MongoClient.create(URL)
    private val database: MongoDatabase = connection.getDatabase(DATABASE_NAME)

    private val usersCollection = database.getCollection<User>("users")
    private val chatsCollection = database.getCollection<Chat>("chats")

    override suspend fun doesUserExist(username: String, password: String): Boolean {
        val filter = Filters.eq(User::username.name, username)
        val user = usersCollection.find<User>(filter).firstOrNull() ?: return false
        return user.password == password
    }

    override suspend fun doesUsernameExist(username: String): Boolean {
        val filter = Filters.eq(User::username.name, username)
        return usersCollection.find<User>(filter).firstOrNull()==null
    }

    override suspend fun createUser(username: String, password: String): Boolean {
        if (doesUsernameExist(username))
            return false

        val user = User(username, password)
        usersCollection.insertOne(user)

        println("Created New User. username: $username passeord: $password ")
        return true

    }

    override suspend fun doesChatExist(chatname: String): Boolean {
        val filter = Filters.eq(Chat::chatname.name, chatname)
        return chatsCollection.find<Chat>(filter).firstOrNull() != null
    }

    override suspend fun createChat(chatname: String, adming: String): Boolean {
        if (doesChatExist(chatname))
            return false

        val chat = Chat(chatname, adming, true, mutableListOf<String>())
        chatsCollection.insertOne(chat)

        println("Created New Chat. chatname: $chatname")
        return true
    }

    override suspend fun enterChat(chatname: String, username: String): Boolean {
        val filter = Filters.eq(Chat::chatname.name, chatname)
        val chat = chatsCollection.find<Chat>(filter).firstOrNull() ?: return false

        if (chat.open)
            return true
        if (chat.admin.equals(username))
            return true
        if (chat.members.contains(username))
            return true

        return false
    }

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

    override suspend fun getChatAdmin(chatname: String): String {
        var result = ""

        val filter = Filters.eq(Chat::chatname.name, chatname)
        chatsCollection.find<Chat>(filter).collect {
            result = it.admin
        }

        return result
    }

    override suspend fun setChatPrivacy(action: MessageAction, chatname: String) {
        val filter = Filters.eq(Chat::chatname.name, chatname)
        val update = Updates.set(Chat::open.name, action == MessageAction.PUBLIC_CHAT)
        chatsCollection.updateOne(filter, update)

        println("Set Chat Privacy. chatname: $chatname privacy: $action ")
    }

    override suspend fun addUserToChat(chatname: String, username: String) {
        val filter = Filters.eq(Chat::chatname.name, chatname)

        val chat: Chat = chatsCollection.find<Chat>(filter).first()
        chat.members.add(username)

        val update = Updates.set(Chat::members.name, chat.members)
        chatsCollection.updateOne(filter, update)

        println("Added User To Chat. username: $username chatname: $chatname ")
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

    private fun getChatMessagesCollectionName(chatname: String):String{
        val prefix = "_"
        return prefix + chatname
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