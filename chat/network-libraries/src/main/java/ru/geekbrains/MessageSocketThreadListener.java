package ru.geekbrains;

public interface MessageSocketThreadListener {

    // дописываем MessageSocketThread thread чтобы понимать кто выполнил действие какой поток
    void onSocketReady(MessageSocketThread thread);

    void onSocketClosed(MessageSocketThread thread);

    void onMessageReceived(MessageSocketThread thread, String msg);

    void onException(MessageSocketThread thread, Throwable throwable); // метод обработки ошибки
}