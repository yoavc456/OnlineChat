package messages

import java.io.Serializable

data class Message(
    val stage: Stage = Stage.NULL,
    val action: MessageAction = MessageAction.NULL,
    val username: String = "",
    val receiverUsername: String = "",
    val password: String = "",
    val chatname: String = "",
    val success: Boolean = true,
    val message: String = "",
    val chatMessages: List<Message>? = null,
    val admin: String = "",
    var read: Boolean = false
) : Serializable
