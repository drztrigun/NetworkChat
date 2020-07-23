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

    // преднастроенный список сообщений
    public enum MESSAGE_TYPE {
        UNKNOWN,
        AUTH_ACCEPT,
        AUTH_DENIED,
        TYPE_BROADCAST,
        TYPE_BROADCAST_CLIENT,
        MSG_FORMAT_ERROR,
        USER_LIST
    }

    public static final String DELIMITER = "##";
    public static final String AUTH_METHOD = "/auth";
    public static final String AUTH_REQUEST = "request";
    public static final String AUTH_ACCEPT = "accept";
    public static final String AUTH_DENIED = "denied";

    /* то есть сообщение, которое будет посылаться всем */
    public static final String TYPE_BROADCAST = "/broadcast";

    /* если мы вдруг не поняли, что за сообщение и не смогли разобрать */
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";


    //бродкаст от клиента
    public static final String TYPE_BROADCAST_CLIENT =  "/client_msg";
    // сообщение со списоком пользователей
    public static final String USER_LIST = "/user_list";

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

    // сообщение о ошибке
    public static String getMsgFormatErrorMessage(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getBroadcastMessage(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    //
    public static String getTypeBroadcastClient(String nickname, String  msg){
        return TYPE_BROADCAST_CLIENT + DELIMITER + nickname + DELIMITER + msg;
    }

    // сообщение с пользователями
    public static String getUserList(String users){
        return USER_LIST + DELIMITER + users;
    }

    // обработка сообщений
    public static MESSAGE_TYPE getMessageType (String msg){
        String[] arr = msg.split(DELIMITER);
        if (arr.length < 2){
            return MESSAGE_TYPE.UNKNOWN;
        }
        String msgType = arr[0];
        switch (msgType){
            case AUTH_METHOD:
                if(arr[1].equals(AUTH_ACCEPT)){
                    return MESSAGE_TYPE.AUTH_ACCEPT;     // сообщение о авторизации
                } else if (arr[1].equals(AUTH_DENIED)){
                    return MESSAGE_TYPE.AUTH_DENIED;     //сообщение о запрете аторизации
                } else {
                    return MESSAGE_TYPE.UNKNOWN;          // не изевстное сообщение
                }
            case TYPE_BROADCAST:
                return MESSAGE_TYPE.TYPE_BROADCAST;      // сообщение типа  BROADCAST
            case TYPE_BROADCAST_CLIENT:
                return MESSAGE_TYPE.TYPE_BROADCAST_CLIENT;
            case MSG_FORMAT_ERROR:
                return MESSAGE_TYPE.MSG_FORMAT_ERROR;    // не правильный форамт сообщения
            case USER_LIST:
                return MESSAGE_TYPE.USER_LIST;
            default:
                return MESSAGE_TYPE.UNKNOWN;             // не изевстное сообщение
        }
    }
}