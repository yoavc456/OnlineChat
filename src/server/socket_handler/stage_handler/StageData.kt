package server.socket_handler.stage_handler

import java.net.Socket

data class StageData(val socket:Socket, val stage: StageName, val username:String, val chatname:String)
