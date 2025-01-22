package server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import messages.*
import server.socket_handler.SocketHandler
import utils.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

class ChatServer {

    private val SERVER_SOCKET: ServerSocket
    private val PORT: Int = 1234

    private val serverDataManager:ServerDataManager

    init {
        SERVER_SOCKET = ServerSocket(PORT)
        serverDataManager = ServerDataManager.getInstance()

        GlobalScope.launch {
            waitForNewConnectionCoroutine()
        }

        readln()

        SERVER_SOCKET.close()
    }

    fun waitForNewConnectionCoroutine() {
        while (!SERVER_SOCKET.isClosed()) {
            val socket = SERVER_SOCKET.accept()
            addConnection(socket)
        }
    }
    private fun addConnection(socket: Socket) {

        GlobalScope.launch {
            SocketHandler(socket).start()
        }
    }
}