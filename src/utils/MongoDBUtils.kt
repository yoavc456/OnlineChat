package utils

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.first
import messages.MessageAction

private val url:String = "mongodb://localhost:27017"
private val database_name = "onlineChatDB"

//Receives a username and a password. Check if there is a user with those username and password in 'users' collection.
suspend fun isUserExist(username:String, password:String):Boolean{
    val connection = MongoClient.create(url)
    val collection: MongoCollection<User> = connection.getDatabase(database_name).getCollection<User>("users")

    val filter = Filters.eq("username", username)

    var user: User? = null

    collection.find<User>(filter).collect{
        user = it
    }

    connection.close()

    if(user == null)
        return false

    return user!!.password.equals(password)
}

//Receives a username. Check if there is a user with that username in 'users' collection.
suspend fun isUsernameExist(username: String):Boolean{
    val connection = MongoClient.create(url)
    val collection: MongoCollection<User> = connection.getDatabase(database_name).getCollection<User>("users")

    val filter = Filters.eq("username", username)

    var user: User? = null

    collection.find<User>(filter).collect{
        user = it
    }

    connection.close()

    if(user == null)
        return false

    return true
}

//Receives a username and a password. If the username is not taken, it adds user with those details to 'users' collection
suspend fun createUser(username: String, password: String):Boolean{
    if(isUsernameExist(username))
        return false

    val connection = MongoClient.create(url)
    val collection: MongoCollection<User> = connection.getDatabase(database_name).getCollection<User>("users")

    val user = User(username, password)
    collection.insertOne(user)

    connection.close()
    return true
}

//Receives a chatname. Check if there is a chat with that chatname in 'chats' collection.
suspend fun isChatExist(chatname: String):Boolean{
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Chat> = connection.getDatabase(database_name).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)

    var chat: Chat? = null

    collection.find<Chat>(filter).collect{
        chat = it
    }

    connection.close()

    if(chat == null)
        return false

    return true
}

//Receive details of a new chat. If the chatname is not taken, it adds new chat with those details to 'chats' collection
suspend fun createChat(chatname:String, adming:String):Boolean{
    if(isChatExist(chatname))
        return false

    val connection = MongoClient.create(url)
    val collection: MongoCollection<Chat> = connection.getDatabase(database_name).getCollection<Chat>("chats")

    val chat = Chat(chatname, adming, true, mutableListOf<String>())
    collection.insertOne(chat)

    connection.close()
    return true
}

//Receive chatname and username. Tells if username can enter to chatname
suspend fun enterChat(chatname: String, username: String):Boolean{
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Chat> = connection.getDatabase(database_name).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)

    var chat: Chat? = null

    collection.find<Chat>(filter).collect{
        chat = it
    }

    connection.close()

    if(chat == null)
        return false

    if(chat!!.open)
        return true
    if (chat!!.admin.equals(username))
        return true
    if(chat!!.members.contains(username))
        return true

    return false
}

suspend fun saveMessage(chatname: String, sender:String, msg:String){
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Message> = connection.getDatabase(database_name).getCollection<Message>("_"+chatname)

    val message = Message(sender, msg)
    collection.insertOne(message)

    connection.close()
}

suspend fun loadMessages(chatname: String):List<messages.Message>{
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Message> = connection.getDatabase(database_name).getCollection<Message>("_"+chatname)

    val result = mutableListOf<messages.Message>()

    collection.find<Message>().collect{
        result.add(messages.Message(action = MessageAction.TEXT, message = it.msg, username = it.sender))
    }

    connection.close()
    return result
}

suspend fun getChatAdmin(chatname: String):String{
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Chat> = connection.getDatabase(database_name).getCollection<Chat>("chats")

    var result:String = ""
    val filter = Filters.eq("chatname", chatname)
    collection.find<Chat>(filter).collect{
        result = it.admin
    }

    connection.close()
    return result
}

suspend fun setChatPrivacy(action: MessageAction, chatname: String){
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Chat> = connection.getDatabase(database_name).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)
    val update = Updates.set("open", action==MessageAction.PUBLIC_CHAT)

    collection.updateOne(filter, update)


    connection.close()
}

suspend fun addUserToChat(chatname: String, username: String){
    val connection = MongoClient.create(url)
    val collection: MongoCollection<Chat> = connection.getDatabase(database_name).getCollection<Chat>("chats")

    val filter = Filters.eq("chatname", chatname)

    val chat:Chat = collection.find<Chat>(filter).first()
    chat.members.add(username)

    val update = Updates.set("members", chat.members)
    collection.updateOne(filter, update)

    connection.close()
}


suspend fun createCollection(){
    val connection = MongoClient.create(url)
    val collection: MongoCollection<User> = connection.getDatabase(database_name).getCollection<User>("chats")

    val indexOptions = IndexOptions().unique(true)
    collection.createIndex(Indexes.ascending("chatname"), indexOptions)

    connection.close()
}

fun deleteCollection(){

}

data class User(
    val username: String,
    val password: String
)

data class Chat(
    val chatname: String,
    val admin: String,
    val open:Boolean,
    val members: MutableList<String>
)

data class Message(
    val sender: String,
    val msg: String
)