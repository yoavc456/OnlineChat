package server.database

interface DatabaseManager:DatabaseUsersManager, DatabaseChatsManager, DatabaseMessagesManager{

    suspend fun createDatabase(dbName: String)
    suspend fun deleteDatabase(dbName: String)
    fun close()
}