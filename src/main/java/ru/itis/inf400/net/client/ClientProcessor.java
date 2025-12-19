// ClientProcessor.java (упрощенная версия без интерфейса)
package ru.itis.inf400.net.client;

import ru.itis.inf400.net.dto.GameMessage;
import ru.itis.inf400.net.dto.MessageType;
import ru.itis.inf400.net.dto.records.*;
import ru.itis.inf400.net.util.JsonUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientProcessor {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private GameClientApp gameClientApp;
    private ExecutorService receiverExecutor;
    private volatile boolean connected;

    private int playerId;
    private String roomName;

    public ClientProcessor(GameClientApp gameClientApp, Socket socket) throws IOException {
        this.gameClientApp = gameClientApp;
        this.socket = socket;
        this.connected = true;
        this.receiverExecutor = Executors.newSingleThreadExecutor();

        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        startReceiver();
    }

    private void startReceiver() {
        receiverExecutor.submit(() -> {
            while (connected && !socket.isClosed()) {
                try {
                    int length = in.readInt();
                    byte[] data = new byte[length];
                    in.readFully(data);

                    GameMessage gameMessage = GameMessage.fromByteArray(data);
                    processIncomingMessage(gameMessage);
                } catch (IOException e) {
                    if (connected) {
                        gameClientApp.showAlert("Connection lost: " + e.getMessage());
                        disconnect();
                    }
                    break;
                }
            }
        });
    }

    private void processIncomingMessage(GameMessage gameMessage) {
        try {
            switch (gameMessage.getType()) {
                case ROOM_CREATED:
                    RoomInfo roomInfo = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            RoomInfo.class
                    );
                    this.playerId = roomInfo.playerId();
                    this.roomName = roomInfo.roomName();
                    gameClientApp.onRoomCreated(roomInfo.roomName(), roomInfo.playerId());
                    break;

                case ROOM_JOINED:
                    RoomInfo joinedRoomInfo = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            RoomInfo.class
                    );
                    this.playerId = joinedRoomInfo.playerId();
                    this.roomName = joinedRoomInfo.roomName();
                    gameClientApp.onRoomJoined(joinedRoomInfo.roomName(), joinedRoomInfo.playerId());
                    break;

                case ROOM_NOT_FOUND:
                    gameClientApp.onRoomNotFound(roomName);
                    break;

                case ROOM_FULL:
                    gameClientApp.onRoomFull(roomName);
                    break;

                case ROOMS_LIST:
                    RoomsList roomsList = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            RoomsList.class
                    );
                    gameClientApp.onRoomsList(roomsList.rooms());
                    break;

                case GAME_START:
                    GameState gameState = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            GameState.class
                    );
                    gameClientApp.onGameStart(gameState);
                    break;

                case STATE_UPDATE:
                    GameState updatedGameState = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            GameState.class
                    );
                    gameClientApp.onGameStateUpdate(updatedGameState);
                    break;

                case PLAYER_TURN:
                    PlayerTurn playerTurn = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            PlayerTurn.class
                    );
                    gameClientApp.onPlayerTurn(playerTurn.clientId());
                    break;

                case GAME_OVER:
                    GameOver gameOver = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            GameOver.class
                    );
                    gameClientApp.onGameOver(gameOver.clientId());
                    break;

                case ERROR:
                    ErrorInfo error = JsonUtil.fromJson(
                            gameMessage.getPayloadAsString(),
                            ErrorInfo.class
                    );
                    gameClientApp.onError(error.message());
                    break;

                default:
                    System.out.println("Unhandled message type: " + gameMessage.getType());
            }
        } catch (Exception e) {
            gameClientApp.onError("Error processing message: " + e.getMessage());
        }
    }

    // Методы для отправки действий
    public void sendCreateRoom() {
        sendMessage(MessageType.CREATE_ROOM, "{}");
    }

    public void sendJoinRoom(String roomName) {
        JoinRoomRequest joinRoomRequest = new JoinRoomRequest(roomName);
        sendMessage(MessageType.JOIN_ROOM, joinRoomRequest);
    }

    public void sendGetRooms() {
        sendMessage(MessageType.GET_ROOMS, "{}");
    }

    public void sendGameStart() {
        StartGame startGame = new StartGame(playerId, roomName);
        sendMessage(MessageType.GAME_START, startGame);
    }

    public void sendGetCard() {
        GetCard getCard = new GetCard(playerId, roomName);
        sendMessage(MessageType.GET_CARD, getCard);
    }

    public void sendPutCard(int positionInHand, int requiredPositionOnField) {
        PutCard putCard = new PutCard(playerId, roomName, positionInHand, requiredPositionOnField);
        sendMessage(MessageType.PUT_CARD, putCard);
    }

    public void sendAttack() {
        Attack attack = new Attack(playerId, roomName);
        sendMessage(MessageType.ATTACK, attack);
    }

    public void sendQuitGame() {
        QuitGame quitGame = new QuitGame(playerId);
        sendMessage(MessageType.QUIT_GAME, quitGame);
    }

    private void sendMessage(MessageType messageType, Object message) {
        if (!connected) {
            gameClientApp.onError("Not connected to server");
            return;
        }

        try {
            String jsonData;
            if (message instanceof String) {
                jsonData = (String) message;
            } else {
                jsonData = JsonUtil.toJson(message);
            }

            GameMessage gameMessage = new GameMessage(messageType, jsonData);
            byte[] data = gameMessage.toByteArray();

            synchronized (out) {
                out.writeInt(data.length);
                out.write(data);
                out.flush();
            }
        } catch (IOException e) {
            gameClientApp.onError("Failed to send message: " + e.getMessage());
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;
        receiverExecutor.shutdown();

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Игнорируем ошибки при закрытии
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getRoomName() {
        return roomName;
    }
}