package ru.geekbrains.chat.git.server;

import java.sql.*;
import java.util.ArrayList;

public class SQLHandler {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            //Если изменить ЮРЛ к базе данных (или ее самой уже не будет) то програма не выдает ошибку... по возможности дорабоать...
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:db.db");
            } catch (SQLException e) {
                e.printStackTrace();

            }
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList getMsg() {
        ArrayList<String> arl = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT *FROM history");
            while (rs.next()) {
                arl.add(rs.getString(1) + " " + rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return arl;
    }

    public static void setMsg(String nick, String msg) {
        try {
            stmt.execute("INSERT INTO history  (Nick, Msg) VALUES ('" + nick + " ', '" + msg + " ')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMsg() {
        try {
            stmt.execute("DELETE FROM history");
        } catch (Exception e) {
        }
    }

    public static void setChangeNick(String nick, String new_nick) {
        //Возможна проблемма если есть несколько человек с одинаковым ником, то команда на замену от одного  сменит ники у всех
        try {
            stmt.execute("UPDATE users SET Nick = '" + new_nick + "' WHERE Nick = '" + nick + "';");
        } catch (Exception e) {
        }
    }

    public static String getNickByLoginPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT Nick FROM users WHERE Login = '" + login + "' AND Pass = '" + pass + "';");
            if (!rs.next()) return null;
            return rs.getString(1);
        } catch (Exception e) {
        }
        return null;
    }

    public void examples() {
        //        try {
//            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
//            while(rs.next()) {
//                System.out.println(rs.getInt(1) + " " + rs.getString(2));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        try {
//            stmt.execute("DELETE FROM users WHERE Login = 'newLogin';");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

//        try {
//            stmt.execute("UPDATE users SET Login = 'newLogin' WHERE Login = 'Login4';");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

//        try {
//            stmt.execute("INSERT INTO users (Login, Pass, Nick) VALUES ('login4', 'pass4', 'nick4')");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
}
