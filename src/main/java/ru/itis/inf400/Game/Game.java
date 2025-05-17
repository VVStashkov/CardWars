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
            printRules();
            //возможно на это и стоит закончить
    }
    public String printRules(){
        String rule = "";
        try {
            Scanner sc = new Scanner(new File("rules.txt"));

            while (sc.hasNextLine()) {
                rule = rule + sc.nextLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rule;

    }
}
