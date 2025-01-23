package client

import messages.*
import java.io.ObjectOutputStream
import java.net.Socket

class ClientDataManager private constructor() {

    var user_name: String = ""
    var chat_name: String = ""
    var admin: Boolean = false

    lateinit var SOCKET: Socket


    companion object{
        private val clientDataManager = ClientDataManager()
        fun getInstance():ClientDataManager{
            return clientDataManager
        }
    }

    fun sendMsg(msg: Any) {
        val output = ObjectOutputStream(SOCKET.getOutputStream())
        output.writeObject(msg)
    }

    fun closeClient() {
        sendMsg(Message(Stage.CLOSE, username = user_name, chatname = chat_name))
        SOCKET.close()
    }

    fun handleReceivedTextMessage(msg: Message) {
        if (msg.receiverUsername.equals(user_name))
            println("${msg.username} (Private Message): ${msg.message}")
        else
            println("${msg.username}: ${msg.message}")
    }
}