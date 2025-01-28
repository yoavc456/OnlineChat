package server.socket_handler

import connection.ClientConnection
import server.database.MongoDBManager
import messages.Message
import messages.MessageAction
import server.ServerDataManager

private val serverDataManager = ServerDataManager.getInstance()
private val mongoDBManager = MongoDBManager.getInstance()

suspend fun userEntryHandler(clientConnection: ClientConnection, msg: Message) {
    if (msg.action == MessageAction.LOG_IN) {
        val result = logIn(clientConnection, msg)
        val entryAcceptMessage: String = if (result) "Logged In" else "Log In Failed"
        clientConnection.send(Message(success = result, message = entryAcceptMessage))
    }

    if (msg.action == MessageAction.REGISTER) {
        val result = register(clientConnection, msg)
        val entryAcceptMessage: String = if (result) "Register" else "Register Failed"
        clientConnection.send(Message(success = result, message = entryAcceptMessage))
    }

}

private suspend fun logIn(clientConnection: ClientConnection, msg: Message): Boolean {
    if (mongoDBManager.doesUserExist(msg.username, msg.password) && serverDataManager.LOGGED_IN_CLIENTS.get(msg.username) == null) {
        serverDataManager.CLIENT_CONNECTIONS.remove(clientConnection)
        serverDataManager.LOGGED_IN_CLIENTS.put(msg.username, clientConnection)
        return true
    }
    return false
}

private suspend fun register(clientConnection: ClientConnection, msg: Message): Boolean {
    if (!mongoDBManager.doesUsernameExist(msg.username)) {
        if (serverDataManager.LOGGED_IN_CLIENTS.get(msg.username) != null)
            return false
        mongoDBManager.createUser(msg.username, msg.password)
        serverDataManager.CLIENT_CONNECTIONS.remove(clientConnection)
        serverDataManager.LOGGED_IN_CLIENTS.put(msg.username, clientConnection)
        return true
    }

    return false
}