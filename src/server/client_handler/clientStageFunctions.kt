package server.client_handler

import messages.client_msg.ClientMessage
import server.client_handler.client_menus.*

fun clientUserEntryStage(): ClientMessage {
    println(userEntryMenu)
    val answer = readln()
    return ClientMessage(message = answer)
}

fun clientReceiveUsernameAndPasswordStage(): ClientMessage {
    print("UserName: ")
    val userName: String = readln()
    print("Password: ")
    val password: String = readln()

    return ClientMessage(username = userName, password = password)
}

fun clientChatEntryStage():ClientMessage{
    println(chatEntryMenu)
    val answer = readln()
    return ClientMessage(message = answer)
}

fun clientGetChatName():ClientMessage{
    print("Chat Name: ")
    val chatName: String = readln()
    return ClientMessage(chatName = chatName)
}

fun clientSendTextMessage():ClientMessage{
    val msg = readln()
    return ClientMessage(message = msg)
}

fun clientChatMenu():ClientMessage{
    println(chatMenu)
    val answer: String = readln()
    return ClientMessage(message = answer)
}

fun clientChatMenuAdmin():ClientMessage{
    println(chatMenuAdmin)
    val answer: String = readln()
    return ClientMessage(message = answer)
}

fun clientAddUserToChat():ClientMessage{
    print("Username: ")
    val answer:String = readln()
    return ClientMessage(secondUsername = answer)
}