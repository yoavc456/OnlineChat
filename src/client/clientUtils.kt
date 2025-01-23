package client

import messages.Message
import messages.MessageAction
import messages.Stage
import java.io.ObjectInputStream

val clientDataManager = ClientDataManager.getInstance()

fun logIn(): Boolean {
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

fun register(): Boolean {
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

fun enterChat(): Boolean {
    print("Chat Name: ")
    val chatName: String = readln()

    clientDataManager.sendMsg(Message(stage=Stage.CHAT_ENTRY, action = MessageAction.ENTER_CHAT,
        username = clientDataManager.user_name, chatname = chatName))

    val input = ObjectInputStream(clientDataManager.SOCKET.getInputStream())
    val msg = input.readObject() as Message

    println(msg.message)
    if (msg.success) {
        clientDataManager.chat_name = chatName

        println(msg.admin)
        for (m in msg.chatMessages!!) {
                clientDataManager.handleReceivedTextMessage(m)
        }
        clientDataManager.admin = msg.admin.equals(clientDataManager.user_name)
        println(chatName)
        return true
    }

    return false
}

fun createChat(): Boolean {
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

fun waitForTextInputCoroutine() {
    while (!clientDataManager.SOCKET.isClosed) {
        val msg = readln()
        handleTextInput(msg)
    }
}

fun receiveFromServerCoroutine() {
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

fun handleTextInput(msg: String) {
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