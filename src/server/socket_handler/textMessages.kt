package server.socket_handler

import server.database.mongodb.MongoDBManager
import messages.Message
import messages.MessageAction
import server.ServerDataManager

suspend fun textMessagesHandler(msg: Message) {
    if (msg.action == MessageAction.TEXT) {
        sendTextMessage(msg)
        println(msg.username)
    } else if (msg.action == MessageAction.PRIVATE_CHAT || msg.action == MessageAction.PUBLIC_CHAT) {
        ServerDataManager.databaseManager.setChatPrivacy(msg.action, msg.chatname)
    } else if (msg.action == MessageAction.ADD_USER_TO_CHAT) {
        ServerDataManager.databaseManager.addUserToChat(msg.chatname, msg.receiverUsername)
    } else if (msg.action == MessageAction.OUT_OF_CHAT) {
        ServerDataManager.CHATS.get(msg.chatname)?.remove(msg.username)
    }
}

private suspend fun sendTextMessage(msg: Message) {
    ServerDataManager.databaseManager.saveMessage(msg.chatname, msg.username, msg.message)
    for (userName in ServerDataManager.CHATS.get(msg.chatname)!!) {
        if (!userName.equals(msg.username)) {
            ServerDataManager.LOGGED_IN_CLIENTS.get(userName)?.send(msg)
        }
    }
}