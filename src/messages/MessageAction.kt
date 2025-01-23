package messages

enum class MessageAction {
    LOG_IN,
    REGISTER,
    ENTER_CHAT,
    CREATE_CHAT,
    PUBLIC_CHAT,
    PRIVATE_CHAT,
    ADD_USER_TO_CHAT,
    TEXT,
    CLOSE,
    OUT_OF_CHAT,
    OUT_OF_USER,
    NULL
}