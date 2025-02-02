package server.database.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import server.database.DatabaseUsersManager

class MongoDBUsersManager(database: MongoDatabase):DatabaseUsersManager {
    private val usersCollection = database.getCollection<User>("users")

    override suspend fun doesUserExist(username: String, password: String): Boolean {
        val filter = Filters.eq(User::username.name, username)
        val user = usersCollection.find<User>(filter).firstOrNull() ?: return false
        return user.password == password
    }

    override suspend fun doesUsernameExist(username: String): Boolean {
        val filter = Filters.eq(User::username.name, username)
        return usersCollection.find<User>(filter).firstOrNull()!=null
    }

    override suspend fun createUser(username: String, password: String): Boolean {
        println(1)
        if (doesUsernameExist(username))
            return false
        println(2)
        val user = User(username, password)
        usersCollection.insertOne(user)

        println("Created New User. username: $username passeord: $password ")
        return true
    }
}