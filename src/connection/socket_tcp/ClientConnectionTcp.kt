package connection.socket_tcp

import connection.ClientConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import messages.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ClientConnectionTcp(private val socket:Socket) :ClientConnection{

    override fun send(message: Message) {
        val output = ObjectOutputStream(socket.getOutputStream())
        output.writeObject(message)
    }

    override fun receive(): Flow<Message> {
        return flow {
            while (isOpen()) {
                val input = ObjectInputStream(socket.getInputStream())
                val msg = input.readObject() as Message
                emit(msg)
            }
        }
            .flowOn(Dispatchers.IO)
    }

    override fun isOpen(): Boolean {
        return !socket.isClosed
    }

    override fun close() {
        socket.close()
    }

}