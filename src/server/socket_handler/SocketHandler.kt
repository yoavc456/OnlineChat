package server.socket_handler

import connection.ClientConnection
import kotlinx.coroutines.runBlocking
import messages.*
import messages.Message
import server.ServerDataManager

class SocketHandler(private val clientConnection: ClientConnection){
    private val serverDataManager = ServerDataManager.getInstance()
    private var msg: Message? = null
    private var run = true

    fun start()= runBlocking{
        try{
            waitingForClientInput()
        }catch (_:Exception){
            println("Client Disconnected From The Server")
        }
        finally {
            closeClient()
        }
    }

    private suspend fun waitingForClientInput(){
        clientConnection.receive().collect{
            message ->
            msg = message
            handleMessage()
        }
    }

    private suspend fun handleMessage(){
        when(msg!!.stage){
            Stage.USER_ENTRY -> userEntryHandler(clientConnection, msg!!)
            Stage.CHAT_ENTRY -> chatEntryHandler(clientConnection, msg!!)
            Stage.TEXT_MESSAGES -> textMessagesHandler(msg!!)

            Stage.CLOSE -> run=false

            else -> {}
        }
    }

    private fun closeClient(){
        if(msg != null){
            serverDataManager.CHATS.remove(msg!!.chatname)?.remove(msg!!.username)
            serverDataManager.LOGGED_IN_CLIENTS.remove(msg!!.username)
            serverDataManager.CLIENT_CONNECTIONS.remove(clientConnection)
        }

        clientConnection.close()

        println("CLOSE")
    }

}