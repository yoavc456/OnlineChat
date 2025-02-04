package client

import client.ClientDataManager.serverConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messages.server_msg.MessageInstruction.*
import messages.server_msg.ServerMessage

val scope = CoroutineScope(Dispatchers.Default)

suspend fun receiveFromServerCoroutine() {
    serverConnection.receive().collect { msg ->
        val message = msg as ServerMessage
        when (message.instruction) {
            CLOSE -> ClientDataManager.closeClient()
            PRINT -> println(message.message)
            ACTIVE -> scope.launch { serverConnection.send(message.createMessage!!.invoke()) }
            LOAD_CHAT -> {
                for (m in message.chatMessages!!) {
                    val textMessage = m as ServerMessage
                    println(textMessage.message)
                }
            }
        }
    }
}