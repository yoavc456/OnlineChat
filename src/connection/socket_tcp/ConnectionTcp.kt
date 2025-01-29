package connection.socket_tcp

import connection.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import messages.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

open class ConnectionTcp() :Connection{

    private lateinit var socket:Socket

    constructor(socket:Socket) : this() {
        this.socket = socket
    }

    constructor(ip:String, port: Int) : this() {
        try {
            socket = Socket(ip, port)
        } catch (e: Exception) {
            throw Exception("Failed to connect to the server!")
        }
    }

    override fun send(message: Message) {
        if(socket.isClosed)
            return
        val output = ObjectOutputStream(socket.getOutputStream())
        output.writeObject(message)
    }

    override fun receive(): Flow<Message> {
        return flow {
            while (isOpen()) {
                try {
                    val input = ObjectInputStream(socket.getInputStream())
                    val msg = input.readObject() as Message
                    emit(msg)
                }catch (e:Exception){
                    close()
                }
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