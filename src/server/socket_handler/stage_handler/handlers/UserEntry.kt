package server.socket_handler.stage_handler.handlers

import messages.EntryAcceptMessage
import messages.EntryMessage
import messages.MessageAction
import server.ServerDataManager
import server.socket_handler.stage_handler.StageData
import server.socket_handler.stage_handler.StageHandler
import server.socket_handler.stage_handler.StageName
import utils.createUser
import utils.isUserExist
import utils.isUsernameExist
import java.io.ObjectInputStream
import java.net.Socket

class UserEntry() : StageHandler() {

    val serverDataManager = ServerDataManager.getInstance()
    lateinit var stageData: StageData

    var run: Boolean = true
    var username: String = ""

    lateinit var msg: EntryMessage
    var result: StageData? = null

    override suspend fun start(stageData: StageData): StageData {
        this.stageData = stageData
        serverDataManager.SOCKETS.add(stageData.socket)

        while (run) {
            try {
                waitingForClientInput()
                handleClientInput()
                if (result != null)
                    return result as StageData
            } catch (e: Exception) {
                clientDisconnected()
                return StageData(stageData.socket, StageName.CLOSE, "", "")
            }

        }
        return StageData(stageData.socket, StageName.CHAT_ENTRY, username, "")
    }


    private suspend fun logIn(socket: Socket, msg: EntryMessage): Boolean {
        val serverDataManager = ServerDataManager.getInstance()
        if (isUserExist(msg.username, msg.password) && serverDataManager.LOGGED_IN_SOCKETS.get(msg.username) == null) {
            serverDataManager.SOCKETS.remove(socket)
            serverDataManager.LOGGED_IN_SOCKETS.put(msg.username, socket)
            return true
        }
        return false
    }

    private suspend fun register(socket: Socket, msg: EntryMessage): Boolean {
        val serverDataManager = ServerDataManager.getInstance()
        if (!isUsernameExist(msg.username)) {
            if (serverDataManager.LOGGED_IN_SOCKETS.get(msg.username) != null)
                return false
            createUser(msg.username, msg.password)
            serverDataManager.SOCKETS.remove(socket)
            serverDataManager.LOGGED_IN_SOCKETS.put(msg.username, socket)
            return true
        }

        return false
    }

    private fun waitingForClientInput() {
        val input = ObjectInputStream(stageData.socket.getInputStream())
        msg = input.readObject() as EntryMessage
        username = msg.username
    }

    private suspend fun handleClientInput() {
        if (msg.action == MessageAction.CLOSE) {
            serverDataManager.SOCKETS.remove(stageData.socket)
            stageData.socket.close()
            result = StageData(stageData.socket, StageName.CLOSE, "", "")
        }

        if (msg.action == MessageAction.LOG_IN) {
            run = !logIn(stageData.socket, msg)
            val entryAcceptMessage: String = if (!run) "Logged In" else "Log In Failed"
            serverDataManager.sendMessage(EntryAcceptMessage(!run, entryAcceptMessage), stageData.socket)
        }

        if (msg.action == MessageAction.REGISTER) {
            run = !register(stageData.socket, msg)
            val entryAcceptMessage: String = if (!run) "Register" else "Register Failed"
            serverDataManager.sendMessage(EntryAcceptMessage(!run, entryAcceptMessage), stageData.socket)
        }
    }

    private fun clientDisconnected() {
        serverDataManager.SOCKETS.remove(stageData.socket)
        stageData.socket.close()
        println("a")
    }
}