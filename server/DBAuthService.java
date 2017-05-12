package ru.geekbrains.chat.git.server;

import java.sql.SQLException;

public class DBAuthService implements AuthService {
    public DBAuthService() {
        try {
            SQLHandler.connect();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка запуска сервиса авторизации");
        }
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        return SQLHandler.getNickByLoginPass(login, pass);
    }

    @Override
    public void setChangeNick(String nick, String new_nick) {
        SQLHandler.setChangeNick(nick, new_nick);
    }

    @Override
    public void shutdown() {
        SQLHandler.disconnect();
        System.out.println("Авторизация по Базе данных завершила работу");
    }
}
