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
    private static String nameOfPlayer;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(SERVER_PORT);
            System.out.println("start server");
            clientSocket = server.accept();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            in.read();
            setNameOfPlayer();
            send("Имя соперника: " + nameOfPlayer + "\n");
            System.out.println(in.readLine());
            startGame();
            while (true) {
                System.out.println(in.readLine());

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void send(String str) throws IOException {
        out.write(str + "\n");
        out.flush();
    }

    //А зачем вообще давать такой выбор, можем же просто сделать по базе первым игроком сервер, типа
    //типа пускай пользователь сам решит кто хост и кто первый ходит
    public static void startGame() {
        player = new Player();
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
    public static void setNameOfPlayer() {
        System.out.println("Введите имя вашего игрока: ");
        nameOfPlayer = sc.nextLine();
    }
}
