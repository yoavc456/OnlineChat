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
import kotlin.math.log

open class ConnectionTcp() :Connection{

    private lateinit var socket:Socket
    private lateinit var output:ObjectOutputStream
    private lateinit var input:ObjectInputStream

    constructor(socket:Socket) : this() {
        this.socket = socket
        this.output = ObjectOutputStream(socket.getOutputStream())
        this.input = ObjectInputStream(socket.getInputStream())
    }

    constructor(ip:String, port: Int) : this() {
        try {
            socket = Socket(ip, port)
            this.output = ObjectOutputStream(socket.getOutputStream())
            this.input = ObjectInputStream(socket.getInputStream())
            println("checking = ${socket.inetAddress}")

        } catch (e: Exception) {
            throw Exception("Failed to connect to the server!")
        }
    }

    override fun send(message: Message) {
        if(socket.isClosed)
            return
        output.writeObject(message)
    }

    override fun receive(): Flow<Message> {
        return flow {
            while (isOpen()) {
                try {
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