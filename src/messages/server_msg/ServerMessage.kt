package messages.server_msg

import messages.Message
import messages.client_msg.ClientMessage
import messages.server_msg.MessageInstruction.*
import java.io.Serializable

data class ServerMessage(
    val createMessage: (() -> ClientMessage)? = null,
    val instruction: MessageInstruction = PRINT,
    val message: String = "",
    val chatMessages: List<ServerMessage>? = null
) : Message, Serializable