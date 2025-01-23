package client

import client.stage_handler.StageHandler
import client.stage_handler.StageName
import client.stage_handler.handlers.ChatEntry
import client.stage_handler.handlers.TextMessages
import client.stage_handler.handlers.UserEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import messages.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ChatClient {
    val clientDataManager = ClientDataManager.getInstance()
    private val IP_ADDRESS: String = "localhost"
    private val PORT: Int = 1234

    var stage = StageName.USER_ENTRY
    private val stages:MutableMap<StageName, StageHandler>

    init {
        stages = mutableMapOf(
            StageName.USER_ENTRY to UserEntry(),
            StageName.CHAT_ENTRY to ChatEntry(),
            StageName.TEXT_MESSAGES to TextMessages()
        )
    }
    constructor() {
        createConnection()
        try {
            while (stage != StageName.CLOSE && !clientDataManager.SOCKET.isClosed){
                stage = stages.get(stage)!!.start()
            }
        } catch (e: Exception) {
            clientDataManager.SOCKET.close()
            println("Disconnected From The Server!")
        }
    }

    private fun createConnection() {
        try {
            clientDataManager.SOCKET = Socket(IP_ADDRESS, PORT)
        } catch (e: Exception) {
            println("Didn't Connect To The Server!")
            return
        }
    }
}