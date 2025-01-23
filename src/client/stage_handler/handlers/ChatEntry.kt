package client.stage_handler.handlers

import client.ClientDataManager
import client.stage_handler.StageHandler
import client.stage_handler.StageName
import messages.EntryAcceptMessage
import messages.EntryMessage
import messages.MessageAction
import java.io.ObjectInputStream

class ChatEntry: StageHandler() {
    val clientDataManager = ClientDataManager.getInstance()

    override fun start(): StageName {
        var run: Boolean = true
        var answer = ""
        while (run) {
            println("e(Enter A Chat)/cr(Create A Chat)/c(Close)")
            answer = readln()

            if (answer.equals("e"))
                run = !enterChat()
            else if (answer.equals("cr")) {
                run = !createChat()
            } else if (answer.equals("c")) {
                run = false
                clientDataManager.closeClient()
            }
        }

        return StageName.TEXT_MESSAGES
    }

    private fun enterChat(): Boolean {
        print("Chat Name: ")
        val chatName: String = readln()

        clientDataManager.sendMsg(EntryMessage(MessageAction.ENTER_CHAT, clientDataManager.user_name, "", chatName))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            clientDataManager.chat_name = chatName

            for (m in msg.chatMessages!!) {
                clientDataManager.handleReceivedTextMessage(m)
            }
            clientDataManager.admin = msg.admin.equals(clientDataManager.user_name)
            return true
        }

        return false
    }

    private fun createChat(): Boolean {
        print("Chat Name: ")
        val chatName: String = readln()

        clientDataManager.sendMsg(EntryMessage(MessageAction.CREATE_CHAT, clientDataManager.user_name, "", chatName))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            clientDataManager.chat_name = chatName
            clientDataManager.admin = true
            return true
        }

        return false
    }
}