package server.socket_handler.stage_handler.handlers

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

class TextMessages: StageHandler() {
    override suspend fun start(stageData: StageData): StageData {
        val serverDataManager = ServerDataManager.getInstance()

        var run: Boolean = true

        while (run) {
            try {
                val input = ObjectInputStream(stageData.socket.getInputStream())
                val msg = input.readObject() as TextMessage
                if (msg.action == MessageAction.CLOSE) {
                    serverDataManager.LOGGED_IN_SOCKETS.remove(msg.senderUserName)
                    serverDataManager.CHATS.get(msg.chatName)?.remove(msg.senderUserName)
                    stageData.socket.close()
                    return StageData(stageData.socket, StageName.CLOSE, "", "")
                } else if (msg.action == MessageAction.TEXT) {
                    sendTextMessage(msg)
                    println(msg.senderUserName)
                } else if (msg.action == MessageAction.PRIVATE_CHAT || msg.action == MessageAction.PUBLIC_CHAT) {
                    setChatPrivacy(msg.action, msg.chatName)
                } else if (msg.action == MessageAction.ADD_USER_TO_CHAT) {
                    addUserToChat(msg.chatName, msg.receiverUserName)
                }
            } catch (e: Exception) {
                serverDataManager.LOGGED_IN_SOCKETS.remove(stageData.username)
                serverDataManager.CHATS.get(stageData.chatname)?.remove(stageData.username)
                stageData.socket.close()
                run = false
                println("aaa")
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
}