package server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import server.socket_handler.SocketHandler
import java.net.ServerSocket
import java.net.Socket

class ChatServer {

    private val SERVER_SOCKET: ServerSocket
    private val PORT: Int = 1234

    private val serverDataManager: ServerDataManager

    init {
        SERVER_SOCKET = ServerSocket(PORT)
        serverDataManager = ServerDataManager.getInstance()

        GlobalScope.launch {
            waitForNewConnectionCoroutine()
        }

        readln()

        serverDataManager.close()
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