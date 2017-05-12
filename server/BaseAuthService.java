package ru.geekbrains.chat.git.server;

import java.util.ArrayList;

public class BaseAuthService implements AuthService {
    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public void setNick(String nick) {
            this.nick = nick;
        }

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            //if (login != null ? !login.equals(entry.login) : entry.login != null) return false;
          //  if (pass != null ? !pass.equals(entry.pass) : entry.pass != null) return false;
            return nick != null ? nick.equals(entry.nick) : entry.nick == null;
        }

    }

    private ArrayList<Entry> entries;


    public BaseAuthService() {
        this.entries = new ArrayList<>();
        this.entries.add(new Entry("login1", "pass1", "nick1"));
        this.entries.add(new Entry("login2", "pass2", "nick2"));
        this.entries.add(new Entry("login3", "pass3", "nick3"));
        this.entries.add(new Entry("login4", "pass4", "nick4"));
        this.entries.add(new Entry("login5", "pass5", "nick5"));
    }

    @Override
    public void setChangeNick(String nick, String new_nick) {
//Возможна проблемма если есть несколько человек с одинаковым ником, то команда на замену от одного  сменит ники у всех
        for (Entry o : entries) {
            if (o.nick.equals(nick)) {
                o.setNick(new_nick);
            }
        }
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) {
                return o.nick;
            }
        }
        return null;
    }

    @Override
    public void shutdown() {
        System.out.println("Базовый сервис авторизации завершил работу");
    }
}
