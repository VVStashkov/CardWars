package ru.itis.inf400.net.client;

import ru.itis.inf400.net.dto.GameMessage;
import ru.itis.inf400.net.dto.MessageType;
import ru.itis.inf400.net.dto.records.CardPlaceInGameType;
import ru.itis.inf400.net.dto.records.GameState;
import ru.itis.inf400.net.dto.records.PutCard;
import ru.itis.inf400.net.dto.records.StartGame;
import ru.itis.inf400.net.server.Room;
import ru.itis.inf400.net.util.JsonUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientProcessor {

    private String roomName;
    private DataInputStream in;
    private DataOutputStream out;
    private GameClient gameClient;

    public ClientProcessor(String roomName, Socket socket, GameClient gameClient) {
        this.roomName = roomName;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.gameClient = gameClient;
    }

    public void sendGameStart() {
        StartGame startGame = new StartGame(gameClient.getId(), roomName);
        String jsonData = JsonUtil.toJson(startGame);
        GameMessage gameMessage = new GameMessage(MessageType.GAME_START, jsonData);
        sendMessage(gameMessage);
        //todo получение сообщения?
    }

    public void sendGetCard() {
        //todo
    }

    public void sendPutCard(int positionInHand, int requiredPositionOnField) {
        PutCard putCard = new PutCard(gameClient.getId(), roomName, positionInHand, requiredPositionOnField);
        //todo
    }

    public void sendAttack() {
        //todo
    }

    private void sendMessage(GameMessage gameMessage) {
        try {
            byte[] data = gameMessage.toByteArray();
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void recieveGameMessage() throws IOException {
        try {
            int length = in.readInt();
            byte[] data = new byte[length];
            in.read(data);
            GameMessage gameMessage = GameMessage.fromByteArray(data);
            GameState gameState = JsonUtil.fromJson(new String(data), GameState.class);
            gameClient.updateView(gameState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
