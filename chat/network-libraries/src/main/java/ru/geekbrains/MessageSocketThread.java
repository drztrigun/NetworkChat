package ru.geekbrains;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageSocketThread extends Thread {

    private Socket socket;
    private MessageSocketThreadListener listener;
    private DataInputStream in;  // глобальная переменная исходящего потока
    private DataOutputStream out; // глобальная переменная входящего потока
    private boolean isClosed = false; //глобальный рубильник по закрытию потока соединения

    public MessageSocketThread(MessageSocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        start();
    }

    @Override  // отдает сообщение наружу
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            listener.onSocketReady();
            while (!isInterrupted()) {
                if(!isClosed) {
                    listener.onMessageReceived(in.readUTF());
                }
            }
        } catch (IOException e) {
            close();
            System.out.println(e);
        } finally {
            close();
        }
    }


    // метод по отправке сообщения для того кто ждет его
    public void sendMessage(String message) {
        try {
            if (!socket.isConnected() || socket.isClosed() || isClosed) {
                listener.onException(new RuntimeException("Socked closed or not initialized"));
                return;
            }
            if(!isClosed) {
                out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(message);
            }
        } catch (IOException e) {
            close();
            listener.onException(e);
        }

    }

    //делаем поток синхронным чтобы несколько потоков не изменяли этот метод сразу
    public synchronized void close() {
        isClosed = true;
        interrupt();
        try {
            if (out != null){
                out.close();
            }
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.onSocketClosed();
    }
}
