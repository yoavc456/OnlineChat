package client

import messages.*
import java.io.ObjectOutputStream
import java.net.Socket

class ClientDataManager private constructor() {

    var userName: String = ""
    var chatName: String = ""
    var admin: Boolean = false

    var socket: Socket? = null
    var stage = Stage.USER_ENTRY


    companion object {
        private val clientDataManager = ClientDataManager()
        fun getInstance(): ClientDataManager {
            return clientDataManager
        }
    }

    fun sendMsg(msg: Any) {
        if (socket!!.isClosed)
            return
        val output = ObjectOutputStream(socket!!.getOutputStream())
        output.writeObject(msg)
    }

    fun closeClient() {
        if (socket == null)
            return

        sendMsg(Message(Stage.CLOSE, username = userName, chatname = chatName))
        stage = Stage.CLOSE
        socket!!.close()

        println("CLOSE")
    }

    fun handleReceivedTextMessage(msg: Message) {
        if (msg.receiverUsername.equals(userName))
            println("${msg.username} (Private Message): ${msg.message}")
        else
            println("${msg.username}: ${msg.message}")
    }
}