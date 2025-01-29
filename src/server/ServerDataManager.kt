package server

import connection.Connection
import server.database.MongoDBManager

object ServerDataManager{


    val CLIENT_CONNECTIONS: MutableList<Connection> = mutableListOf()
    val LOGGED_IN_CLIENTS: HashMap<String, Connection> = hashMapOf()
    val CHATS: HashMap<String, MutableList<String>> = HashMap()

    fun close() {
        for (c in CLIENT_CONNECTIONS) {
            c.close()
        }

        for (s in LOGGED_IN_CLIENTS) {
            s.value.close()
        }

        MongoDBManager.close()
    }
}