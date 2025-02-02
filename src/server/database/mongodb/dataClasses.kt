package server.database.mongodb

data class User(
    val username: String,
    val password: String
)

data class Chat(
    val chatname: String,
    val admin: String,
    val open: Boolean,
    val members: MutableList<String>
)

data class MongodbChatMessage(
    val sender: String,
    val msg: String
)