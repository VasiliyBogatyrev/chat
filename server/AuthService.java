package ru.geekbrains.chat.git.server;

public interface AuthService {
    String getNickByLoginPass(String login, String pass);
    void shutdown();
    void setChangeNick (String nick, String new_nick);
}
