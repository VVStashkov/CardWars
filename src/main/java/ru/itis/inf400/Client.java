package ru.itis.inf400;

import ru.itis.inf400.Game.Player;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String nameOfPlayer;
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

            player = new Player();
            send("");
            System.out.println(in.readLine());
            setNameOfPlayer();
            send("Имя соперника: " + nameOfPlayer + "\n");
            System.out.println(in.readLine());
            System.out.println(in.readLine());//прочитали правила


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //приват ибо метод не может работать при вызове из другого класса
    private static void send(String str) throws IOException {
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        out.write(str + "\n");
        out.flush();
    }
    public static void setNameOfPlayer() {
        System.out.println("Введите имя вашего игрока: ");
        nameOfPlayer = sc.nextLine();
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
