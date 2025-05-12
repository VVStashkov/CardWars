package ru.itis.inf400;

import ru.itis.inf400.Game.Player;

import java.io.*;
import java.lang.management.PlatformLoggingMXBean;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String name;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static Player player;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            clientSocket = new Socket("127.0.0.1", 50000);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void setName() {
        System.out.println("Введите своё имя: ");
        name = sc.nextLine();
    }
    public static void setPlayer(Player player) {
        Client.player = player;
    }

    public static Socket getClientSocket() {
        return clientSocket;
    }

    public static BufferedReader getIn() {
        return in;
    }

    public static BufferedWriter getOut() {
        return out;
    }

    public static Player getPlayer() {
        return player;
    }
}
