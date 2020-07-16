package ru.geekbrains.client;

import ru.geekbrains.MessageSocketThread;
import ru.geekbrains.MessageSocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, MessageSocketThreadListener {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private final JTextArea chatArea = new JTextArea();                             // область сообщений
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));     // верхняя панель
    private final JTextField ipAddressField = new JTextField("127.0.0.1");          // полея IP адреса
    private final JTextField portField = new JTextField("8181");                    // поле порта
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);//галочка всегда поверх окон
    private final JTextField loginField = new JTextField("login");                  // панель ввода логина клиента
    private final JPasswordField passwordField = new JPasswordField("123");    // панель ввода пароля
    private final JButton buttonLogin = new JButton("Login");                  // кнопка залогинится

    private final JPanel panelBottom = new JPanel(new BorderLayout());             // нижняя панель
    private final JButton buttonDisconnect = new JButton("<html><b>Disconnect</b></html>");  //выход с сервера
    private final JTextField messageField = new JTextField();                      // поле ввода сообщения
    private final JButton buttonSend = new JButton("Send");                   // кнопка отправить сообщение

    private final JList<String> listUsers = new JList<>();                         // список пользователей
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");  //формат записи сообщений
    private MessageSocketThread socketThread;

    // отправляемое сообщение

    public static void main(String[] args) {
        //вызываем класс, вызываем единственный метод,
        // передаем туда значение и создаем анонимный класс и реализуем интерфейс
        SwingUtilities.invokeLater(new Runnable() {    //запускаем в отдельном потоке чтобы не загружать другой поток(Runnable)
            @Override   // переписываем метод
            public void run() {
                new ClientGUI();    //запуск сервера
            }
        });
    }

    ClientGUI() {
        //переопределяем исключения
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat");                           // название окна
        setSize(WIDTH, HEIGHT);
        setAlwaysOnTop(true);                       // по дефолту галочка всегда стоит

        // предзаполняем список используем setListData, так как у нас указаны строки
        listUsers.setListData(new String[]{"user1", "user2", "user3", "user4", "user5",
                "user6", "user7", "user8", "user9", "user-with-too-long-name-in-this-chat"});
        JScrollPane scrollPaneUsers = new JScrollPane(listUsers);       // добавляем ползунки для списка пользователей
        JScrollPane scrollPaneChatArea = new JScrollPane(chatArea);     // добавляем ползунки для основного поля
        scrollPaneUsers.setPreferredSize(new Dimension(100, 0)); // фиксируем зазмер списка пользователей

        chatArea.setLineWrap(true);      // устанавливаем перенос текста в чате
        chatArea.setWrapStyleWord(true); // делаем перенос по словам
        chatArea.setEditable(false);     // устанавливаем запрет на изменения осноного поля чата

        panelTop.add(ipAddressField);
        panelTop.add(portField);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(loginField);
        panelTop.add(passwordField);
        panelTop.add(buttonLogin);
        panelBottom.add(buttonDisconnect, BorderLayout.WEST);
        panelBottom.add(messageField, BorderLayout.CENTER);
        panelBottom.add(buttonSend, BorderLayout.EAST);

        add(scrollPaneChatArea, BorderLayout.CENTER);
        add(scrollPaneUsers, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);

        cbAlwaysOnTop.addActionListener(this);  // делаем слушителя для фложочка cbAlwaysOnTop
        buttonSend.addActionListener(this);     //
        messageField.addActionListener(this);   //
        buttonLogin.addActionListener(this);    // обработка логина

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == buttonSend || src == messageField) {
            sendMessage(loginField.getText(), messageField.getText());
        } else if (src == buttonLogin) {
            Socket socket = null;
            try {
                // используем для подключения поля ipAdress и порт
                socket = new Socket(ipAddressField.getText(), Integer.parseInt(portField.getText()));
                socketThread = new MessageSocketThread(this, "Client" + loginField.getText(), socket);
            } catch (IOException ioException) {
                showError(ioException.getMessage());
            }

        } else {
            throw new RuntimeException("Unsupported action: " + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] ste = e.getStackTrace();
        String msg = String.format("Exception in \"%s\": %s %s%n\t %s",
                t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
        showError(msg);
    }

    public void sendMessage(String user, String msg) {
        if (msg.isEmpty()) {
            return;
        }
        //23.06.2020 12:20:25 <Login>: сообщение
        putMessageInChat(user, msg);
        messageField.setText("");
        messageField.grabFocus();
        socketThread.sendMessage(msg);   //отправка сообщения
    }

    public void putMessageInChat(String user, String msg) {
        String messageToChat = String.format("%s <%s>: %s%n", sdf.format(Calendar.getInstance().getTime()), user, msg);
        chatArea.append(messageToChat);
        putIntoFileHistory(user, messageToChat);
    }

    private void putIntoFileHistory(String user, String msg) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(user + "-history.txt", true))) {
            pw.print(msg);
        } catch (FileNotFoundException e) {
            showError(msg);
        }
    }

    private void showError(String errorMsg) {
        JOptionPane.showMessageDialog(this, errorMsg, "Exception!", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onMessageReceived(String msg) {
        putMessageInChat("server", msg);
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
        showError(throwable.getMessage());
    }
}
