package ru.geekbrains.chat.git.client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MyWindow extends JFrame {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private JTextField jtf;
    private JTextField jtfLogin;
    private JPasswordField jpfPass;
    private JPanel bottomPanel, upperPanel;
    private final String SERVER_IP_ADDRESS = "localhost";
    private final int SERVER_PORT = 8189;
    private boolean isAuthorized;
    private String nick;
    private JList jlUsers;
    private JScrollPane jspUsers;
    private JTextArea jta;

    public void setNick(String nick) {
        this.nick = nick;
        setTitle("JavaChat: " + nick);
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        upperPanel.setVisible(!isAuthorized);
        bottomPanel.setVisible(isAuthorized);
        jspUsers.setVisible(isAuthorized);
        if (!authorized) {
            setNick("");
        }
    }

    public MyWindow() {
        setBounds(600, 300, 600, 400);
        jlUsers = new JList(new DefaultListModel());
        jlUsers.setPreferredSize(new Dimension(120, 1));
        jspUsers = new JScrollPane(jlUsers);
        add(jspUsers, BorderLayout.EAST);
        upperPanel = new JPanel(new GridLayout(1, 3));
        bottomPanel = new JPanel();
        setAuthorized(false);
        add(bottomPanel, BorderLayout.SOUTH);
        add(upperPanel, BorderLayout.NORTH);
        jta = new JTextArea();
        JScrollPane jsp = new JScrollPane(jta);
        add(jsp, BorderLayout.CENTER);
        JButton jbSend = new JButton("Send");
        jta.setEditable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(jbSend, BorderLayout.EAST);
        jtf = new JTextField();
        bottomPanel.add(jtf, BorderLayout.CENTER);
        jtfLogin = new JTextField();
        jpfPass = new JPasswordField();
        JButton jbAuth = new JButton("Auth");
        upperPanel.add(jtfLogin);
        upperPanel.add(jpfPass);
        upperPanel.add(jbAuth);

        jbAuth.addActionListener(e -> sendAuthData());
        jpfPass.addActionListener(e -> sendAuthData());

        jlUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    jtf.setText("/pm " + jlUsers.getSelectedValue().toString() + " ");
                    jtf.grabFocus();
                }
            }
        });

        jbSend.addActionListener(e -> sendMsg());
        jtf.addActionListener(e -> sendMsg());
        tryToConnect();


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    socket.close();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    setAuthorized(false);
                }
            }
        });

        setVisible(true);
    }

    public void tryToConnect() {
        try {
            socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/authok")) {
                            nick = str.split(" ")[1];
                            setAuthorized(true);
                            setNick(nick);
                            jta.setText("");
                            out.writeUTF("/history");
                            break;
                        }

                        jta.append(str + "\n");
                        jta.setCaretPosition(jta.getDocument().getLength());

                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {

                            if (str.startsWith("/history")) {
                                String[] sarr = str.split(",");
                                for (String o : sarr) {
                                    jta.append(o + "\n");
                                }
                                jta.setCaretPosition(jta.getDocument().getLength());
                            }

                            if (str.startsWith("/userslist")) {
                                String[] sarr = str.split(" ");
                                ((DefaultListModel) jlUsers.getModel()).clear();
                                for (int i = 1; i < sarr.length; i++) {
                                    ((DefaultListModel) jlUsers.getModel()).addElement(sarr[i]);
                                }
                                jlUsers.repaint();
                            }

                            if (str.startsWith("/changenick")) {
                                String[] sarr = str.split(" ");
                                setNick(sarr[1]);
                            }


                        } else {
                            jta.append(str + "\n");
                            jta.setCaretPosition(jta.getDocument().getLength());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    setAuthorized(false);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Невозможно подключиться к серверу, проверьте сетевое соединение");
            e.printStackTrace();
        }
    }

    public void sendAuthData() {
        if (socket.isClosed())
            tryToConnect();
        try {
            out.writeUTF("/auth " + jtfLogin.getText() + " " + new String(jpfPass.getPassword()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMsg() {
        if (!jtf.getText().trim().isEmpty()) {
            String s = jtf.getText();
            jtf.setText("");
            jtf.grabFocus();
            try {
                out.writeUTF(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
