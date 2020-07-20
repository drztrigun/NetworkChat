package ru.geekbrains.chat.common;


// класс в которым будем прописывать все технические сообщения
public class MessageLibrary {

    /* константы
     * /auth##request##login##password         // для авторизации будет приходить в таком формате
     * /auth##accept##nickname                // для подтверждения авторизации в таком формате
     * /auth##denied                         // для запрета авторизации в таком виде
     * /broadcast##msg                       // сообщение для всех пользователей
     *
     * /msg_format_error##msg                // формат ошибки
     * */

    public static final String DELIMITER = "##";
    public static final String AUTH_METHOD = "/auth";
    public static final String AUTH_REQUEST = "request";
    public static final String AUTH_ACCEPT = "accept";
    public static final String AUTH_DENIED = "denied";
    /* если мы вдруг не поняли, что за сообщение и не смогли разобрать */
    public static final String TYPE_BROADCAST = "/broadcast";

    /* то есть сообщение, которое будет посылаться всем */
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";

    //формируем сообщение  на авторизацию
    public static String getAuthRequestMessage(String login, String password) {
        return AUTH_METHOD + DELIMITER + AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    // сообщение о подветрждении авторизации
    public static String getAuthAcceptMessage(String nickname) {
        return AUTH_METHOD + DELIMITER + AUTH_ACCEPT + DELIMITER + nickname;
    }

    // сообщение о запрете авторизации
    public static String getAuthDeniedMessage() {
        return AUTH_METHOD + DELIMITER + AUTH_DENIED;
    }

    // сообщение о ощибке
    public static String getMsgFormatErrorMessage(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getBroadcastMessage(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }
}
