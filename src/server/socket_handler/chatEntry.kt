package server.socket_handler

import connection.Connection
import server.database.MongoDBManager
import messages.Message
import messages.MessageAction
import server.ServerDataManager
import server.database.DatabaseManager

private val serverDataManager = ServerDataManager
private val mongoDBManager:DatabaseManager = MongoDBManager

suspend fun chatEntryHandler(clientConnection: Connection, msg: Message) {
    if (msg.action == MessageAction.ENTER_CHAT) {
        val result = enterChat(msg)

        val entryAcceptMessage: String = if (result) "Entered Chat" else "Chat Does Not Exist"
        if (result)
            clientConnection.send(Message(success = true, message = entryAcceptMessage,
                chatMessages = mongoDBManager.loadMessages(msg.chatname), admin = mongoDBManager.getChatAdmin(msg.chatname)
            ))
        else
            clientConnection.send(Message(success = false, message = entryAcceptMessage))
    }

    if (msg.action == MessageAction.CREATE_CHAT) {
        val result = createChat(msg)
        val entryAcceptMessage: String = if (result) "Chat Created" else "Chat Does Not Created"
        clientConnection.send(Message(success = result, message = entryAcceptMessage))
    }

    if (msg.action == MessageAction.OUT_OF_USER) {
        serverDataManager.LOGGED_IN_CLIENTS.remove(msg.username)
    }
}

private suspend fun enterChat(msg: Message): Boolean {
    if (mongoDBManager.enterChat(msg.chatname, msg.username)) {
        if (serverDataManager.CHATS.get(msg.chatname) == null) {
            val chatUsers = mutableListOf<String>()
            chatUsers.add(msg.username)
            serverDataManager.CHATS.put(msg.chatname, chatUsers)
        } else {
            serverDataManager.CHATS.get(msg.chatname)!!.add(msg.username)
        }
        return true
    }

    return false
}

private suspend fun createChat(msg: Message): Boolean {
    if (mongoDBManager.createChat(msg.chatname, msg.username)) {
        val chatUsers = mutableListOf<String>()
        chatUsers.add(msg.username)
        serverDataManager.CHATS.put(msg.chatname, chatUsers)
        return true
    }

    return false
}