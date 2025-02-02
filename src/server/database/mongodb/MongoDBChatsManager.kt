package server.database.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import messages.MessageAction
import server.database.DatabaseChatsManager

class MongoDBChatsManager(database:MongoDatabase):DatabaseChatsManager {
    private val chatsCollection = database.getCollection<Chat>("chats")

    override suspend fun doesChatExist(chatname: String): Boolean {
        val filter = Filters.eq(Chat::chatname.name, chatname)
        return chatsCollection.find<Chat>(filter).firstOrNull() != null
    }

    override suspend fun createChat(chatname: String, adming: String): Boolean {
        if (MongoDBManager.doesChatExist(chatname))
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
}