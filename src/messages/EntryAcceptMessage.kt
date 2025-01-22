package messages

import java.io.Serializable

data class EntryAcceptMessage(val success:Boolean, val message:String="",
                              val chatMessages:List<TextMessage>?=null, val admin:String=""):Serializable
