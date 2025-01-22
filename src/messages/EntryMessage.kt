package messages

import java.io.Serializable

data class EntryMessage(val action: MessageAction, val username:String="", val password:String="", val chatname:String=""):Serializable
