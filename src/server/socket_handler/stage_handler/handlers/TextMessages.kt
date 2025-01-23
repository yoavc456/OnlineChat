package server.socket_handler.stage_handler.handlers

import messages.EntryMessage
import messages.MessageAction
import messages.TextMessage
import server.ServerDataManager
import server.socket_handler.stage_handler.StageData
import server.socket_handler.stage_handler.StageHandler
import server.socket_handler.stage_handler.StageName
import utils.addUserToChat
import utils.saveMessage
import utils.setChatPrivacy
import java.io.ObjectInputStream

class TextMessages : StageHandler() {
    val serverDataManager = ServerDataManager.getInstance()
    lateinit var stageData: StageData

    lateinit var msg: TextMessage
    var run: Boolean = true

    var result: StageData? = null

    override suspend fun start(stageData: StageData): StageData {
        this.stageData = stageData

        while (run) {
            try {
                waitingForClientInput()
                handleClientInput()
                if(result != null)
                    return result as StageData
            } catch (e: Exception) {
                clientDisconnected()
                return StageData(stageData.socket, StageName.CLOSE, "", "")
            }
        }

        return stageData
    }

    suspend fun sendTextMessage(msg: TextMessage) {
        val serverDataManager = ServerDataManager.getInstance()

        saveMessage(msg.chatName, msg.senderUserName, msg.message)
        for (userName in serverDataManager.CHATS.get(msg.chatName)!!) {
            if (!userName.equals(msg.senderUserName)) {
                serverDataManager.LOGGED_IN_SOCKETS.get(userName)?.let { serverDataManager.sendMessage(msg, it) }
            }
        }
    }

    private fun waitingForClientInput() {
        val input = ObjectInputStream(stageData.socket.getInputStream())
        msg = input.readObject() as TextMessage
    }

    private suspend fun handleClientInput(){
        if (msg.action == MessageAction.CLOSE) {
            serverDataManager.LOGGED_IN_SOCKETS.remove(msg.senderUserName)
            serverDataManager.CHATS.get(msg.chatName)?.remove(msg.senderUserName)
            stageData.socket.close()
            result = StageData(stageData.socket, StageName.CLOSE, "", "")
        } else if (msg.action == MessageAction.TEXT) {
            sendTextMessage(msg)
            println(msg.senderUserName)
        } else if (msg.action == MessageAction.PRIVATE_CHAT || msg.action == MessageAction.PUBLIC_CHAT) {
            setChatPrivacy(msg.action, msg.chatName)
        } else if (msg.action == MessageAction.ADD_USER_TO_CHAT) {
            addUserToChat(msg.chatName, msg.receiverUserName)
        }else if(msg.action == MessageAction.REGISTER){
            println("OUT1")
            serverDataManager.CHATS.get(msg.chatName)?.remove(msg.senderUserName)
            result = StageData(stageData.socket, StageName.CHAT_ENTRY, stageData.username, "")
            println("OUT2")
        }
    }

    private fun clientDisconnected(){
        serverDataManager.LOGGED_IN_SOCKETS.remove(stageData.username)
        serverDataManager.CHATS.get(stageData.chatname)?.remove(stageData.username)
        stageData.socket.close()
        run = false
        println("aaa")
    }
}