package server.client_handler

import connection.Connection
import kotlinx.coroutines.runBlocking
import messages.*
import messages.Stage.*
import messages.client_msg.ClientMessage
import messages.server_msg.MessageInstruction
import messages.server_msg.MessageInstruction.*
import messages.server_msg.ServerMessage
import server.ServerDataManager
import java.util.UUID

class ClientHandler(private val clientConnection: Connection) {
    companion object {
        val clientMessageFunctions: Map<Stage, () -> ClientMessage> = mapOf(
            USER_ENTRY to ::clientUserEntryStage,
            LOG_IN to ::clientReceiveUsernameAndPasswordStage,
            REGISTER to ::clientReceiveUsernameAndPasswordStage,
            CHAT_ENTRY to ::clientChatEntryStage,
            ENTER_TO_CHAT to ::clientGetChatName,
            CREATE_CHAT to ::clientGetChatName,
            TEXT_MESSAGES to ::clientSendTextMessage,
            CHAT_MENU to ::clientChatMenu,
            CHAT_MENU_ADMIN to ::clientChatMenuAdmin,
            ADD_USER_TO_CHAT to ::clientAddUserToChat
        )
        val handleMessageFunctions: Map<Stage, suspend (ClientMessage, UUID) -> Stage> = mapOf(
            USER_ENTRY to ::userEntryStage,
            LOG_IN to ::logInStage,
            REGISTER to ::registerStage,
            CHAT_ENTRY to ::chatEntryStage,
            ENTER_TO_CHAT to ::enterToChatStage,
            CREATE_CHAT to ::createChatStage,
            TEXT_MESSAGES to ::textMessagesStage,
            CHAT_MENU to ::textMessagesChatMenuStage,
            CHAT_MENU_ADMIN to ::textMessagesChatMenuAdminStage,
            ADD_USER_TO_CHAT to ::addUserToChatStage
        )
    }

    private var uuid: UUID = UUID.randomUUID()

    private var stage: Stage = USER_ENTRY

    fun start() = runBlocking {
        try {
            ServerDataManager.CONNECTIONS.put(uuid, clientConnection)
            send()
            waitingForClientInput()
        } catch (_: Exception) {
            println("Client Disconnected From The Server")
        } finally {
            closeClient()
        }
    }

    private suspend fun send() {
        if (stage == Stage.CLOSE) {
            val msg = ServerMessage(instruction = MessageInstruction.CLOSE)
            clientConnection.send(msg)
            clientConnection.close()
            return
        }

        if (stage == Stage.LOAD_CHAT) {
            val msg = ServerMessage(
                instruction = MessageInstruction.LOAD_CHAT,
                chatMessages = ServerDataManager.databaseManager.loadMessages(ServerDataManager.UUID_TO_CHAT.get(uuid)!!)
            )
            clientConnection.send(msg)
            stage = TEXT_MESSAGES
        }

        val msg = ServerMessage(clientMessageFunctions.get(stage), ACTIVE)
        clientConnection.send(msg)
    }

    private suspend fun waitingForClientInput() {
        clientConnection.receive().collect { message ->
            stage = handleMessageFunctions.get(stage)?.invoke(message as ClientMessage, uuid)!!
            send()
        }
    }

    private fun closeClient() {
        val username = ServerDataManager.UUID_TO_USERNAME.get(uuid)
        val chatName = ServerDataManager.UUID_TO_CHAT.get(uuid)

        ServerDataManager.CONNECTIONS.remove(uuid)
        ServerDataManager.UUID_TO_USERNAME.remove(uuid)
        ServerDataManager.UUID_TO_CHAT.remove(uuid)
        ServerDataManager.USERNAMES.remove(username)
        ServerDataManager.CHATS.get(chatName)?.remove(username)
        clientConnection.close()

        println("CLOSE")
    }

}