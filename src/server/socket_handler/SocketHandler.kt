package server.socket_handler

import kotlinx.coroutines.runBlocking
import messages.*
import messages.Message
import server.ServerDataManager
import utils.*
import java.io.ObjectInputStream
import java.net.Socket

class SocketHandler(private val socket: Socket){
    private val serverDataManager = ServerDataManager.getInstance()
    private var msg: Message? = null
    private var run = true

    fun start()= runBlocking{
        try{
            while (run){
                waitingForClientInput()
                handleMessage()
            }
        }catch (e:Exception){

        }
        finally {
            closeClient()
        }
    }

    private fun waitingForClientInput(){
        val input = ObjectInputStream(socket.getInputStream())
        msg = input.readObject() as Message
    }

    private suspend fun handleMessage(){
        if(msg!!.action == MessageAction.CLOSE)
            run = false
        else if(msg!!.stage == Stage.USER_ENTRY)
            userEntryHandler(socket, msg!!)
        else if(msg!!.stage == Stage.CHAT_ENTRY)
            chatEntryHandler(socket, msg!!)
        else if(msg!!.stage == Stage.TEXT_MESSAGES)
            textMessagesHandler(msg!!)
    }

    private fun closeClient(){
        if(msg != null){
            serverDataManager.CHATS.remove(msg!!.chatname)?.remove(msg!!.username)
            serverDataManager.LOGGED_IN_SOCKETS.remove(msg!!.username)
            serverDataManager.SOCKETS.remove(socket)
        }
        socket.close()
    }

}