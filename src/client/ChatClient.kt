package client

import kotlinx.coroutines.*
import messages.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ChatClient {
    val clientDataManager = ClientDataManager.getInstance()
    private val IP_ADDRESS: String = "localhost"
    private val PORT: Int = 1234

    var stage = Stage.USER_ENTRY

    constructor() {
        createConnection()
        try {
            while (stage != Stage.CLOSE){
                println(stage)
                if(stage == Stage.USER_ENTRY)
                    userEntryHandler()
                else if(stage == Stage.CHAT_ENTRY)
                    chatEntryHandler()
                else if(stage == Stage.TEXT_MESSAGES)
                    textMessagesHandler()
            }

        } catch (e: Exception) {
            clientDataManager.SOCKET.close()
            println("Disconnected From The Server!")
        }finally {
            println(clientDataManager.chat_name)
            println(clientDataManager.user_name)
            clientDataManager.SOCKET.close()
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

    private fun userEntryHandler(){
        println("l(LogIn)/r(Register)/c(Close)")
        var answer = readln()

        var result = false
        if (answer.equals("l"))
            result = logIn()
        else if (answer.equals("r"))
            result = !register()
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

    private fun logIn(): Boolean {
        print("UserName: ")
        val userName: String = readln()
        print("Password: ")
        val password: String = readln()

        clientDataManager.sendMsg(Message(stage = Stage.USER_ENTRY, action = MessageAction.LOG_IN, username = userName, password=password))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as Message

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

        clientDataManager.sendMsg(Message(stage=Stage.USER_ENTRY, action = MessageAction.REGISTER, username = userName, password = password))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as Message

        println(msg.message)
        if (msg.success) {
            clientDataManager.user_name = userName
            return true
        }

        return false
    }

    private fun enterChat(): Boolean {
        print("Chat Name: ")
        val chatName: String = readln()

        clientDataManager.sendMsg(Message(stage=Stage.CHAT_ENTRY, action = MessageAction.ENTER_CHAT,
            username = clientDataManager.user_name, chatname = chatName))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as Message

        println(msg.message)
        if (msg.success) {
            clientDataManager.chat_name = chatName

            for (m in msg.chatMessages!!) {
//                clientDataManager.handleReceivedTextMessage(m)
                println(m.message)
            }
            clientDataManager.admin = msg.admin.equals(clientDataManager.user_name)
            return true
        }

        return false
    }

    private fun createChat(): Boolean {
        print("Chat Name: ")
        val chatName: String = readln()

        clientDataManager.sendMsg(Message(stage = Stage.CHAT_ENTRY, action = MessageAction.CREATE_CHAT,
            username = clientDataManager.user_name, chatname = chatName))

        val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
        val msg = input.readObject() as Message

        println(msg.message)
        if (msg.success) {
            clientDataManager.chat_name = chatName
            clientDataManager.admin = true
            return true
        }

        return false
    }

    private fun waitForTextInputCoroutine() {
        while (!clientDataManager.SOCKET.isClosed) {
            val msg = readln()
            handleTextInput(msg)
        }
    }

    private fun receiveFromServerCoroutine() {
        while (!clientDataManager.SOCKET.isClosed) {
            try {
                val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
                val msg = input.readObject() as Message
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
                clientDataManager.sendMsg(Message(stage = Stage.TEXT_MESSAGES, action = MessageAction.PUBLIC_CHAT, chatname = clientDataManager.chat_name))
            else if (answer.equals("pr") && clientDataManager.admin)
                clientDataManager.sendMsg(Message(stage = Stage.TEXT_MESSAGES, action = MessageAction.PRIVATE_CHAT, chatname = clientDataManager.chat_name))
            else if (answer.equals("a") && clientDataManager.admin) {
                print("User Name: ")
                val username: String = readln()
                clientDataManager.sendMsg(Message(stage = Stage.TEXT_MESSAGES, action = MessageAction.ADD_USER_TO_CHAT, receiverUsername = username, chatname = clientDataManager.chat_name))
            }

            return
        }

        clientDataManager.sendMsg(Message(stage=Stage.TEXT_MESSAGES, action = MessageAction.TEXT, message = msg, username = clientDataManager.user_name, chatname = clientDataManager.chat_name))
    }
}