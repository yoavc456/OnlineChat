package server.socket_handler.stage_handler.handlers

import messages.EntryAcceptMessage
import messages.EntryMessage
import messages.MessageAction
import server.ServerDataManager
import server.socket_handler.stage_handler.StageData
import server.socket_handler.stage_handler.StageHandler
import server.socket_handler.stage_handler.StageName
import utils.getChatAdmin
import utils.loadMessages
import java.io.ObjectInputStream

class ChatEntry : StageHandler() {

    override suspend fun start(stageData: StageData): StageData {
        val serverDataManager = ServerDataManager.getInstance()

        var run: Boolean = true
        var chatname: String = ""
        while (run) {
            try {
                val serverDataManager = ServerDataManager.getInstance()

                val input = ObjectInputStream(stageData.socket.getInputStream())
                val msg = input.readObject() as EntryMessage
                chatname = msg.chatname

                if (msg.action == MessageAction.CLOSE) {
                    serverDataManager.LOGGED_IN_SOCKETS.remove(msg.username)
                    stageData.socket.close()
                    return StageData(stageData.socket, StageName.CLOSE, "", "")
                }

                if (msg.action == MessageAction.ENTER_CHAT) {
                    run = !enterChat(msg)

                    val entryAcceptMessage: String = if (!run) "Entered Chat" else "Chat Does Not Exist"
                    if (!run)
                        serverDataManager.sendMessage(
                            EntryAcceptMessage(
                                true, entryAcceptMessage,
                                loadMessages(msg.chatname), getChatAdmin(msg.chatname)
                            ), stageData.socket
                        )
                    else
                        serverDataManager.sendMessage(EntryAcceptMessage(false, entryAcceptMessage), stageData.socket)
                }

                if (msg.action == MessageAction.CREATE_CHAT) {
                    run = !createChat(msg)
                    val entryAcceptMessage: String = if (!run) "Chat Created" else "Chat Does Not Created"
                    serverDataManager.sendMessage(EntryAcceptMessage(!run, entryAcceptMessage), stageData.socket)
                }
            } catch (e: Exception) {
                serverDataManager.LOGGED_IN_SOCKETS.remove(stageData.username)
                stageData.socket.close()
                println("aa")
                return StageData(stageData.socket, StageName.CLOSE, "", "")
            }

        }
        return StageData(stageData.socket, StageName.TEXT_MESSAGES, stageData.username, chatname)
    }

    private suspend fun enterChat(msg: EntryMessage): Boolean {
        val serverDataManager = ServerDataManager.getInstance()

        if (utils.enterChat(msg.chatname, msg.username)) {
            if (serverDataManager.CHATS.get(msg.chatname) == null) {
                val chatUsers = mutableListOf<String>()
                chatUsers.add(msg.username)
                serverDataManager.CHATS.put(msg.chatname, chatUsers)
            } else {
                serverDataManager.CHATS.get(msg.chatname)!!.add(msg.username)
            }
            return true
        }

        return false
    }

    private suspend fun createChat(msg: EntryMessage): Boolean {
        val serverDataManager = ServerDataManager.getInstance()

        if (utils.createChat(msg.chatname, msg.username)) {
            val chatUsers = mutableListOf<String>()
            chatUsers.add(msg.username)
            serverDataManager.CHATS.put(msg.chatname, chatUsers)
            return true
        }

        return false
    }
}