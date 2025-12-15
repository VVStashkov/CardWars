package ru.itis.inf400;

import ru.itis.inf400.Cards.Game;
import ru.itis.inf400.Cards.Player;

import java.util.Scanner;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int SERVER_PORT = 50001;
    private static Socket clientSocket;
//    private static BufferedReader in;
//    private static BufferedWriter out;
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
//            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            objOut = new ObjectOutputStream(clientSocket.getOutputStream());
            objOut.flush(); // Важно!
            objIn = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("sdf");

            objIn.readObject();
            setNameOfPlayer();
            send("Имя соперника: " + nameOfPlayer );
            System.out.println(objIn.readObject()); // имя соперника
            game = new Game(player, Client.getPlayer());
            String rule = game.rules();
            System.out.println(rule);
            game.setDefaultAttributes();
            send(game);
            objIn.readObject();
            play();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void play() throws IOException {


        while (game.getServerPlayer().getHp() > 0 && game.getClientPlayer().getHp() > 0) {

            String actions = game.serverStep();
            send(actions);
            objIn.read();

            if (actions.equals("next")) {
                break;

            }
            //принимаем управление
        }

        acceptActions();
    }
    private static void acceptActions() throws IOException {
        send(game);
        while (true) {
            String message;
            try {
                message = (String) objIn.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
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
            send("");
        }
        play();

    }
    //приват ибо метод не может работать при вызове из другого класса
    private static void send(Object str) throws IOException {
        objOut.writeObject(str);
        objOut.flush();
    }

    public static void setNameOfPlayer() {
        System.out.println("Введите имя вашего игрока: ");
        nameOfPlayer = sc.nextLine();
    }
}
