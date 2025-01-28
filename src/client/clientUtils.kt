package client

import kotlinx.coroutines.delay
import messages.Message
import messages.MessageAction
import messages.Stage
import java.util.concurrent.TimeoutException

val clientDataManager = ClientDataManager.getInstance()
var lastMessage: Message = Message(read = true)
val maxWaitingTimeForMessage: Long = 3000

suspend fun clientLogIn(): Boolean {
    print("UserName: ")
    val userName: String = readln()
    print("Password: ")
    val password: String = readln()

    clientDataManager.serverConnection!!.send(
        Message(
            stage = Stage.USER_ENTRY,
            action = MessageAction.LOG_IN,
            username = userName,
            password = password
        )
    )

    val msg = getLastMessage(maxWaitingTimeForMessage)

    println(msg.message)
    if (msg.success) {
        clientDataManager.userName = userName
        return true
    }

    return false
}

suspend fun clientRegister(): Boolean {
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

    clientDataManager.serverConnection!!.send(
        Message(
            stage = Stage.USER_ENTRY,
            action = MessageAction.REGISTER,
            username = userName,
            password = password
        )
    )

    val msg = getLastMessage(maxWaitingTimeForMessage)

    println(msg.message)
    if (msg.success) {
        clientDataManager.userName = userName
        return true
    }

    return false
}

suspend fun clientEnterToChat(): Boolean {
    print("Chat Name: ")
    val chatName: String = readln()

    clientDataManager.serverConnection!!.send(
        Message(
            stage = Stage.CHAT_ENTRY, action = MessageAction.ENTER_CHAT,
            username = clientDataManager.userName, chatname = chatName
        )
    )

    val msg = getLastMessage(maxWaitingTimeForMessage)

    println(msg.message)
    if (msg.success) {
        clientDataManager.chatName = chatName

        println(msg.admin)
        for (m in msg.chatMessages!!) {
            clientDataManager.handleReceivedTextMessage(m)
        }
        clientDataManager.admin = msg.admin.equals(clientDataManager.userName)
        return true
    }

    return false
}

suspend fun clientCreateChat(): Boolean {
    print("Chat Name: ")
    val chatName: String = readln()

    clientDataManager.serverConnection!!.send(
        Message(
            stage = Stage.CHAT_ENTRY, action = MessageAction.CREATE_CHAT,
            username = clientDataManager.userName, chatname = chatName
        )
    )

    val msg = getLastMessage(maxWaitingTimeForMessage)

    println(msg.message)
    if (msg.success) {
        clientDataManager.chatName = chatName
        clientDataManager.admin = true
        return true
    }

    return false
}

suspend fun receiveFromServerCoroutine() {
    clientDataManager.serverConnection!!.receive().collect{
        msg ->
        if (clientDataManager.stage == Stage.TEXT_MESSAGES)
            clientDataManager.handleReceivedTextMessage(msg)
        else
            lastMessage = msg
    }
}

fun handleTextInput(msg: String) {
    if (msg.equals("//")) {
        if (clientDataManager.admin)
            println("c(Close)/o(out)/pu(public)/pr(private)/a(add)")
        else
            println("c(Close)/o(out)")

        val answer: String = readln()
        openTextInputMenu(answer)
        return
    }

    clientDataManager.serverConnection!!.send(
        Message(
            stage = Stage.TEXT_MESSAGES,
            action = MessageAction.TEXT,
            message = msg,
            username = clientDataManager.userName,
            chatname = clientDataManager.chatName
        )
    )
}

private fun openTextInputMenu(msg: String) {
    if (msg.equals("c"))
        clientDataManager.stage = Stage.CLOSE
    else if (msg.equals("o")) {
        clientDataManager.serverConnection!!.send(
            Message(
                stage = Stage.TEXT_MESSAGES, action = MessageAction.OUT_OF_CHAT,
                chatname = clientDataManager.chatName, username = clientDataManager.userName
            )
        )
        clientDataManager.stage = Stage.CHAT_ENTRY
        clientDataManager.chatName = ""
    } else if (msg.equals("pu") && clientDataManager.admin)
        clientDataManager.serverConnection!!.send(
            Message(
                stage = Stage.TEXT_MESSAGES,
                action = MessageAction.PUBLIC_CHAT,
                chatname = clientDataManager.chatName
            )
        )
    else if (msg.equals("pr") && clientDataManager.admin)
        clientDataManager.serverConnection!!.send(
            Message(
                stage = Stage.TEXT_MESSAGES,
                action = MessageAction.PRIVATE_CHAT,
                chatname = clientDataManager.chatName
            )
        )
    else if (msg.equals("a") && clientDataManager.admin) {
        print("User Name: ")
        val username: String = readln()
        clientDataManager.serverConnection!!.send(
            Message(
                stage = Stage.TEXT_MESSAGES,
                action = MessageAction.ADD_USER_TO_CHAT,
                receiverUsername = username,
                chatname = clientDataManager.chatName
            )
        )
    }
}

private suspend fun getLastMessage(maxWaitingTimeMillis: Long): Message {
    val time = System.currentTimeMillis()
    while (System.currentTimeMillis() - time < maxWaitingTimeMillis) {
        if (!lastMessage.read) {
            lastMessage.read = true
            return lastMessage
        }
        delay(100)
    }
    throw TimeoutException()
}