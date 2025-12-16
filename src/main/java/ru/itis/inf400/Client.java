package ru.itis.inf400;

import ru.itis.inf400.Cards.Game;
import ru.itis.inf400.Cards.Player;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String nameOfPlayer;
    private static Socket clientSocket;
//    private static BufferedReader in;
//    private static BufferedWriter out;
    private static Player player = new Player();
    private static Scanner sc = new Scanner(System.in);
    private static ObjectInputStream objIn;
    private static ObjectOutputStream objOut;
    public static Game game;


    public static void main(String[] args) {

        try {

            clientSocket = new Socket("127.0.0.1", 50001);
            System.out.println("gjl");
//            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            objIn = new ObjectInputStream(clientSocket.getInputStream());
            objOut = new ObjectOutputStream(clientSocket.getOutputStream());
            objOut.flush();
            System.out.println("SDF");

            send("");
            System.out.println(objIn.readObject());//получили имя
            setNameOfPlayer();
            send("Имя соперника: " + nameOfPlayer + "\n");




            game = (Game) objIn.readObject();
            String rule = game.rules();
            System.out.println(rule);
            acceptActions();



        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
                send("");//позволяем отправить объект серверу
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

    public static void play() throws IOException {

        while (game.getServerPlayer().getHp() > 0 && game.getClientPlayer().getHp() > 0) {
            String actions = game.clientStep();
            send(actions);
            objIn.read();
            if (actions.equals("next")) {
                break;

            }
            //принимаем управление
        }
        System.out.println("sdfsdfss");

        acceptActions();
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
    public static void setPlayer(Player player) {
        Client.player = player;
    }

    public static Socket getClientSocket() {
        return clientSocket;
    }

    public static Player getPlayer() {
        return player;
    }
}
