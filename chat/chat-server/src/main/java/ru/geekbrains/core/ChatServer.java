package ru.geekbrains.core;

import ru.geekbrains.MessageSocketThread;
import ru.geekbrains.MessageSocketThreadListener;
import ru.geekbrains.ServerSocketThread;
import ru.geekbrains.ServerSocketThreadListener;
import ru.geekbrains.chat.common.MessageLibrary;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, MessageSocketThreadListener {

    private ServerSocketThread serverSocketThread;
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
        disconnectAll();
    }

    /*
     * Server Socket Thread Listener Methods
     */

    @Override
    public void onClientConnected() {
        logMessage("Client connected");
    }


    @Override // подключение
    public void onSocketAccepted(Socket socket) {
        // пополняем наш vector новыми подключениями
        clients.add(new ClientSessionThread(this, "ClientSessionThread", socket));
    }

    @Override    //обработка ошибок
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override //обработка ошибки когда клиент отключился
    public void onClientTimeout(Throwable throwable) {
    }

    /*
     * Message Socket Thread Listener Methods
     */

    @Override
    public void onSocketReady(MessageSocketThread thread) {
        logMessage("Socket ready");
    }

    // закрытие сокета
    @Override
    public void onSocketClosed(MessageSocketThread thread) {
        ClientSessionThread clientSession = (ClientSessionThread) thread;
        logMessage("Socket closed");
        clients.remove(thread);   // убираем пользователя из списка
        if (clientSession.isAuthorized() &&!clientSession.isReconnected()){
        sendToAllAouthorizedClients(MessageLibrary.getBroadcastMessage("server: ",
                "User " + clientSession.getNickname() + " disconnected!"));
        }
        sendToAllAouthorizedClients(MessageLibrary.getUserList(getUserList()));
    }

    //логика переотправки сообщения
    @Override
    public void onMessageReceived(MessageSocketThread thread, String msg) {
        // кастим родителький поток к наследнику
        ClientSessionThread clientSession = (ClientSessionThread)thread;
        if (clientSession.isAuthorized()) {
            processAuthorizedUserMessage(msg);
        } else{
            processUnauthorizedUserMessage(clientSession, msg);
        }
    }

    @Override    //обработка ошибок
    public void onException(MessageSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }


    // метод по отправке сообщения если клиент авторизован
    private void processAuthorizedUserMessage(String msg) {
        logMessage(msg);
        for (ClientSessionThread client : clients){  // цикл по сессиям
            if(!client.isAuthorized()){  // првоеряем что клиетн авторизваон еали нет то следующий
                continue;
            }
            client.sendMessage(msg); // отправка всем клиентам
        }
    }

    // метод массовой рассылки всем авторизованным клиентам о присоединениие нового клиента
    private void sendToAllAouthorizedClients(String msg){
        for (ClientSessionThread client : clients){
            if(!client.isAuthorized()){
                continue;
            }
            // на строне сервера для того чтобы не отправлять сообщение тому кто отправил его надо распарсить сообщение
            // и сравнить ники которое в сообщении и с тем который отправил это сообщение
            client.sendMessage(msg);
        }
    }

    // метод по отправке сообщения если клиент не авторизован
    private void processUnauthorizedUserMessage(ClientSessionThread clientSession, String msg) {
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
        } else { // логика если клиент переконнектился заново с другого устройства
            ClientSessionThread oldClientSession = findClinetSessionNickname(nickname);
            clientSession.authAccept(nickname);
            if(oldClientSession == null){
                sendToAllAouthorizedClients(MessageLibrary.getBroadcastMessage("server:", nickname + " connected"));
            } else {
                oldClientSession.setReconnected(true);
                clients.remove(oldClientSession);
            }
        }
        clientSession.authAccept(nickname);   // авторизация прошла успешна
        sendToAllAouthorizedClients(MessageLibrary.getUserList(getUserList()));
    }

    public void disconnectAll() {
        //делаем копию коллекции, для того чтобы не идти по одной коллекции и не удалет из нее же клиента
        ArrayList<ClientSessionThread> currentClients = new ArrayList<>(clients);
        for (ClientSessionThread client : currentClients){
            client.close();            //отключили клиента
            clients.remove(client);   // удаляем клиента из родительской коллекции
        }
    }

    private void logMessage(String msg) {
        listener.onChatServerMessage(msg);
    }

    // метод по выводу списка пользователей
    public String getUserList(){
        StringBuilder sb = new StringBuilder();  //для конкъютинации большого количества строк
        for (ClientSessionThread client: clients){
            if (!client.isAuthorized()){
                continue;
            }
            sb.append(client.getNickname()).append(MessageLibrary.DELIMITER);
        }
        return sb.toString();
    }

    // метод по неявному переподлючению пользователя к чату т.е. ни кто не увидит сообщений о переподлючении
    private ClientSessionThread findClinetSessionNickname(String nickname){
        for (ClientSessionThread client: clients) {
            if(!client.isAuthorized()){
                continue;
            }
            if(client.getNickname().equals(nickname)){
                return client;
            }
        }
        return null;
    }
}