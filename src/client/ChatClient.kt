package client

import kotlinx.coroutines.*
import messages.*
import java.net.Socket

class ChatClient {
    val clientDataManager = ClientDataManager.getInstance()
    private val IP_ADDRESS: String = "localhost"
    private val PORT: Int = 1234

    var stage = Stage.USER_ENTRY

    constructor() {
        try {
            createConnection()
            chatIsOpen()
        } catch (e: Exception) {
            clientDataManager.SOCKET.close()
            println("Disconnected From The Server!")
        }finally {
            close()
        }
    }

    private fun createConnection() {
        try {
            clientDataManager.SOCKET = Socket(IP_ADDRESS, PORT)
        } catch (e: Exception) {
            println("Didn't Connect To The Server!")
            return
        }
    }

    private fun chatIsOpen(){
        while (stage != Stage.CLOSE){
            if(stage == Stage.USER_ENTRY)
                userEntryHandler()
            else if(stage == Stage.CHAT_ENTRY)
                chatEntryHandler()
            else if(stage == Stage.TEXT_MESSAGES)
                textMessagesHandler()
        }
    }

    private fun userEntryHandler(){
        println("l(LogIn)/r(Register)/c(Close)")
        var answer = readln()

        var result = false
        if (answer.equals("l"))
            result = logIn()
        else if (answer.equals("r"))
            result = register()
        else if (answer.equals("c")) {
            stage = Stage.CLOSE
            clientDataManager.closeClient()
        }

        if(result)
            stage = Stage.CHAT_ENTRY
    }

    private fun chatEntryHandler(){
        println("e(Enter A Chat)/cr(Create A Chat)/c(Close)")
        var result = false
        val answer = readln()
        if (answer.equals("e"))
            result = enterChat()
        else if (answer.equals("cr")) {
            result = createChat()
        } else if (answer.equals("c")) {
            result = false
            clientDataManager.closeClient()
        }

        if(result)
            stage = Stage.TEXT_MESSAGES
    }

    private fun textMessagesHandler(){
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            waitForTextInputCoroutine()
        }

        scope.launch {
            receiveFromServerCoroutine()
        }

        while (!clientDataManager.SOCKET.isClosed){

        }
        stage = Stage.CLOSE
    }

    private fun close(){
        println(clientDataManager.chat_name)
        println(clientDataManager.user_name)
        clientDataManager.SOCKET.close()
    }
}