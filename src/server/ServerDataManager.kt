package server

import connection.ClientConnection
import server.database.MongoDBManager

class ServerDataManager private constructor() {


    val CLIENT_CONNECTIONS: MutableList<ClientConnection> = mutableListOf()
    val LOGGED_IN_CLIENTS: HashMap<String, ClientConnection> = hashMapOf()
    val CHATS: HashMap<String, MutableList<String>> = HashMap()

    companion object {
        private val serverDataManager: ServerDataManager = ServerDataManager()

        fun getInstance(): ServerDataManager {
            return serverDataManager
        }
    }

    fun close() {
        for (c in CLIENT_CONNECTIONS) {
            c.close()
        }

        for (s in LOGGED_IN_CLIENTS) {
            s.value.close()
        }

        MongoDBManager.getInstance().close()
    }
}