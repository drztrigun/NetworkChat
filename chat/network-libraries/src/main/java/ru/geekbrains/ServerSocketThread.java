package ru.geekbrains;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {

    private final int port;
    private final int timeout;
    private final ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.listener = listener;
        this.timeout = timeout;
    }

    @Override  // подключает новых пользователей, возвращает тока подключение
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {      //такой записью закрываем сокет
            serverSocket.setSoTimeout(timeout);
            System.out.println(getName() + " running on port " + port );
            while (!isInterrupted()) {
                System.out.println("Waiting for connect");
                try {
                    Socket socket = serverSocket.accept();
                    listener.onSocketAccepted(socket);
                } catch (SocketTimeoutException e) {
                    listener.onClientTimeout(e);
                    continue;
                }
                listener.onClientConnected();
            }
        } catch (IOException e) {
            listener.onException(e);
        }
    }
}
