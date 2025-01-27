package utils

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import messages.MessageAction

private const val URL: String = "mongodb://localhost:27017"
private const val DATABASE_NAME = "onlineChatDB"

suspend fun doesUserExist(username: String, password: String): Boolean {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<User> = connection.getDatabase(DATABASE_NAME).getCollection<User>("users")

    val filter = Filters.eq("username", username)

    var user: User? = null

    collection.find<User>(filter).collect {
        user = it
    }

    connection.close()

    if (user == null)
        return false

    return user!!.password.equals(password)
}

suspend fun doesUsernameExist(username: String): Boolean {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<User> = connection.getDatabase(DATABASE_NAME).getCollection<User>("users")

    val filter = Filters.eq("username", username)

    var user: User? = null

    collection.find<User>(filter).collect {
        user = it
    }

    connection.close()

    return user != null
}

suspend fun createUser(username: String, password: String): Boolean {
    if (doesUsernameExist(username))
        return false

    val connection = MongoClient.create(URL)
    val collection: MongoCollection<User> = connection.getDatabase(DATABASE_NAME).getCollection<User>("users")

    val user = User(username, password)
    collection.insertOne(user)

    connection.close()
    return true
}

suspend fun doesChatExist(chatname: String): Boolean {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Chat> = connection.getDatabase(DATABASE_NAME).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)

    var chat: Chat? = null

    collection.find<Chat>(filter).collect {
        chat = it
    }

    connection.close()

    if (chat == null)
        return false

    return true
}

suspend fun createChat(chatname: String, adming: String): Boolean {
    if (doesChatExist(chatname))
        return false

    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Chat> = connection.getDatabase(DATABASE_NAME).getCollection<Chat>("chats")

    val chat = Chat(chatname, adming, true, mutableListOf<String>())
    collection.insertOne(chat)

    connection.close()
    return true
}

suspend fun enterChat(chatname: String, username: String): Boolean {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Chat> = connection.getDatabase(DATABASE_NAME).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)

    var chat: Chat? = null

    collection.find<Chat>(filter).collect {
        chat = it
    }

    connection.close()

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

suspend fun saveMessage(chatname: String, sender: String, msg: String) {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Message> =
        connection.getDatabase(DATABASE_NAME).getCollection<Message>("_" + chatname)

    val message = Message(sender, msg)
    collection.insertOne(message)

    connection.close()
}

suspend fun loadMessages(chatname: String): List<messages.Message> {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Message> =
        connection.getDatabase(DATABASE_NAME).getCollection<Message>("_" + chatname)

    val result = mutableListOf<messages.Message>()

    collection.find<Message>().collect {
        result.add(messages.Message(action = MessageAction.TEXT, message = it.msg, username = it.sender))
    }

    connection.close()
    return result
}

suspend fun getChatAdmin(chatname: String): String {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Chat> = connection.getDatabase(DATABASE_NAME).getCollection<Chat>("chats")

    var result = ""
    val filter = Filters.eq("chatname", chatname)
    collection.find<Chat>(filter).collect {
        result = it.admin
    }

    connection.close()
    return result
}

suspend fun setChatPrivacy(action: MessageAction, chatname: String) {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Chat> = connection.getDatabase(DATABASE_NAME).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)
    val update = Updates.set("open", action == MessageAction.PUBLIC_CHAT)

    collection.updateOne(filter, update)


    connection.close()
}

suspend fun addUserToChat(chatname: String, username: String) {
    val connection = MongoClient.create(URL)
    val collection: MongoCollection<Chat> = connection.getDatabase(DATABASE_NAME).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)

    val chat: Chat = collection.find<Chat>(filter).first()
    chat.members.add(username)

    val update = Updates.set("members", chat.members)
    collection.updateOne(filter, update)

    connection.close()
}

suspend fun createDatabase(dbName: String) {
    val connection = MongoClient.create(URL)
    val usersCollection: MongoCollection<User> = connection.getDatabase(dbName).getCollection<User>("users")

    val usernameIndexOptions = IndexOptions().unique(true)
    usersCollection.createIndex(Indexes.ascending("username"), usernameIndexOptions)

    val chatsCollection: MongoCollection<Chat> = connection.getDatabase(dbName).getCollection<Chat>("chats")
    val chatnameIndexOptions = IndexOptions().unique(true)
    chatsCollection.createIndex(Indexes.ascending("chatname"), chatnameIndexOptions)

    connection.close()
}

suspend fun deleteDatabase(dbName: String) {
    val connection = MongoClient.create(URL)
    val database: MongoDatabase = connection.getDatabase(dbName)
    database.drop()
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

data class Message(
    val sender: String,
    val msg: String
)