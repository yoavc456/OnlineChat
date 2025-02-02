package client

import kotlinx.coroutines.channels.Channel
import messages.Message
import messages.MessageAction
import messages.Stage

var serverMessage = Channel<Message>(1)

suspend fun clientLogIn(): Boolean {
    print("UserName: ")
    val userName: String = readln()
    print("Password: ")
    val password: String = readln()

    ClientDataManager.serverConnection.send(
        Message(
            stage = Stage.USER_ENTRY,
            action = MessageAction.LOG_IN,
            username = userName,
            password = password
        )
    )

    val msg = serverMessage.receive()

    println(msg.message)
    if (msg.success) {
        ClientDataManager.userName = userName
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

    ClientDataManager.serverConnection.send(
        Message(
            stage = Stage.USER_ENTRY,
            action = MessageAction.REGISTER,
            username = userName,
            password = password
        )
    )

    val msg = serverMessage.receive()

    println(msg.message)
    if (msg.success) {
        ClientDataManager.userName = userName
        return true
    }

    return false
}

suspend fun clientEnterToChat(): Boolean {
    print("Chat Name: ")
    val chatName: String = readln()

    ClientDataManager.serverConnection.send(
        Message(
            stage = Stage.CHAT_ENTRY, action = MessageAction.ENTER_CHAT,
            username = ClientDataManager.userName, chatname = chatName
        )
    )

    val msg = serverMessage.receive()

    println(msg.message)
    if (msg.success) {
        ClientDataManager.chatName = chatName

        println(msg.admin)
        for (m in msg.chatMessages!!) {
            ClientDataManager.handleReceivedTextMessage(m)
        }
        ClientDataManager.admin = msg.admin.equals(ClientDataManager.userName)
        return true
    }

    return false
}

suspend fun clientCreateChat(): Boolean {
    print("Chat Name: ")
    val chatName: String = readln()

    ClientDataManager.serverConnection.send(
        Message(
            stage = Stage.CHAT_ENTRY, action = MessageAction.CREATE_CHAT,
            username = ClientDataManager.userName, chatname = chatName
        )
    )

    val msg = serverMessage.receive()

    println(msg.message)
    if (msg.success) {
        ClientDataManager.chatName = chatName
        ClientDataManager.admin = true
        return true
    }

    return false
}

suspend fun receiveFromServerCoroutine() {
    ClientDataManager.serverConnection.receive().collect { msg ->
        if (ClientDataManager.stage == Stage.TEXT_MESSAGES)
            ClientDataManager.handleReceivedTextMessage(msg)
        else
            serverMessage.send(msg)
    }
}

fun handleTextInput(msg: String) {
    if (msg.equals("//")) {
        if (ClientDataManager.admin)
            println("c (Close) / o (out) / pu (public) / pr(private) / a(add)")
        else
            println("c (Close) / o(out) ")

        val answer: String = readln()
        openTextInputMenu(answer)
        return
    }

    ClientDataManager.serverConnection.send(
        Message(
            stage = Stage.TEXT_MESSAGES,
            action = MessageAction.TEXT,
            message = msg,
            username = ClientDataManager.userName,
            chatname = ClientDataManager.chatName
        )
    )
}

private fun openTextInputMenu(msg: String) {
    when (msg) {
        "c" -> ClientDataManager.stage = Stage.CLOSE
        "o" -> {
            ClientDataManager.serverConnection.send(
                Message(
                    stage = Stage.TEXT_MESSAGES,
                    action = MessageAction.OUT_OF_CHAT,
                    chatname = ClientDataManager.chatName,
                    username = ClientDataManager.userName
                )
            )
            ClientDataManager.stage = Stage.CHAT_ENTRY
            ClientDataManager.chatName = ""
        }
    }
    if (!ClientDataManager.admin)
        return

    when (msg) {
        "pu" -> ClientDataManager.serverConnection.send(
            Message(
                stage = Stage.TEXT_MESSAGES,
                action = MessageAction.PUBLIC_CHAT,
                chatname = ClientDataManager.chatName
            )
        )

        "pr" -> ClientDataManager.serverConnection.send(
            Message(
                stage = Stage.TEXT_MESSAGES,
                action = MessageAction.PRIVATE_CHAT,
                chatname = ClientDataManager.chatName
            )
        )

        "a" -> {
            print("User Name: ")
            val username: String = readln()
            ClientDataManager.serverConnection.send(
                Message(
                    stage = Stage.TEXT_MESSAGES,
                    action = MessageAction.ADD_USER_TO_CHAT,
                    receiverUsername = username,
                    chatname = ClientDataManager.chatName
                )
            )
        }
    }

}