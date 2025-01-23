package server.socket_handler

import messages.Message
import messages.MessageAction
import server.ServerDataManager
import utils.addUserToChat
import utils.saveMessage
import utils.setChatPrivacy

suspend fun textMessagesHandler(msg:Message){
    if (msg.action == MessageAction.TEXT) {
        sendTextMessage(msg)
        println(msg.username)
    } else if (msg.action == MessageAction.PRIVATE_CHAT || msg.action == MessageAction.PUBLIC_CHAT) {
        setChatPrivacy(msg.action, msg.chatname)
    } else if (msg.action == MessageAction.ADD_USER_TO_CHAT) {
        addUserToChat(msg.chatname, msg.receiverUsername)
    }
}

private suspend fun sendTextMessage(msg: Message) {
    val serverDataManager = ServerDataManager.getInstance()

    saveMessage(msg.chatname, msg.username, msg.message)
    for (userName in serverDataManager.CHATS.get(msg.chatname)!!) {
        if (!userName.equals(msg.username)) {
            serverDataManager.LOGGED_IN_SOCKETS.get(userName)?.let { serverDataManager.sendMessage(msg, it) }
        }
    }
}