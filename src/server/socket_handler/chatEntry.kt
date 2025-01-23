package server.socket_handler

import messages.EntryAcceptMessage
import messages.Message
import messages.MessageAction
import server.ServerDataManager
import utils.getChatAdmin
import utils.loadMessages
import java.net.Socket

private val serverDataManager = ServerDataManager.getInstance()

suspend fun chatEntryHandler(socket: Socket, msg:Message){
    if (msg.action == MessageAction.ENTER_CHAT) {
        val result = enterChat(msg)

        val entryAcceptMessage: String = if (result) "Entered Chat" else "Chat Does Not Exist"
        if (result)
            serverDataManager.sendMessage(
                Message(
                    success = true, message = entryAcceptMessage,
                    chatMessages = loadMessages(msg.chatname), admin = getChatAdmin(msg.chatname)
                ), socket
            )
        else
            serverDataManager.sendMessage(Message(success = false, message = entryAcceptMessage), socket)
    }

    if (msg.action == MessageAction.CREATE_CHAT) {
        val result = createChat(msg)
        val entryAcceptMessage: String = if (result) "Chat Created" else "Chat Does Not Created"
        serverDataManager.sendMessage(Message(success = result, message = entryAcceptMessage), socket)
    }
}

private suspend fun enterChat(msg: Message): Boolean {
    if (utils.enterChat(msg.chatname, msg.username)) {
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
    if (utils.createChat(msg.chatname, msg.username)) {
        val chatUsers = mutableListOf<String>()
        chatUsers.add(msg.username)
        serverDataManager.CHATS.put(msg.chatname, chatUsers)
        return true
    }

    return false
}