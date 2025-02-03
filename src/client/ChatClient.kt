package client

import connection.socket_tcp.ConnectionTcp
import kotlinx.coroutines.*

class ChatClient {

    val IP = "localhost"
    val PORT = 1234


    init {
        try {
            createConnection()
            startReceivingMessagesFromServer()

            while (ClientDataManager.serverConnection.isOpen()){

            }
        } catch (e: Exception) {
            println("Disconnected From The Server!")
        } finally {
            ClientDataManager.closeClient()
        }
    }

    private fun createConnection(){
        try{
            ClientDataManager.serverConnection = ConnectionTcp(IP, PORT)
        }catch (e:Exception){
            println("Failed to connect to the server!")
            println("Press 'c' to close. Press any other button to try again.")

            if(readln() == "c")
                return

            createConnection()
        }
    }

    private fun startReceivingMessagesFromServer() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            receiveFromServerCoroutine()
        }
    }

}