package ru.geekbrains.chat.git.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

//По запросу вывод списка служебных комманд

public class MainClass {
    public static void main(String[] args) {
        new Server();
    }
}
