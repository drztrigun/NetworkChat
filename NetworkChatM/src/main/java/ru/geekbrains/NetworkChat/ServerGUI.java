package ru.geekbrains.NetworkChat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    private static final int POS_X = 1000;
    private static final int POS_Y = 550;
    private static final int WIGHT = 200;
    private static final int HEIGHT = 100;

    private ChatServer chatServer;
    private final JButton buttonStart = new JButton("Start");
    private final JButton buttonStop = new JButton("Stop");

    public static void main(String[] args) {
        //вызываем класс, вызываем единственный метод, передаем туда значение и создаем анонимный класс и реализаем интерфейс
        SwingUtilities.invokeLater(new Runnable() {    //запускаем в отдельном потоке чтобы не загружать другой поток(Runnable)
            @Override   // переписываем метод
            public void run() {
                new ServerGUI();    //запуск сервера
            }
        });
    }

    ServerGUI(){
        //переопределяем исключения
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIGHT, HEIGHT);     // устанавливаем значения окна
        setResizable(false);                        // запрещаем изменять окно
        setTitle("Chat Server Admin Console");      // название окна

        setLayout(new GridLayout(1,2));   // создаем всего две кнопки
        chatServer = new ChatServer();              // добавляем старт и стоп сервера
        buttonStart.addActionListener(this);      // добиваляем ActionListener для фиксации нажатия кнопки
        buttonStop.addActionListener(this);

        add(buttonStart);                           //добавляем кнопки
        add(buttonStop);

        setVisible(true);

    }

    @Override
    //выясняем какая кнопка была нажата через один слушатель(но лучше делать для каждой кнопки отдельный слушитель)
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == buttonStart){
            chatServer.start(8181);
        } else  if (src == buttonStop){
            chatServer.stop();
            throw new RuntimeException("Unsupported action" + src);
        } else {
            throw new RuntimeException("Unsupported action" + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] ste = e.getStackTrace(); //массив вызовов
        String msg = String.format("Exception in \"%s\": %s %s%n\t %s",
                t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
        JOptionPane.showMessageDialog(this, msg, "Exception!", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}

