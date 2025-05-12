package ru.itis.inf400;

import ru.itis.inf400.Game.Game;
import ru.itis.inf400.Game.Player;

import java.util.Scanner;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int SERVER_PORT = 50000;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static Player player;
    private static String name;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(SERVER_PORT);
            System.out.println("start server");
            clientSocket = server.accept();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            startGame();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void startGame() {
        Client.setPlayer(new Player(Client.getClientSocket()));
        player = new Player(clientSocket);
        System.out.println("Кто ходит первый? \n" + "1 - игрок-сервер \n" + "2 - игрок-клиент");
        String c = sc.nextLine();
        if (c.equals("1")) {
            Game game = new Game(player, Client.getPlayer());
            game.start();
        } else if (c.equals("2")){
            Game game = new Game(Client.getPlayer(), player);
            game.start();
        } else {
            System.out.println("Введено некорректное значение");
            startGame();
        }
    }
    public static void setName() {
        System.out.println("Введите своё имя: ");
        name = sc.nextLine();
    }
}
