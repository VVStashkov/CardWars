package ru.itis.inf400.Game;


import ru.itis.inf400.Game.Warriors.Buffer;
import ru.itis.inf400.Game.Warriors.Healer;
import ru.itis.inf400.Game.Warriors.Swordsman;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Game implements Serializable{
    private Player serverPlayer;
    private Player clientPlayer;
    private int winsOfServer;
    private int winsOfClient;
//    private Scanner sc = new Scanner(System.in);

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
        Scanner sc = new Scanner(System.in);
        String actions = "";
        boolean flag = true;
        while (flag) {
            try {
                flag = false;
                player.printActionChoose();
                String choose = sc.nextLine();
                switch (choose) {
                    case "1": {
                        player.attack(enemyplayer);
                        actions += "next";
                        break;
                    }
                    case "2": {
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
                    }
                    case "3": {
                        player.printHand();
                        System.out.println("Введите номер разыгрываемой карты ");
                        int num = Integer.parseInt(sc.nextLine());

                        player.put(num, enemyplayer);
                        actions += "разыграл карту: " + player.getHand().get(num).getDescription();
                        break;
                        //....
                    }
                    case "4": {
                        player.printHand();
                        actions += "посмотрел свои карты";
                        break;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Введено неверное значение");
                flag = true;
            }
        }
        return actions;
    }

    //метод установит дефолтные колоды и поля для всех игроков;
    public void setDefaultAttributes() {
        setServerPlayer();
        setClientPlayer();
    }
    public void setServerPlayer() {

        List<Field> fields = new ArrayList<>(4);
        fields.add(new Field(FieldTypes.BLUEFIELD));
        fields.add(new Field(FieldTypes.BLUEFIELD));
        fields.add(new Field(FieldTypes.BLUEFIELD));
        fields.add(new Field(FieldTypes.BLUEFIELD));
        serverPlayer.setFields(fields);
        serverPlayer.setDeck(createDeck());
        for (int i = 0; i < 5; ++i) {
            serverPlayer.takeCard();
        }
        serverPlayer.setActionPoint(2);
    }
    public void setClientPlayer() {
        List<Field> fields = new ArrayList<>(4);
        fields.add(new Field(FieldTypes.CORNFIELD));
        fields.add(new Field(FieldTypes.CORNFIELD));
        fields.add(new Field(FieldTypes.CORNFIELD));
        fields.add(new Field(FieldTypes.CORNFIELD));
        clientPlayer.setFields(fields);
        clientPlayer.setDeck(createDeck());
        for (int i = 0; i < 5; ++i) {
            clientPlayer.takeCard();
        }
        clientPlayer.setActionPoint(2);
    }

    public List<Card> createDeck() {
        List<Card> deck = new LinkedList<>();
        deck.add(new Swordsman());
        deck.add(new Buffer());
        deck.add(new Swordsman());
        deck.add(new Swordsman());
        deck.add(new Healer());
        deck.add(new Swordsman());
        deck.add(new Swordsman());
        deck.add(new Buffer());
        deck.add(new Swordsman());
        deck.add(new Buffer());
        deck.add(new Healer());
        deck.add(new Healer());
        deck.add(new Buffer());
        deck.add(new Swordsman());
        deck.add(new Buffer());
        deck.add(new Healer());
        deck.add(new Buffer());
        deck.add(new Swordsman());
        deck.add(new Swordsman());
        deck.add(new Healer());
        return deck;
    }

    public Player getServerPlayer() {
        return serverPlayer;
    }

    public Player getClientPlayer() {
        return clientPlayer;
    }
}
