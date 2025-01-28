package connection

import kotlinx.coroutines.flow.Flow
import messages.Message

interface ClientConnection {
    fun send(message: Message)
    fun receive():Flow<Message>
    fun isOpen():Boolean
    fun close()
}