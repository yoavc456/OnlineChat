package connection.socket_tcp

import connection.ClientConnection
import connection.ServerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.ServerSocket

class ServerManagerTcp(port: Int) :ServerManager {

    private val serverSocket: ServerSocket = ServerSocket(port)

    override fun receiveNewConnection(): Flow<ClientConnection> {
        return flow {
            while (isOpen()) {
                val socket = serverSocket.accept()
                val clientConnectionTcp = ClientConnectionTcp(socket)
                emit(clientConnectionTcp)
            }
        }
    }

    override fun isOpen(): Boolean {
        return !serverSocket.isClosed
    }

    override fun close() {
        serverSocket.close()
    }
}