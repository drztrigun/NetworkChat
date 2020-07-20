package ru.geekbrains.core;

import ru.geekbrains.MessageSocketThreadListener;
import ru.geekbrains.ServerSocketThread;
import ru.geekbrains.ServerSocketThreadListener;
import ru.geekbrains.chat.common.MessageLibrary;

import java.net.Socket;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {

    private ServerSocketThread serverSocketThread;
    private ClientSessionThread clientSession;
    private ChatServerListener listener;
    private AuthController authController;
    private Vector<ClientSessionThread> clients = new Vector<>();

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread = new ServerSocketThread(this, "Chat-Server-Socket-Thread", port, 2000);
        serverSocketThread.start();
        authController = new AuthController(); // инициализируем контроллер
        authController.init();
    }

    public void stop() {
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            return;
        }
        serverSocketThread.interrupt();
    }

    @Override
    public void onClientConnected() {
        logMessage("Client connected");
    }


    @Override // подключение
    public void onSocketAccepted(Socket socket) {
        this.clientSession = new ClientSessionThread(this, "ClientSessionThread", socket);
        // вроде здесь мы должны пополнять наш vector новыми подключениями
        clients.add(this.clientSession);
    }

    @Override    //обработка ошибок
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override //обработка ошибки когда клиент отключился
    public void onClientTimeout(Throwable throwable) {
    }

    @Override
    public void onSocketReady() {
        logMessage("Socket ready");
    }

    @Override
    public void onSocketClosed() {
        logMessage("Socket closed");
    }

    //логика переотправки сообщения
    @Override
    public void onMessageReceived(String msg) {
        if (clientSession.isAuthorized()) {
            processAuthorizedUserMessage(msg);
        } else{
            processUnauthorizedUserMessage(msg);
        }
    }

    // метод по отправке сообщения если клиент авторизован
    private void processAuthorizedUserMessage(String msg) {
        logMessage(msg);
//        for (ClientSessionThread  c : clients) {
//                clientSession.broadcast(msg);
//        }
//        clientSession.sendMessage("echo: " + msg);
    }

    // метод по отправке сообщения если клиент не авторизован
    private void processUnauthorizedUserMessage(String msg) {
        // парсим сообщение по делиметру
        String[] arr = msg.split(MessageLibrary.DELIMITER);
        if (arr.length < 4 ||
                !arr[0].equals(MessageLibrary.AUTH_METHOD) ||
                !arr[1].equals(MessageLibrary.AUTH_REQUEST)) {
            clientSession.authError("Incorrect request: " + msg); // ошибка формата
            return;
        }
        String login = arr[2];
        String password = arr[3];
        String nickname = authController.getNickname(login, password);
        if (nickname == null) {
            clientSession.authDeny();   // ошибка авторизации
            return;
        }
        clientSession.authAccept(nickname);   // авторизация прошла успешна
    }

    public void disconnectAll() {
    }

    private void logMessage(String msg) {
        listener.onChatServerMessage(msg);
    }
}

// что-то я запутался где надо делать отправку сообщения всем пользователям, у меня получается только что отправляется
// сообщение тока тому кто позже приконектился, получается не правильно заполняю массив
// но не пойму как его тогда правильно заполнить
//вопросов не так много
//1) можно по подробнее про MessageLibrary и как понять что именно туда надо добавлять при ее создании
//2) DELIMITER нам нужен для того чтобы разграничивать нашиданные после ввода их в поля при подключении
// так как они идут сполныи текстом получается?