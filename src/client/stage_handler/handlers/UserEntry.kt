package client.stage_handler.handlers

import client.ClientDataManager
import client.stage_handler.StageHandler
import client.stage_handler.StageName
import messages.EntryAcceptMessage
import messages.EntryMessage
import messages.MessageAction
import java.io.ObjectInputStream

class UserEntry: StageHandler() {

    val clientDataManager = ClientDataManager.getInstance()

    override fun start(): StageName {
        var run = true
        var answer = ""
        while (run) {
            println("l(LogIn)/r(Register)/c(Close)")
            answer = readln()

            if (answer.equals("l"))
                run = !logIn()
            else if (answer.equals("r"))
                run = !register()
            else if (answer.equals("c")) {
                run = false
                clientDataManager.closeClient()
            }
        }

        return StageName.CHAT_ENTRY
    }

    private fun logIn(): Boolean {
        print("UserName: ")
        val userName: String = readln()
        print("Password: ")
        val password: String = readln()

        clientDataManager.sendMsg(EntryMessage(MessageAction.LOG_IN, userName, password))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            clientDataManager.user_name = userName
            return true
        }

        return false
    }

    private fun register(): Boolean {
        print("UserName: ")
        val userName: String = readln()
        print("Password: ")
        val password: String = readln()

        if (userName.length < 2 || userName.length > 15 || password.length < 2
            || userName.split(" ").size > 1 || password.split(" ").size > 1
        ) {
            println("\u001b[31m" + "Register Failed" + "\u001b[0m")
            return false
        }

        clientDataManager.sendMsg(EntryMessage(MessageAction.REGISTER, userName, password))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            clientDataManager.user_name = userName
            return true
        }

        return false
    }
}