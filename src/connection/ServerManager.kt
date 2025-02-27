package connection

import kotlinx.coroutines.flow.Flow

interface ServerManager {
    fun receiveNewConnection():Flow<Connection>
    fun isOpen():Boolean
    fun close()
}