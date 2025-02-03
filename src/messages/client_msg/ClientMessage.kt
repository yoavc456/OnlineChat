package messages.client_msg

import messages.Message
import java.io.Serializable

data class ClientMessage(
    val username:String="",
    val secondUsername:String="",
    val password:String="",
    val chatName:String="",
    val message:String="",
): Message, Serializable
