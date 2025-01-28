package server.socket_handler

import database.MongoDBManager
import messages.Message
import messages.MessageAction
import server.ServerDataManager

private val serverDataManager = ServerDataManager.getInstance()
private val mongoDBManager = MongoDBManager.getInstance()

suspend fun textMessagesHandler(msg: Message) {
    if (msg.action == MessageAction.TEXT) {
        sendTextMessage(msg)
        println(msg.username)
    } else if (msg.action == MessageAction.PRIVATE_CHAT || msg.action == MessageAction.PUBLIC_CHAT) {
        mongoDBManager.setChatPrivacy(msg.action, msg.chatname)
    } else if (msg.action == MessageAction.ADD_USER_TO_CHAT) {
        mongoDBManager.addUserToChat(msg.chatname, msg.receiverUsername)
    } else if (msg.action == MessageAction.OUT_OF_CHAT) {
        serverDataManager.CHATS.get(msg.chatname)?.remove(msg.username)
    }
}

private suspend fun sendTextMessage(msg: Message) {
    val serverDataManager = ServerDataManager.getInstance()

    mongoDBManager.saveMessage(msg.chatname, msg.username, msg.message)
    for (userName in serverDataManager.CHATS.get(msg.chatname)!!) {
        if (!userName.equals(msg.username)) {
            serverDataManager.LOGGED_IN_SOCKETS.get(userName)?.let { serverDataManager.sendMessage(msg, it) }
        }
    }
}