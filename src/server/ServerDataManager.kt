package server

import connection.Connection
import server.database.DatabaseManager
import server.database.mongodb.MongoDBManager
import java.util.UUID

object ServerDataManager{

    val CONNECTIONS:MutableMap<UUID, Connection> = mutableMapOf()
    val USERNAMES:MutableMap<String, UUID> = mutableMapOf()
    val CHATS:MutableMap<String, MutableList<String>> = mutableMapOf()

    val UUID_TO_USERNAME:MutableMap<UUID, String> = mutableMapOf()
    val UUID_TO_CHAT:MutableMap<UUID, String> = mutableMapOf()

    val databaseManager:DatabaseManager = MongoDBManager

    fun close() {
        for (c in CONNECTIONS) {
            c.value.close()
        }

        MongoDBManager.close()
    }
}