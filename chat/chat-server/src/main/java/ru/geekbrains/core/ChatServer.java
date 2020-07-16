package ru.geekbrains.core;

import ru.geekbrains.MessageSocketThread;
import ru.geekbrains.MessageSocketThreadListener;
import ru.geekbrains.ServerSocketThread;
import ru.geekbrains.ServerSocketThreadListener;

import java.net.Socket;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {

    private ServerSocketThread serverSocketThread;
    private MessageSocketThread socket;

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread = new ServerSocketThread(this,"Chat-Server-Socket-Thread", port, 2000);
        serverSocketThread.start();
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
    }

    @Override
    public void onClientConnected() {
        System.out.println("Client connected");
    }


    @Override // подключение
    public void onSocketAccepted(Socket socket) {
        this.socket = new MessageSocketThread(this, "ServerSocket", socket);
    }

    @Override    //обработка ошибок
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override //обработка ошибки когда клиент отключился
    public void onClientTimeout(Throwable throwable) {

    }

     //логика переотправки сообщения
    @Override
    public void onMessageReceived(String msg) {
        System.out.println(msg);
        socket.sendMessage("echo: " + msg);
    }

}

   /*Вопросы:
    1) Если смысл делать отправку сообщений отдельным классом  и добавлять его в библиотеку?
    и на сколько это трудно сделать? Или это делатеся только тогда когда мы предусматриваем отправку не только текстовых
    сообщений но и картинок и тому подобное
    2) Должны ли мы записывать сообщения так же и на строне самого сервера? если да то лучше метод так же выносить в
    библиотеку?
    3) Надо ли делать отдельный файл для записи ошибок на сервере?
* */
