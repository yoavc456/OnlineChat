package client

import connection.Connection
import messages.*

object ClientDataManager{

    var userName: String = ""
    var chatName: String = ""
    var admin: Boolean = false
    var stage = Stage.USER_ENTRY

    lateinit var serverConnection:Connection

    fun closeClient() {
        if(!::serverConnection.isInitialized)
            return

        serverConnection.send(Message(Stage.CLOSE, username = userName, chatname = chatName))
        stage = Stage.CLOSE
        serverConnection.close()

        println("CLOSE")
    }

    fun handleReceivedTextMessage(msg: Message) {
        if (msg.receiverUsername.equals(userName))
            println("${msg.username} (Private Message): ${msg.message}")
        else
            println("${msg.username}: ${msg.message}")
    }
}