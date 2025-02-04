package server.client_handler

import messages.MessageAction
import messages.Stage
import messages.Stage.*
import messages.client_msg.ClientMessage
import messages.server_msg.ServerMessage
import server.ServerDataManager
import server.client_handler.client_menus.MenuOptions
import java.util.UUID

suspend fun userEntryStage(message: ClientMessage, uuid: UUID): Stage {
    when (message.message) {
        MenuOptions.LOG_IN.input -> return LOG_IN
        MenuOptions.REGISTER.input -> return REGISTER
        MenuOptions.CLOSE.input -> return CLOSE
    }

    return USER_ENTRY
}

suspend fun logInStage(message: ClientMessage, uuid: UUID): Stage {
    if (ServerDataManager.databaseManager.doesUserExist(message.username, message.password) &&
        ServerDataManager.USERNAMES.get(message.username) == null
    ) {
        ServerDataManager.USERNAMES.put(message.username, uuid)
        ServerDataManager.UUID_TO_USERNAME.put(uuid, message.username)
        return CHAT_ENTRY
    }
    return USER_ENTRY
}

suspend fun registerStage(message: ClientMessage, uuid: UUID): Stage {
    if (!ServerDataManager.databaseManager.createUser(message.username, message.password)){
        return USER_ENTRY
    }
    ServerDataManager.USERNAMES.put(message.username, uuid)
    ServerDataManager.UUID_TO_USERNAME.put(uuid, message.username)
    return CHAT_ENTRY
}

suspend fun chatEntryStage(message: ClientMessage, uuid: UUID): Stage {
    when (message.message) {
        MenuOptions.ENTER_CHAT.input -> return ENTER_TO_CHAT
        MenuOptions.CREATE_CHAT.input -> return CREATE_CHAT
        MenuOptions.CLOSE.input -> return CLOSE

        MenuOptions.LOG_OUT_USER.input -> {
            gettingOutOfUser(uuid)
            return USER_ENTRY
        }
    }

    return CHAT_ENTRY
}

suspend fun enterToChatStage(message: ClientMessage, uuid: UUID): Stage {
    val chatName = message.chatName
    val username: String = ServerDataManager.UUID_TO_USERNAME.get(uuid).toString()

    if (!ServerDataManager.databaseManager.enterChat(chatName, username)){
        return CHAT_ENTRY
    }

    if (ServerDataManager.CHATS.get(chatName) == null) {
        addChatToServer(username, chatName)
    } else {
        ServerDataManager.CHATS.get(chatName)!!.add(username)
    }

    ServerDataManager.UUID_TO_CHAT.put(uuid, chatName)

    return LOAD_CHAT
}

suspend fun createChatStage(message: ClientMessage, uuid: UUID): Stage {
    val chatName = message.chatName
    val username: String = ServerDataManager.UUID_TO_USERNAME.get(uuid).toString()

    if (!ServerDataManager.databaseManager.createChat(chatName, username)){
        return CHAT_ENTRY
    }

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
        MenuOptions.CLOSE.input -> return CLOSE
        MenuOptions.LOG_OUT_CHAT.input -> {
            gettingOutOfChat(uuid)
            return CHAT_ENTRY
        }
    }

    return TEXT_MESSAGES
}

suspend fun textMessagesChatMenuAdminStage(message: ClientMessage, uuid: UUID): Stage {
    val chatName:String = ServerDataManager.UUID_TO_CHAT.get(uuid).toString()
    when (message.message) {
        MenuOptions.CLOSE.input -> return CLOSE
        MenuOptions.LOG_OUT_CHAT.input -> {
            gettingOutOfChat(uuid)
            return CHAT_ENTRY
        }

        MenuOptions.CHAT_TO_PUBLIC.input -> {
            ServerDataManager.databaseManager.setChatPrivacy(MessageAction.PUBLIC_CHAT, chatName)
        }

        MenuOptions.CHAT_TO_PRIVATE.input -> {
            ServerDataManager.databaseManager.setChatPrivacy(MessageAction.PRIVATE_CHAT, chatName)
        }

        MenuOptions.ADD_USER_TO_CHAT.input -> {
            return ADD_USER_TO_CHAT
        }
    }

    return TEXT_MESSAGES
}

suspend fun addUserToChatStage(message: ClientMessage, uuid: UUID):Stage{
    val username = message.secondUsername
    val chatName:String = ServerDataManager.UUID_TO_CHAT.get(uuid).toString()

    ServerDataManager.databaseManager.addUserToChat(chatName, username)

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
    val chatName:String = ServerDataManager.UUID_TO_CHAT.get(uuid).toString()

    ServerDataManager.CHATS.get(chatName)?.remove(username)
    ServerDataManager.UUID_TO_CHAT.remove(uuid)
}

private suspend fun sendTextMessage(sender: String, chatName: String, msg: String) {
    val message = ServerMessage(message = "$sender: $msg")
    ServerDataManager.databaseManager.saveMessage(chatName, sender, msg)
    for (u in ServerDataManager.CHATS.get(chatName)!!) {
        if(u!=sender){
            val uuid = ServerDataManager.USERNAMES.get(u)
            ServerDataManager.CONNECTIONS.get(uuid)?.send(message)
        }
    }
}