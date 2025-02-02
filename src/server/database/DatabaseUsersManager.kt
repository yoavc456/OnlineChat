package server.database

interface DatabaseUsersManager {
    suspend fun doesUserExist(username: String, password: String): Boolean
    suspend fun doesUsernameExist(username: String): Boolean
    suspend fun createUser(username: String, password: String): Boolean
}