package server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import messages.*
import utils.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

class ChatServer {

    private val SERVER_SOCKET:ServerSocket
    private val PORT:Int = 1234
    private val SOCKETS:MutableList<Socket>
    private val LOGGED_IN_SOCKETS:HashMap<String, Socket>
    private val CHATS:HashMap<String, MutableList<String>>

    init {
        SERVER_SOCKET = ServerSocket(PORT)

        SOCKETS = mutableListOf<Socket>()
        LOGGED_IN_SOCKETS = hashMapOf<String, Socket>()
        CHATS = HashMap<String, MutableList<String>>()

        GlobalScope.launch {
            waitForNewConnectionCoroutine()
        }

        readln()

        for (s in SOCKETS){
            s.close()
        }

        for (s in LOGGED_IN_SOCKETS){
            s.value.close()
        }

        SERVER_SOCKET.close()
    }

    fun waitForNewConnectionCoroutine(){
        while (!SERVER_SOCKET.isClosed()){
            val socket = SERVER_SOCKET.accept()
            addConnection(socket)
        }
    }

    //Activated when a new client connected the server. Make a coroutine that handle the client.
    private fun addConnection(socket: Socket){
        GlobalScope.launch {
            val username:String = entryMessageCoroutine(socket)
            if(!username.equals("")){
                val chatname:String = chatEnterCoroutine(socket, username)
                if(!chatname.equals("")){
                    textMessageCoroutine(socket, username, chatname)
                }
            }
        }
    }

    fun sendMessage(msg:Any, socket:Socket){
        val output = ObjectOutputStream(socket.getOutputStream())
        output.writeObject(msg)
    }

    //Activated when a client send a text message.
    // Use "sendMessage" function to send the message to the clients that supposed to receive it
    suspend fun sendTextMessage(msg:TextMessage){
        saveMessage(msg.chatName, msg.senderUserName, msg.message)
        for (userName in CHATS.get(msg.chatName)!!){
            if(!userName.equals(msg.senderUserName)){
                LOGGED_IN_SOCKETS.get(userName)?.let { sendMessage(msg, it) }
            }
        }
    }

    //Waiting for an EntryMessage from the client. According to the message action it handle the client (close, log in, register)
    suspend fun entryMessageCoroutine(socket: Socket):String{
        SOCKETS.add(socket)
        var run:Boolean = true
        var username:String=""

        while (run){

            try {
                val input = ObjectInputStream(socket.getInputStream())
                val msg = input.readObject() as EntryMessage
                username = msg.username

                if(msg.action == MessageAction.CLOSE){
                    SOCKETS.remove(socket)
                    socket.close()
                    return ""
                }

                if(msg.action == MessageAction.LOG_IN){
                    run = !logIn(socket, msg)
                    val entryAcceptMessage:String = if(!run) "Logged In" else "Log In Failed"
                    sendMessage(EntryAcceptMessage(!run, entryAcceptMessage), socket)
                }

                if(msg.action == MessageAction.REGISTER){
                    run = !register(socket, msg)
                    val entryAcceptMessage:String = if(!run) "Register" else "Register Failed"
                    sendMessage(EntryAcceptMessage(!run, entryAcceptMessage), socket)
                }
            }catch (e:Exception){
                SOCKETS.remove(socket)
                socket.close()
                println("a")
                return ""
            }

        }
        return username
    }

