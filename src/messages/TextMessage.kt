package messages

import java.io.Serializable

data class TextMessage(val action: MessageAction, val message: String="",
                       val senderUserName: String="", val receiverUserName: String="", val chatName:String=""):Serializable
