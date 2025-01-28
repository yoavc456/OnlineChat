package server

import connection.ClientConnection
import connection.ServerManager
import connection.socket_tcp.ServerManagerTcp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import server.socket_handler.SocketHandler

/**
 * Open the server.
 * Waiting for clients to connect to the server, and handle them.
 * Close the server.
 */

class ChatServer {

    private val PORT: Int = 1234

    private val serverManager:ServerManager = ServerManagerTcp(PORT)
    private val serverDataManager: ServerDataManager = ServerDataManager.getInstance()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        coroutineScope.launch {
            waitForNewConnectionCoroutine()
        }

        readln()

        serverDataManager.close()
        serverManager.close()
    }

    suspend fun waitForNewConnectionCoroutine() {
        serverManager.receiveNewConnection().collect{
            clientConnection ->

            coroutineScope.launch {
                SocketHandler(clientConnection).start()
            }
        }
    }

}