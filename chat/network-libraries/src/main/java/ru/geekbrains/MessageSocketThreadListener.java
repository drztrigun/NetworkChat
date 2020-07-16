package ru.geekbrains;

public interface MessageSocketThreadListener {

    void onMessageReceived(String msg);

    void onException(Throwable throwable); // метод обработки ошибки
}