package ru.itis.inf400.Game;


import java.io.*;
import java.util.Scanner;

public class Game {
    private Player serverPlayer;
    private Player clientPlayer;
    private int winsOfServer;
    private int winsOfClient;
    private Scanner sc = new Scanner(System.in);

    public Game(Player serverPlayer, Player clientPlayer) {
        this.serverPlayer = serverPlayer;
        this.clientPlayer = clientPlayer;
    }

    public String rules(){
        String rule = "";
        try {
            Scanner sc = new Scanner(new File("rules.txt"));

            while (sc.hasNextLine()) {
                rule = rule + sc.nextLine() + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rule;

    }

    public String serverStep() {
        return play(serverPlayer, clientPlayer);
    }
    public String clientStep() {
        return play(clientPlayer, serverPlayer);
    }


        public String play(Player player, Player enemyplayer) {
        String actions = "";
        boolean flag = true;
        while (flag) {
            player.printActionChoose();
            String choose = sc.nextLine();
            switch (choose) {
                case "1": {
                    player.attack(enemyplayer);
                    actions += "произвёл атаку";
                    flag = false;
                    break;
                } case "2": {
                    System.out.println("1 - воина + \n + 2 - здание");
                    if (sc.nextLine().equals("1")) {
                        player.printFlupableWarrior();
                        System.out.println("Введите номер поля для флюпа");
                        int p = Integer.parseInt(sc.nextLine());
                        player.flupWarrior(p - 1, enemyplayer);// -1 для отчёта от 0
                        actions += "флюпнул воина " + p;
                    } else {
                        player.printFlupableBuildings();
                        System.out.println("Введите номер поля для флюпа");
                        int p = Integer.parseInt(sc.nextLine());
                        player.flupBuilding(p, enemyplayer);
                        actions += "флюпнул строение " + p;
                    }
                    break;
                } case "3": {
                    player.printHand();
                    System.out.println("Введите номер разыгрываемой карты ");
                    int num = Integer.parseInt(sc.nextLine());
                    actions += "разыграл карту: " + player.getHand().get(num).getDescription();
                    player.put(num,enemyplayer);
                    break;
                    //....
                } case "4": {
                    player.printHand();
                    break;
                }
            }
        }
        return actions;
    }

    //метод установит дефолтные колоды и поля для всех игроков;
    //
    public void setAttributes() {

    }

    public Player getServerPlayer() {
        return serverPlayer;
    }

    public Player getClientPlayer() {
        return clientPlayer;
    }
}
