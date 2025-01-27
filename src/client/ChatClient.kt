package client

import kotlinx.coroutines.*
import messages.*
import java.net.Socket

class ChatClient {
    val clientDataManager = ClientDataManager.getInstance()

    private val IP_ADDRESS: String = "localhost"
    private val PORT: Int = 1234

    constructor() {
        try {
            createConnection()
            if (clientDataManager.SOCKET == null)
                return
            startReceivingMessagesFromServer()
            connectedToServerLoop()
        } catch (e: Exception) {
            println("Disconnected From The Server!")
            clientDataManager.stage = Stage.CLOSE
        } finally {
            clientDataManager.closeClient()
        }
    }

    private fun createConnection() {
        try {
            clientDataManager.SOCKET = Socket(IP_ADDRESS, PORT)
        } catch (e: Exception) {
            println("Didn't Connect To The Server!")
            clientDataManager.stage = Stage.CLOSE
        }
    }

    private fun startReceivingMessagesFromServer() {
        GlobalScope.launch {
            receiveFromServerCoroutine()
        }
    }

    private fun connectedToServerLoop() = runBlocking {
        while (!clientDataManager.SOCKET!!.isClosed && clientDataManager.stage != Stage.CLOSE) {
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
            clientDataManager.sendMsg(
                Message(
                    stage = Stage.CHAT_ENTRY, action = MessageAction.OUT_OF_USER,
                    chatname = clientDataManager.chat_name, username = clientDataManager.user_name
                )
            )
            clientDataManager.stage = Stage.USER_ENTRY
            clientDataManager.user_name = ""

        }

        if (result)
            clientDataManager.stage = Stage.TEXT_MESSAGES
    }

    private fun textMessagesHandler() {
        val msg = readln()
        handleTextInput(msg)
    }
}