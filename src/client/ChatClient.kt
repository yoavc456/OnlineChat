package client

import connection.socket_tcp.ConnectionTcp
import kotlinx.coroutines.*
import messages.*

class ChatClient() {

    val IP = "localhost"
    val PORT = 1234

    init {
        try {
            createConnection()
            startReceivingMessagesFromServer()
            connectedToServerLoop()
        } catch (e: Exception) {
            println("Disconnected From The Server!")
            ClientDataManager.stage = Stage.CLOSE
        } finally {
            ClientDataManager.closeClient()
        }
    }

    private fun createConnection(){
        try{
            ClientDataManager.serverConnection = ConnectionTcp(IP, PORT)
        }catch (e:Exception){
            println("Failed to connect to the server!")
            println("Press 'c' to close. Press any other button to try again.")

            if(readln() == "c")
                return

            createConnection()
        }
    }

    private fun startReceivingMessagesFromServer() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            receiveFromServerCoroutine()
        }
    }

    private fun connectedToServerLoop() = runBlocking {
        while (ClientDataManager.serverConnection.isOpen() && ClientDataManager.stage != Stage.CLOSE) {
            if (ClientDataManager.stage == Stage.USER_ENTRY)
                userEntryHandler()
            else if (ClientDataManager.stage == Stage.CHAT_ENTRY)
                chatEntryHandler()
            else if (ClientDataManager.stage == Stage.TEXT_MESSAGES)
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
            ClientDataManager.stage = Stage.CLOSE
        }

        if (result)
            ClientDataManager.stage = Stage.CHAT_ENTRY
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
            ClientDataManager.stage = Stage.CLOSE
        } else if (answer.equals("o")) {
            ClientDataManager.serverConnection.send(
                Message(
                    stage = Stage.CHAT_ENTRY, action = MessageAction.OUT_OF_USER,
                    chatname = ClientDataManager.chatName, username = ClientDataManager.userName
                )
            )
            ClientDataManager.stage = Stage.USER_ENTRY
            ClientDataManager.userName = ""

        }

        if (result)
            ClientDataManager.stage = Stage.TEXT_MESSAGES
    }

    private fun textMessagesHandler() {
        val msg = readln()
        handleTextInput(msg)
    }
}