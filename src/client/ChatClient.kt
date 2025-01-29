package client

import connection.socket_tcp.ConnectionTcp
import kotlinx.coroutines.*
import messages.*

class ChatClient {

    val clientDataManager = ClientDataManager
    val IP = "localhost"
    val PORT = 1234

    constructor() {
        try {
            createConnection()
            startReceivingMessagesFromServer()
            connectedToServerLoop()
        } catch (e: Exception) {
            println("Disconnected From The Server!")
            clientDataManager.stage = Stage.CLOSE
        } finally {
            clientDataManager.closeClient()
        }
    }

    private fun createConnection(){
        clientDataManager.serverConnection = ConnectionTcp(IP, PORT)
    }

    private fun startReceivingMessagesFromServer() {
        GlobalScope.launch {
            receiveFromServerCoroutine()
        }
    }

    private fun connectedToServerLoop() = runBlocking {
        while (clientDataManager.serverConnection!!.isOpen() && clientDataManager.stage != Stage.CLOSE) {
            if (clientDataManager.stage == Stage.USER_ENTRY)
                userEntryHandler()
            else if (clientDataManager.stage == Stage.CHAT_ENTRY)
                chatEntryHandler()
            else if (clientDataManager.stage == Stage.TEXT_MESSAGES)
                textMessagesHandler()
        }
    }

    private suspend fun userEntryHandler() {
        println("l(LogIn)/r(Register)/c(Close)")
        val answer = readln()

        var result = false
        if (answer.equals("l"))
            result = clientLogIn()
        else if (answer.equals("r"))
            result = clientRegister()
        else if (answer.equals("c")) {
            clientDataManager.stage = Stage.CLOSE
        }

        if (result)
            clientDataManager.stage = Stage.CHAT_ENTRY
    }

    private suspend fun chatEntryHandler() {
        println("e(Enter A Chat)/cr(Create A Chat)/c(Close)/o(out)")
        var result = false
        val answer = readln()
        if (answer.equals("e"))
            result = clientEnterToChat()
        else if (answer.equals("cr")) {
            result = clientCreateChat()
        } else if (answer.equals("c")) {
            result = false
            clientDataManager.stage = Stage.CLOSE
        } else if (answer.equals("o")) {
            clientDataManager.serverConnection!!.send(
                Message(
                    stage = Stage.CHAT_ENTRY, action = MessageAction.OUT_OF_USER,
                    chatname = clientDataManager.chatName, username = clientDataManager.userName
                )
            )
            clientDataManager.stage = Stage.USER_ENTRY
            clientDataManager.userName = ""

        }

        if (result)
            clientDataManager.stage = Stage.TEXT_MESSAGES
    }

    private fun textMessagesHandler() {
        val msg = readln()
        handleTextInput(msg)
    }
}