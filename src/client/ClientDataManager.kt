package client

import connection.Connection
import messages.*

object ClientDataManager{

    lateinit var serverConnection:Connection

    fun closeClient() {
        if(!::serverConnection.isInitialized)
            return

        serverConnection.close()

        println("CLOSE")
    }
}