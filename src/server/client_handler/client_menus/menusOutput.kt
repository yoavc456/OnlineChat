package server.client_handler.client_menus

import server.client_handler.client_menus.MenuOptions.*

val userEntryMenu = "${LOG_IN.output} / ${REGISTER.output} / ${CLOSE.output}"
val chatEntryMenu = "${ENTER_CHAT.output} / ${CREATE_CHAT.output} / ${CLOSE.output} / ${LOG_OUT_USER.output}"
val chatMenu = "${CLOSE.output} / ${LOG_OUT_CHAT.output}"
val chatMenuAdmin = "$chatMenu / ${CHAT_TO_PUBLIC.output} / ${CHAT_TO_PRIVATE.output} / ${ADD_USER_TO_CHAT.output}"
