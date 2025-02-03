package server.socket_handler

import messages.client_msg.ClientMessage

fun clientUserEntryStage(): ClientMessage {
    println("l(LogIn)/r(Register)/c(Close)")
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
    println("e(Enter A Chat)/cr(Create A Chat)/c(Close)/o(out)")
    val answer = readln()
    return ClientMessage(message = answer)
}

fun clientGetChatName():ClientMessage{
    print("Chat Name: ")
    val chatName: String = readln()
    return ClientMessage(chatName = chatName)
}

fun clientSendTextMessage():ClientMessage{
    print("Message: ")
    val msg = readln()
    return ClientMessage(message = msg)
}

fun clientChatMenu():ClientMessage{
    println("c (Close) / o(out) ")
    val answer: String = readln()
    return ClientMessage(message = answer)
}

fun clientChatMenuAdmin():ClientMessage{
    println("c (Close) / o (out) / pu (public) / pr(private) / a(add)")
    val answer: String = readln()
    return ClientMessage(message = answer)
}