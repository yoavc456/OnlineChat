package server.socket_handler

import messages.Message
import messages.MessageAction
import server.ServerDataManager
import utils.createUser
import utils.doesUserExist
import utils.doesUsernameExist
import java.net.Socket

private val serverDataManager = ServerDataManager.getInstance()

suspend fun userEntryHandler(socket: Socket, msg: Message) {
    if (msg.action == MessageAction.LOG_IN) {
        val result = logIn(socket, msg)
        val entryAcceptMessage: String = if (result) "Logged In" else "Log In Failed"
        serverDataManager.sendMessage(Message(success = result, message = entryAcceptMessage), socket)
    }

    if (msg.action == MessageAction.REGISTER) {
        val result = register(socket, msg)
        val entryAcceptMessage: String = if (result) "Register" else "Register Failed"
        serverDataManager.sendMessage(Message(success = result, message = entryAcceptMessage), socket)
    }

}

private suspend fun logIn(socket: Socket, msg: Message): Boolean {
    val serverDataManager = ServerDataManager.getInstance()
    if (doesUserExist(msg.username, msg.password) && serverDataManager.LOGGED_IN_SOCKETS.get(msg.username) == null) {
        serverDataManager.SOCKETS.remove(socket)
        serverDataManager.LOGGED_IN_SOCKETS.put(msg.username, socket)
        return true
    }
    return false
}

private suspend fun register(socket: Socket, msg: Message): Boolean {
    val serverDataManager = ServerDataManager.getInstance()
    if (!doesUsernameExist(msg!!.username)) {
        if (serverDataManager.LOGGED_IN_SOCKETS.get(msg!!.username) != null)
            return false
        createUser(msg!!.username, msg!!.password)
        serverDataManager.SOCKETS.remove(socket)
        serverDataManager.LOGGED_IN_SOCKETS.put(msg!!.username, socket)
        return true
    }

    return false
}