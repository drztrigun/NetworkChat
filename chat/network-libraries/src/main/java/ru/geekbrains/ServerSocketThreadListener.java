package ru.geekbrains;

import java.net.Socket;

//создаем интерфейс для передачи сокета
public interface ServerSocketThreadListener {

    void onClientConnected();

    void onSocketAccepted(Socket socket);

    void onException(Throwable throwable); // метод обработки ошибки

    void onClientTimeout(Throwable throwable);
}