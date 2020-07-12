package ru.geekbrains.NetworkChat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    private static final int WIGHT = 400;
    private static final int HEIGHT = 300;
    public FileWriter fr;
    public BufferedWriter bw;
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
    public static FileReader fileReader;
    // отправляемое сообщение

    public static void main(String[] args) {
        //вызываем класс, вызываем единственный метод,
        // передаем туда значение и создаем анонимный класс и реализуем интерфейс
        SwingUtilities.invokeLater(new Runnable() {    //запускаем в отдельном потоке чтобы не загружать другой поток(Runnable)
            @Override   // переписываем метод	            @Override   // переписываем метод
            public void run() {
                new ClientGUI();    //запуск сервера
                // открываем файл записи
                try {
                    File file = new File("LogMessage.txt");
                    fileReader = new FileReader(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    ClientGUI(){
        //переопределяем исключения
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat");                           // название окна
        setSize(WIGHT, HEIGHT);
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

        // открываем файл записи
        try {
            File file = new File("LogMessage.txt");
            FileReader fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // добавляем слушителя по нажатию клавиатуры, переписываем оди метод по отпусканию клавиши Enter
        messageField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMsg();
                }
            }
        });

        // слушитель для кнопки "Send"	        // слушитель для кнопки "Send"
        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });

        setVisible(true);

        // закрываем буфер и сам файл, при любом выходе из программы
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    if (bw != null) {
                        System.out.println("Bye");  //проверяем что попали сюда
                        bw.close();       // закрываем буффер не забываем это делать
                    }
                    if (fr != null) {
                        System.out.println("Bye"); //проверяем что попали сюда
                        fr.close();      // закрываем файл не забываем это делать
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Bye");        //проверяем что попали сюда
            }
        });
    }

    private void sendMsg() {
        if (!messageField.getText().trim().isEmpty()) {  //проверяем что поле не пустое для отправки сообщения
            chatArea.append(messageField.getText() + "\n");
            // тут добавляем введеное сообщение в файл
            try {
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("'Date: ' yyyy.MM.dd ' time: ' HH:mm:ss");
                fr = new FileWriter("LogMessage.txt", true);
                bw = new BufferedWriter(fr);
                bw.append(String.format("User: \"%s\": %s %n",
                        format.format(date), messageField.getText()));
                bw.flush();
            } catch (IOException error) {
                error.printStackTrace();
            }
            messageField.setText("");  // делаем пустое поле в поле набора сообщения, после отправки сообщения
        }
    }

    @Override
    //выясняем какая кнопка была нажата через один слушатель(но лучше делать для каждой кнопки отдельный слушитель)
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == cbAlwaysOnTop){
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());    // условие если стоит, то при нажатии снять и наооборот
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
    }
}
