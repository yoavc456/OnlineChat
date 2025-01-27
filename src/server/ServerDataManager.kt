package server

import java.io.ObjectOutputStream
import java.net.Socket

class ServerDataManager private constructor() {


    val SOCKETS: MutableList<Socket> = mutableListOf()
    val LOGGED_IN_SOCKETS: HashMap<String, Socket> = hashMapOf()
    val CHATS: HashMap<String, MutableList<String>> = HashMap()

    companion object {
        private val serverDataManager: ServerDataManager = ServerDataManager()

        fun getInstance(): ServerDataManager {
            return serverDataManager
        }
    }

    fun sendMessage(msg: Any, socket: Socket) {
        val output = ObjectOutputStream(socket.getOutputStream())
        output.writeObject(msg)
    }

    fun close() {
        for (s in SOCKETS) {
            s.close()
        }

        for (s in LOGGED_IN_SOCKETS) {
            s.value.close()
        }
    }
}