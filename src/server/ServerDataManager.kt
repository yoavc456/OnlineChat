package server

import connection.Connection
import server.database.DatabaseManager
import server.database.mongodb.MongoDBManager

object ServerDataManager{


    val CLIENT_CONNECTIONS: MutableList<Connection> = mutableListOf()
    val LOGGED_IN_CLIENTS: HashMap<String, Connection> = hashMapOf()
    val CHATS: HashMap<String, MutableList<String>> = HashMap()

    val databaseManager:DatabaseManager = MongoDBManager

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