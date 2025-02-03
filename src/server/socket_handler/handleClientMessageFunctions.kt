package server.socket_handler

import messages.MessageAction
import messages.Stage
import messages.Stage.*
import messages.client_msg.ClientMessage
import messages.server_msg.ServerMessage
import server.ServerDataManager
import java.util.UUID

suspend fun userEntryStage(message: ClientMessage, uuid: UUID): Stage {
    when (message.message) {
        "l" -> return LOG_IN
        "r" -> return REGISTER
        "c" -> return CLOSE
    }

    return USER_ENTRY
}

suspend fun logInStage(message: ClientMessage, uuid: UUID): Stage {
    if (ServerDataManager.databaseManager.doesUserExist(message.username, message.password) &&
        ServerDataManager.USERNAMES.get(message.username) == null
    ) {
        ServerDataManager.USERNAMES.put(message.username, uuid)
        ServerDataManager.UUID_TO_CHAT.put(uuid, message.username)
        return CHAT_ENTRY
    }
    return USER_ENTRY
}

suspend fun registerStage(message: ClientMessage, uuid: UUID): Stage {
    if (!ServerDataManager.databaseManager.createUser(message.username, message.password))
        return USER_ENTRY


    ServerDataManager.USERNAMES.put(message.username, uuid)
    ServerDataManager.UUID_TO_CHAT.put(uuid, message.username)
    return CHAT_ENTRY
}

suspend fun chatEntryStage(message: ClientMessage, uuid: UUID): Stage {
    when (message.message) {
        "e" -> return ENTER_TO_CHAT
        "cr" -> return CREATE_CHAT
        "c" -> return CLOSE

        "o" -> {
            gettingOutOfUser(uuid)
            return USER_ENTRY
        }
    }

    return CHAT_ENTRY
}

suspend fun enterToChatStage(message: ClientMessage, uuid: UUID): Stage {
    val chatName = message.chatName
    val username: String = ServerDataManager.UUID_TO_USERNAME.get(uuid).toString()

    if (!ServerDataManager.databaseManager.enterChat(chatName, username))
        return CHAT_ENTRY

    if (ServerDataManager.CHATS.get(chatName) == null) {
        addChatToServer(username, chatName)
    } else {
        ServerDataManager.CHATS.get(chatName)!!.add(username)
    }

    ServerDataManager.UUID_TO_CHAT.put(uuid, chatName)

    return TEXT_MESSAGES
}

suspend fun createChatStage(message: ClientMessage, uuid: UUID): Stage {
    val chatName = message.chatName
    val username: String = ServerDataManager.UUID_TO_USERNAME.get(uuid).toString()

    if (!ServerDataManager.databaseManager.createChat(chatName, username))
        return CHAT_ENTRY

    addChatToServer(username, chatName)
    ServerDataManager.UUID_TO_CHAT.put(uuid, chatName)
    return TEXT_MESSAGES
}

suspend fun textMessagesStage(message: ClientMessage, uuid: UUID): Stage {
    val username: String = ServerDataManager.UUID_TO_USERNAME.get(uuid).toString()
    val chatName: String = ServerDataManager.UUID_TO_CHAT.get(uuid).toString()
    val admin = ServerDataManager.databaseManager.getChatAdmin(chatName) == username

    if (message.message == "//") {
        if (admin)
            return CHAT_MENU_ADMIN
        else
            return CHAT_MENU
    }

    sendTextMessage(username, chatName, message.message)
    return TEXT_MESSAGES
}

suspend fun textMessagesChatMenuStage(message: ClientMessage, uuid: UUID): Stage {
    when (message.message) {
        "c" -> return CLOSE
        "o" -> {
            gettingOutOfChat(uuid)
        }
    }

    return TEXT_MESSAGES
}

suspend fun textMessagesChatMenuAdminStage(message: ClientMessage, uuid: UUID): Stage {
    val chatName:String = ServerDataManager.UUID_TO_CHAT.get(uuid).toString()
    when (message.message) {
        "c" -> return CLOSE
        "o" -> {
            gettingOutOfChat(uuid)
        }

        "pu" -> {
            ServerDataManager.databaseManager.setChatPrivacy(MessageAction.PUBLIC_CHAT, chatName)
        }

        "pr" -> {
            ServerDataManager.databaseManager.setChatPrivacy(MessageAction.PRIVATE_CHAT, chatName)
        }

        "a" -> {
            ServerDataManager.databaseManager.addUserToChat(chatName, message.secondUsername)
        }
    }

    return TEXT_MESSAGES
}

private fun addChatToServer(username: String, chatName: String) {
    val chatUsers = mutableListOf<String>()
    chatUsers.add(username)
    ServerDataManager.CHATS.put(chatName, chatUsers)
}

private fun gettingOutOfUser(uuid: UUID) {
    ServerDataManager.USERNAMES.remove(ServerDataManager.UUID_TO_USERNAME.get(uuid))
    ServerDataManager.UUID_TO_USERNAME.remove(uuid)
}

private fun gettingOutOfChat(uuid: UUID){
    val username:String = ServerDataManager.UUID_TO_USERNAME.get(uuid).toString()
    val chatName = ServerDataManager.UUID_TO_CHAT.get(uuid)

    ServerDataManager.CHATS.get(chatName)?.remove(username)
    ServerDataManager.UUID_TO_CHAT.remove(uuid)
}

private fun sendTextMessage(sender: String, chatName: String, msg: String) {
    val message = ServerMessage(message = msg)
    for (u in ServerDataManager.CHATS.get(chatName)!!) {
        val uuid = ServerDataManager.USERNAMES.get(u)
        ServerDataManager.CONNECTIONS.get(uuid)?.send(message)
    }
}