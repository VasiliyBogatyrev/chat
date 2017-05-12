package ru.geekbrains.chat.git.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server extends JFrame {
    private final int PORT = 8189;
    private Vector<ClientHandler> clients;
    private AuthService authService;
    private JList jlUsers;
    private JScrollPane jspUsers;
    private JScrollPane jspText;
    private JTextArea jText;
    private JButton kick;
    private JTextField applicant;
    private ServerSocket server = null;
    private Socket socket = null;


    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        setBounds(400, 400, 600, 600);
        setTitle("Server");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jText = new JTextArea();
        jText.setLineWrap(true);
        jText.setWrapStyleWord(true);
        jText.setEditable(false);
        jspText = new JScrollPane(jText);
        add(jspText, BorderLayout.CENTER);
        jlUsers = new JList(new DefaultListModel());
        jlUsers.setPreferredSize(new Dimension(150, 10));
        jspUsers = new JScrollPane(jlUsers);
        add(jspUsers, BorderLayout.EAST);
        JPanel kickApp = new JPanel();
        kick = new JButton("Kick");
        kick.setSize(100, 50);
        applicant = new JTextField();
        applicant.setPreferredSize(new Dimension(200, 25));
        applicant.setEditable(false);
        kickApp.add(applicant);
        kickApp.add(kick);
        add(kickApp, BorderLayout.SOUTH);
        kick.addActionListener(e -> setKick());
        jlUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    applicant.setText(jlUsers.getSelectedValue().toString());
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    server.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        setVisible(true);

        try {
            authService = new DBAuthService();
            jText.append("Авторизация через БД\n");

        } catch (RuntimeException e) {

            authService = new BaseAuthService();
            jText.append("Базовая авторизация\n");
        }
        clients = new Vector<>();
        try {
            server = new ServerSocket(PORT);
            jText.append("Сервер запущен, ожидаем подключение клиентов...\n");
            while (true) {
                socket = server.accept();
                new ClientHandler(this, socket);
                jText.append("Клиент подключился\n");
            }
        } catch (IOException e) {
            jText.append("Не удалось запустить сервер\n");
        } finally {
            System.out.println("Завершении сессии\n");
            SQLHandler.deleteMsg();
            try {
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            authService.shutdown();
        }
    }

    private void setKick (){
        SQLHandler.getMsg();
        for (ClientHandler o : clients) {
            if (o.getName().equals(applicant.getText())) {
                o.kick();
            }
        }
    }

    public void broadcastMsg(String name, String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(name + ": " + msg);
        }
        jText.append(name + ": " + msg + "\n");
        jText.setCaretPosition(jText.getDocument().getLength());
        SQLHandler.setMsg(name, msg);
    }

    public synchronized void broadcastUsersList() {
        StringBuffer sb = new StringBuffer("/userslist");
        for (ClientHandler o : clients) {
            sb.append(" " + o.getName());
        }
        for (ClientHandler o : clients) {
            o.sendMsg(sb.toString());
        }
        ((DefaultListModel) jlUsers.getModel()).clear();
            for (ClientHandler o : clients) {
                ((DefaultListModel) jlUsers.getModel()).addElement(o.getName());
            }
            jlUsers.repaint();
        }

    public synchronized void subscribeMe(ClientHandler c) {
        clients.add(c);
        broadcastUsersList();
    }

    public synchronized void unsubscribeMe(ClientHandler c) {
        clients.remove(c);
        broadcastUsersList();
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) return true;
        }
        return false;
    }


    public synchronized void whispTo(ClientHandler from, String to, String msg) {
        for (ClientHandler o : clients) {
            if(o.getName().equalsIgnoreCase(to)) {
                o.sendMsg("from " + from.getName() + ": " + msg);
                from.sendMsg("to " + to + ": " + msg);
                 break;
            }
        }
    }
}
