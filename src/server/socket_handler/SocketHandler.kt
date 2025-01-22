package server.socket_handler

import kotlinx.coroutines.runBlocking
import server.ServerDataManager
import server.socket_handler.stage_handler.StageData
import server.socket_handler.stage_handler.StageHandler
import server.socket_handler.stage_handler.StageName
import server.socket_handler.stage_handler.handlers.ChatEntry
import server.socket_handler.stage_handler.handlers.TextMessages
import server.socket_handler.stage_handler.handlers.UserEntry
import java.net.Socket

class SocketHandler(private val socket: Socket){
    private val serverDataManager = ServerDataManager.getInstance()
    private val stages:MutableMap<StageName, StageHandler>
    private var stage = StageName.USER_ENTRY

    init {
        stages = mutableMapOf(
            StageName.USER_ENTRY to UserEntry(),
            StageName.CHAT_ENTRY to ChatEntry(),
            StageName.TEXT_MESSAGES to TextMessages()
        );
    }

    fun start()= runBlocking{
        var stageData = StageData(socket, StageName.USER_ENTRY, "", "")
        while (stageData.stage != StageName.CLOSE){
            stageData = stages.get(stageData.stage)!!.start(stageData)
        }
    }
}