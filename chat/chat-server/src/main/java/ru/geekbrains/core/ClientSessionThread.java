package ru.geekbrains.core;

import ru.geekbrains.MessageSocketThread;
import ru.geekbrains.MessageSocketThreadListener;
import ru.geekbrains.chat.common.MessageLibrary;

import java.net.Socket;

// эти классом переписываем логику поведения  MessageSocketThread

public class ClientSessionThread extends MessageSocketThread {

    private boolean isAuthorized = false;
    // сохраняем Ник пользователя
    private String nickname;

    public ClientSessionThread(MessageSocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public String getNickname(){
        return nickname;
    }

    //  метод успешной авторизации
    public void authAccept(String nickname){
        this.nickname = nickname; // сохрянаем никнейм
        this.isAuthorized = true;
        sendMessage(MessageLibrary.getAuthAcceptMessage(nickname));
    }

    // метод ошибки авторизации
    public void authDeny() {
        sendMessage(MessageLibrary.getAuthDeniedMessage());
        close();
    }

    // метод ошибки формата ввода данных
    public void authError(String msg) {
        sendMessage(MessageLibrary.getMsgFormatErrorMessage(msg));
        close();
    }


    public void broadcast(String msg) {
        sendMessage(MessageLibrary.getBroadcastMessage(nickname, msg));
    }
}
