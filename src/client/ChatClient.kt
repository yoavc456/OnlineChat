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
            chatIsOpen()
        } catch (e: Exception) {
//            clientDataManager.SOCKET.close()
            println("Disconnected From The Server!")
        }finally {
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

    private fun chatIsOpen()= runBlocking{
        GlobalScope.launch {
            receiveFromServerCoroutine()
        }
        while (clientDataManager.stage != Stage.CLOSE){
            if(clientDataManager.stage == Stage.USER_ENTRY)
                userEntryHandler()
            else if(clientDataManager.stage == Stage.CHAT_ENTRY)
                chatEntryHandler()
            else if(clientDataManager.stage == Stage.TEXT_MESSAGES)
                textMessagesHandler()
        }
    }

    private suspend fun userEntryHandler(){
        println("l(LogIn)/r(Register)/c(Close)")
        var answer = readln()

        var result = false
        if (answer.equals("l"))
            result = logIn()
        else if (answer.equals("r"))
            result = register()
        else if (answer.equals("c")) {
            clientDataManager.stage = Stage.CLOSE
            clientDataManager.closeClient()
        }

        if(result)
            clientDataManager.stage = Stage.CHAT_ENTRY
    }

    private suspend fun chatEntryHandler(){
        println("e(Enter A Chat)/cr(Create A Chat)/c(Close)")
        var result = false
        val answer = readln()
        if (answer.equals("e"))
            result = enterChat()
        else if (answer.equals("cr")) {
            result = createChat()
        } else if (answer.equals("c")) {
            result = false
            clientDataManager.stage = Stage.CLOSE
            clientDataManager.closeClient()
        }

        if(result)
            clientDataManager.stage = Stage.TEXT_MESSAGES
    }

    private fun textMessagesHandler(){
//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            waitForTextInputCoroutine()
//        }
//
//        while (!clientDataManager.SOCKET.isClosed){
//
//        }
//
//        clientDataManager.closeClient()
//        clientDataManager.stage = Stage.CLOSE

        waitForTextInputCoroutine()
    }
}