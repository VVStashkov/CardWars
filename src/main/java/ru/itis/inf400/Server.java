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
    private static ObjectInputStream objIn;
    private static ObjectOutputStream objOut;
    private static Player player = new Player();
    private static String nameOfPlayer;
    private static Scanner sc = new Scanner(System.in);

    public static Game game;

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(SERVER_PORT);
            System.out.println("start server");
            clientSocket = server.accept();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            objIn = new ObjectInputStream(clientSocket.getInputStream());
            objOut = new ObjectOutputStream(clientSocket.getOutputStream());
            in.read();
            setNameOfPlayer();
            send("Имя соперника: " + nameOfPlayer );
            System.out.println(in.readLine()); // имя соперника
            game = new Game(player, Client.getPlayer());
            String rule = game.rules();


            System.out.println(rule);//прочитали правила
            send("");
            in.read();
            play();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void play() throws IOException {

        while (game.getServerPlayer().getHp() > 0 && game.getClientPlayer().getHp() > 0) {
            String actions = game.serverStep();
            send(actions);
            in.read();
            if (actions.equals("next")) {
                break;

            }
            //принимаем управление
        }

        acceptActions();
    }
    private static void acceptActions() throws IOException {
        objOut.writeObject(game);
        while (true) {
            String message = in.readLine();
            System.out.println(message);
            if (message.equals("next")) {
                send("");
                try {
                    game = (Game) objIn.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        play();

    }
    //приват ибо метод не может работать при вызове из другого класса
    private static void send(String str) throws IOException {
        out.write(str + "\n");
        out.flush();
    }

    public static void setNameOfPlayer() {
        System.out.println("Введите имя вашего игрока: ");
        nameOfPlayer = sc.nextLine();
    }
}
