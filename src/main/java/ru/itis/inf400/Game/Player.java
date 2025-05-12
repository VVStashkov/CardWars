package ru.itis.inf400.Game;

import java.net.Socket;
import java.util.List;

public class Player {
    public Socket clientSocket;

    private int hp = 20;
    private List<Card> deck;
    private List<Card> hand;
    private List<Field> fields;

    public Player(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void makeMove() {
        int actionPoint = 2;

        printActionChoose(actionPoint);

    }


    public void printActionChoose(int actionPoint) {
        if (actionPoint > 0) {
            System.out.println("1 - начать ход \n " +
                    "2 - взять карту \n" +
                    "3 - использовать карту ");
        } else {

        }
    }

}
