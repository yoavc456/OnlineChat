package client.stage_handler.handlers

import client.ClientDataManager
import client.stage_handler.StageHandler
import client.stage_handler.StageName
import kotlinx.coroutines.*
import messages.MessageAction
import messages.TextMessage
import server.socket_handler.stage_handler.StageData
import java.io.ObjectInputStream

class TextMessages: StageHandler() {
    val clientDataManager = ClientDataManager.getInstance()
    var result: StageName? = null

    override fun start(): StageName {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            waitForTextInputCoroutine()
        }

        scope.launch {
            receiveFromServerCoroutine()
        }

        while (!clientDataManager.SOCKET.isClosed && result==null){

        }

        if(result!=null){
            scope.cancel()
            return result as StageName
        }

        return StageName.CLOSE
    }

    private fun waitForTextInputCoroutine() {
        while (!clientDataManager.SOCKET.isClosed && result==null) {
            val msg = readln()
            handleTextInput(msg)
        }
    }

    private fun receiveFromServerCoroutine() {
        while (!clientDataManager.SOCKET.isClosed) {
            try {
                val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
                val msg = input.readObject() as TextMessage
                clientDataManager.handleReceivedTextMessage(msg)
            } catch (e: Exception) {
                clientDataManager.SOCKET.close()
                println("Disconnected From The Server!")
            }
        }
    }

    private fun handleTextInput(msg: String) {
        if (msg.equals("//")) {
            if (clientDataManager.admin)
                println("c(Close)/pu(public)/pr(private)/a(add)")
            else
                println("c(Close)")
            val answer: String = readln()

            if (answer.equals("c"))
                clientDataManager.closeClient()
            else if (answer.equals("pu") && clientDataManager.admin)
                clientDataManager.sendMsg(TextMessage(MessageAction.PUBLIC_CHAT, chatName = clientDataManager.chat_name))
            else if (answer.equals("pr") && clientDataManager.admin)
                clientDataManager.sendMsg(TextMessage(MessageAction.PRIVATE_CHAT, chatName = clientDataManager.chat_name))
            else if (answer.equals("a") && clientDataManager.admin) {
                print("User Name: ")
                val username: String = readln()
                clientDataManager.sendMsg(TextMessage(MessageAction.ADD_USER_TO_CHAT, receiverUserName = username, chatName = clientDataManager.chat_name))
            }else if (answer.equals("out")){
                clientDataManager.sendMsg(TextMessage(MessageAction.REGISTER,
                    senderUserName = clientDataManager.user_name, chatName = clientDataManager.chat_name))
                result = StageName.CHAT_ENTRY
            }

            return
        }

        clientDataManager.sendMsg(TextMessage(MessageAction.TEXT, msg, clientDataManager.user_name, "", clientDataManager.chat_name))
    }

}