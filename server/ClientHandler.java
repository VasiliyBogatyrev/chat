package ru.geekbrains.chat.git.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) { // "/auth login pass"
                            String[] elements = str.split(" ");
                            if (elements.length == 3) {
                                String n = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                                if (n != null) {
                                    if (!server.isNickBusy(n)) {
                                        name = n;
                                        sendMsg("/authok " + n);
                                        server.subscribeMe(this);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется на другом компьютере");
                                    }
                                } else {
                                    sendMsg("Неверные логин/пароль");
                                }
                            }
                        } else {
                            sendMsg("Для начала авторизуйтесь");
                        }
                    }
                    server.broadcastMsg("server", "К чату подключился пользователь " + name);
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {

                            //"/changenick new_nick и другие служебные команды"

                            if (str.equalsIgnoreCase("/end")) break;

                            if (str.startsWith("/history")) {
                                sendMsg("/history " + historyMsg());
                            }

                            if (str.startsWith("/changenick")) {
                                String[] elements = str.split(" ");
                                server.getAuthService().setChangeNick(name, elements[1]);
                                name = elements[1];
                                server.broadcastUsersList();
                            }

                            if (str.startsWith("/pm")) { // '/pm nick111 Hello, my friend'
                                String nameTo = str.split(" ")[1];
                                String m = str.substring(5 + nameTo.length());
                                server.whispTo(this, nameTo, m);
                            }
                        } else {
                            server.broadcastMsg(name, str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribeMe(this);
                    server.broadcastMsg("server", "Из чата вышел пользователь " + name);
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kick() {
        server.unsubscribeMe(this);
        server.broadcastMsg("server", "Пользователь " + name + " нарушил правила чата и был удален");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> historyMsg() {
        ArrayList<String> arr = SQLHandler.getMsg();
        return arr;
    }
}
