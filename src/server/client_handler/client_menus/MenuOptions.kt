package server.client_handler.client_menus

enum class MenuOptions(val output: String, val input: String) {
    CLOSE("c (Close)", "c"),
    LOG_IN("l (LogIn)", "l"),
    REGISTER("r (Register)", "r"),
    ENTER_CHAT("e (Enter A Chat)", "e"),
    CREATE_CHAT("cr (Create A Chat)", "cr"),
    LOG_OUT_USER("o (Log Out From User)", "o"),
    LOG_OUT_CHAT("o (Log Out Of Chat)", "o"),

    CHAT_TO_PUBLIC("pu (Set Chat privacy To PUBLIC)", "pu"),
    CHAT_TO_PRIVATE("pr (Set Chat Privacy To PRIVATE)", "pr"),
    ADD_USER_TO_CHAT("a (Add User To Chat)", "a")
}