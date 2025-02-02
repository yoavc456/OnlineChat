package server.socket_handler

import connection.Connection
import messages.Message
import messages.MessageAction
import server.ServerDataManager

suspend fun userEntryHandler(clientConnection: Connection, msg: Message) {
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

private suspend fun logIn(clientConnection: Connection, msg: Message): Boolean {
    if (ServerDataManager.databaseManager.doesUserExist(msg.username, msg.password) && ServerDataManager.LOGGED_IN_CLIENTS.get(msg.username) == null) {
        ServerDataManager.CLIENT_CONNECTIONS.remove(clientConnection)
        ServerDataManager.LOGGED_IN_CLIENTS.put(msg.username, clientConnection)
        return true
    }
    return false
}

private suspend fun register(clientConnection: Connection, msg: Message): Boolean {
    if (!ServerDataManager.databaseManager.doesUsernameExist(msg.username)) {
        if (ServerDataManager.LOGGED_IN_CLIENTS.get(msg.username) != null)
            return false
        ServerDataManager.databaseManager.createUser(msg.username, msg.password)
        ServerDataManager.CLIENT_CONNECTIONS.remove(clientConnection)
        ServerDataManager.LOGGED_IN_CLIENTS.put(msg.username, clientConnection)
        return true
    }

    return false
}