    //Function that activated in a coroutine for each client.
    // Waiting for an 'EntryMessage' messages from the client.
    suspend fun chatEnterCoroutine(socket: Socket, username:String):String{
        var run:Boolean = true
        var chatname:String = ""
        while (run){
            try {
                val input = ObjectInputStream(socket.getInputStream())
                val msg = input.readObject() as EntryMessage
                chatname = msg.chatname

                if(msg.action == MessageAction.CLOSE){
                    LOGGED_IN_SOCKETS.remove(msg.username)
                    socket.close()
                    return ""
                }

                if(msg.action == MessageAction.ENTER_CHAT){
                    run = !enterChat(msg)

//                if(CHATS.get(msg.chatname)!=null){
//                    run = false
//                    CHATS.get(msg.chatname)!!.add(msg.username)
//                }

                    val entryAcceptMessage:String = if(!run) "Entered Chat" else "Chat Does Not Exist"
                    if(!run)
                        sendMessage(EntryAcceptMessage(true, entryAcceptMessage,
                            loadMessages(msg.chatname), getChatAdmin(msg.chatname)), socket)
                    else
                        sendMessage(EntryAcceptMessage(false, entryAcceptMessage), socket)
                }

                if(msg.action == MessageAction.CREATE_CHAT){
                    run = !createChat(msg)
                    val entryAcceptMessage:String = if(!run) "Chat Created" else "Chat Does Not Created"
                    sendMessage(EntryAcceptMessage(!run, entryAcceptMessage), socket)
                }
            }catch (e:Exception){
                LOGGED_IN_SOCKETS.remove(username)
                socket.close()
                println("aa")
                return ""
            }

        }
        return chatname
    }

    //Function that activated in a coroutine for each client.
    // Waiting for a 'TextMessage' messages from the client.
    suspend fun textMessageCoroutine(socket:Socket, username:String, chatname:String){
        var run:Boolean = true

        while (run){
            try {
                val input = ObjectInputStream(socket.getInputStream())
                val msg = input.readObject() as TextMessage

                if(msg.action == MessageAction.CLOSE){
                    LOGGED_IN_SOCKETS.remove(msg.senderUserName)
                    CHATS.get(msg.chatName)?.remove(msg.senderUserName)
                    socket.close()
                    run = false
                }else if(msg.action == MessageAction.TEXT){
                    sendTextMessage(msg)
                    println(msg.senderUserName)
                }else if(msg.action == MessageAction.PRIVATE_CHAT || msg.action == MessageAction.PUBLIC_CHAT){
                    setChatPrivacy(msg.action, msg.chatName)
                } else if(msg.action == MessageAction.ADD_USER_TO_CHAT){
                    addUserToChat(msg.chatName, msg.receiverUserName)
                }
            }catch (e:Exception){
                LOGGED_IN_SOCKETS.remove(username)
                CHATS.get(chatname)?.remove(username)
                socket.close()
                run = false
                println("aaa")
            }
        }
    }

    private suspend fun logIn(socket: Socket, msg:EntryMessage):Boolean{
        if(isUserExist(msg.username, msg.password) && LOGGED_IN_SOCKETS.get(msg.username) == null){
            SOCKETS.remove(socket)
            LOGGED_IN_SOCKETS.put(msg.username, socket)
            return true
        }

        return false
    }

    private suspend fun register(socket: Socket, msg:EntryMessage):Boolean{
        if(!isUsernameExist(msg.username)){
            if(LOGGED_IN_SOCKETS.get(msg.username)!=null)
                return false
            createUser(msg.username, msg.password)
            SOCKETS.remove(socket)
            LOGGED_IN_SOCKETS.put(msg.username, socket)
            return true
        }

        return false
    }

    private suspend fun enterChat(msg: EntryMessage):Boolean{
        if(utils.enterChat(msg.chatname, msg.username)){
            if(CHATS.get(msg.chatname)==null){
                val chatUsers = mutableListOf<String>()
                chatUsers.add(msg.username)
                CHATS.put(msg.chatname, chatUsers)
            }else{
                CHATS.get(msg.chatname)!!.add(msg.username)
            }
            return true
        }

        return false
    }

    private suspend fun createChat(msg:EntryMessage):Boolean{
        if(utils.createChat(msg.chatname, msg.username)){
            val chatUsers = mutableListOf<String>()
            chatUsers.add(msg.username)
            CHATS.put(msg.chatname, chatUsers)
            return true
        }

        return false
    }
}