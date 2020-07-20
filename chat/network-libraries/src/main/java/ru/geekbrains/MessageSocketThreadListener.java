package ru.geekbrains;

public interface MessageSocketThreadListener {

    void onSocketReady();

    void onSocketClosed();

    void onMessageReceived(String msg);

    void onException(Throwable throwable); // метод обработки ошибки
}