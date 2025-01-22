package client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import messages.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ChatClient {

    private val IP_ADDRESS:String = "localhost"
    private val PORT:Int = 1234

    private lateinit var SOCKET: Socket
    private var user_name: String = ""
    private var chat_name: String = ""
    private var admin: Boolean = false

    constructor() {
        createConnection()

        try {
            userEntry()
            if (!user_name.equals(""))
                chatEntry()

            GlobalScope.launch {
                waitForTextInputCoroutine()
            }

            GlobalScope.launch {
                receiveFromServerCoroutine()
            }

            while (!SOCKET.isClosed) {
//                Thread.sleep(500)
            }
        } catch (e: Exception) {
            SOCKET.close()
            println("Disconnected From The Server!")
        }
    }

    private fun waitForTextInputCoroutine() {
        while (!SOCKET.isClosed) {
            val msg = readln()
            handleTextInput(msg)
        }
    }

    private fun receiveFromServerCoroutine() {
        while (!SOCKET.isClosed) {
            try {
                val input = ObjectInputStream(SOCKET.getInputStream())
                val msg = input.readObject() as TextMessage
                handleReceivedTextMessage(msg)
            } catch (e: Exception) {
                SOCKET.close()
                println("Disconnected From The Server!")
            }
        }
    }

    private fun createConnection(){
        try {
            SOCKET = Socket(IP_ADDRESS, PORT)
        } catch (e: Exception) {
            println("Didn't Connect To The Server!")
            return
        }
    }

    private fun sendMsg(msg: Any) {
        val output = ObjectOutputStream(SOCKET.getOutputStream())
        output.writeObject(msg)
    }

    private fun handleReceivedTextMessage(msg: TextMessage) {
        if (msg.receiverUserName.equals(user_name))
            println("${msg.senderUserName} (Private Message): ${msg.message}")
        else
            println("${msg.senderUserName}: ${msg.message}")
    }

    private fun userEntry() {
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
                closeClient()
            }
        }
    }

    private fun chatEntry() {
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
                closeClient()
            }
        }
    }

    private fun logIn(): Boolean {
        print("UserName: ")
        val userName: String = readln()
        print("Password: ")
        val password: String = readln()

        sendMsg(EntryMessage(MessageAction.LOG_IN, userName, password))

        val input = ObjectInputStream(SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            user_name = userName
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

        sendMsg(EntryMessage(MessageAction.REGISTER, userName, password))

        val input = ObjectInputStream(SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            user_name = userName
            return true
        }

        return false
    }

    private fun enterChat(): Boolean {
        print("Chat Name: ")
        val chatName: String = readln()

        sendMsg(EntryMessage(MessageAction.ENTER_CHAT, user_name, "", chatName))

        val input = ObjectInputStream(SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            chat_name = chatName

            for (m in msg.chatMessages!!) {
                handleReceivedTextMessage(m)
            }
            this.admin = msg.admin.equals(user_name)
            return true
        }

        return false
    }

    private fun createChat(): Boolean {
        print("Chat Name: ")
        val chatName: String = readln()

        sendMsg(EntryMessage(MessageAction.CREATE_CHAT, user_name, "", chatName))

        val input = ObjectInputStream(SOCKET.getInputStream())
        val msg = input.readObject() as EntryAcceptMessage

        println(msg.message)
        if (msg.success) {
            chat_name = chatName
            admin = true
            return true
        }

        return false
    }

    private fun handleTextInput(msg: String) {
        if (msg.equals("//")) {
            if (admin)
                println("c(Close)/pu(public)/pr(private)/a(add)")
            else
                println("c(Close)")
            val answer: String = readln()

            if (answer.equals("c"))
                closeClient()
            else if (answer.equals("pu") && admin)
                sendMsg(TextMessage(MessageAction.PUBLIC_CHAT, chatName = chat_name))
            else if (answer.equals("pr") && admin)
                sendMsg(TextMessage(MessageAction.PRIVATE_CHAT, chatName = chat_name))
            else if (answer.equals("a") && admin) {
                print("User Name: ")
                val username: String = readln()
                sendMsg(TextMessage(MessageAction.ADD_USER_TO_CHAT, receiverUserName = username, chatName = chat_name))
            }

            return
        }

        sendMsg(TextMessage(MessageAction.TEXT, msg, user_name, "", chat_name))
    }

    private fun closeClient() {
        if (user_name.equals("") || chat_name.equals(""))
            sendMsg(EntryMessage(MessageAction.CLOSE, user_name))
        else
            sendMsg(TextMessage(MessageAction.CLOSE, "", user_name, "", chat_name))

        SOCKET.close()
    }
}