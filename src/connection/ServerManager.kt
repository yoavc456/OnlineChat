package connection

import kotlinx.coroutines.flow.Flow

interface ServerManager {
    fun receiveNewConnection():Flow<ClientConnection>
    fun isOpen():Boolean
    fun close()
}