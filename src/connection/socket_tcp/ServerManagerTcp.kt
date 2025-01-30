package connection.socket_tcp

import connection.Connection
import connection.ServerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.ServerSocket

class ServerManagerTcp(port: Int) :ServerManager {

    private val serverSocket: ServerSocket = ServerSocket(port)

    override fun receiveNewConnection(): Flow<Connection> {
        return flow {
            while (isOpen()) {
                val socket = serverSocket.accept()
                println(socket.port)
                val clientConnectionTcp = ConnectionTcp(socket)
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