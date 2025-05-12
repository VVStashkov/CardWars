package ru.itis.inf400.Game;

import ru.itis.inf400.Client;
import ru.itis.inf400.Server;

import java.io.*;
import java.util.Scanner;

public class Game {
    private Player player1;
    private Player player2;
    private int winsOfPlayer1;
    private int winsOfPlayer2;
    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void start(){
        try {
            setNamesOfPlayers();
            printRules();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void setNamesOfPlayers() throws IOException {
        DataOutputStream outputStream = new DataOutputStream(Client.getClientSocket().getOutputStream());
        outputStream.writeUTF("Введите своё имя:");
        Client.setName();
        Server.setName();
    }
    public void printRules() throws FileNotFoundException {
        try {


            Scanner sc = new Scanner(new File("rules.txt"));
            String rule = "";
            while (sc.hasNextLine()) {
                rule = rule + sc.nextLine();
            }

            DataOutputStream outputStream1 = new DataOutputStream(player1.clientSocket.getOutputStream());
            DataOutputStream outputStream2 = new DataOutputStream(player2.clientSocket.getOutputStream());
            outputStream1.writeUTF(rule);
            outputStream2.writeUTF(rule);
            //стримами передать правила игрокам
        } catch (IOException e) {

        }

    }
}
