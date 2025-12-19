package ru.itis.inf400.net.server;

import ru.itis.inf400.net.dto.GameMessage;
import ru.itis.inf400.net.dto.MessageType;
import ru.itis.inf400.net.dto.records.ErrorInfo;
import ru.itis.inf400.net.dto.records.JoinRoomRequest;
import ru.itis.inf400.net.dto.records.RoomCreatedResponse;
import ru.itis.inf400.net.dto.records.RoomInfo;
import ru.itis.inf400.net.dto.records.RoomsList;
import ru.itis.inf400.net.dto.records.StartGame;
import ru.itis.inf400.net.util.JsonUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final RoomManager roomManager;
    private Room room;
    private int playerId;
    private volatile boolean connected;

    public ClientHandler(Socket socket, RoomManager roomManager) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.roomManager = roomManager;
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            while (connected && !socket.isClosed()) {
                // Читаем длину сообщения
                int length = in.readInt();
                byte[] data = new byte[length];
                in.readFully(data);

                // Десериализуем сообщение
                GameMessage message = GameMessage.fromByteArray(data);
                processMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            disconnect();

            // Удаляем игрока из комнаты, если он был в ней
            if (room != null) {
                room.closeRoom();
                roomManager.removeRoom(room.getName());
            }
        }
    }

    private void processMessage(GameMessage message) {
        switch (message.getType()) {
            case CREATE_ROOM:
                handleCreateRoom(message);
                break;
            case GET_ROOMS:
                handleRoomsList();
                break;
            case JOIN_ROOM:
                handleJoinRoom(message);
                break;
            case GAME_START:
                handleGameStart(message);
                break;
            default:
                // Если игрок уже в комнате, передаем сообщение в комнату
                if (room != null) {
                    room.processAction(message, this);
                }
        }
    }

    private void handleCreateRoom(GameMessage message) {
        String roomName = roomManager.createRoom(this);

        // Отправляем подтверждение клиенту
        RoomCreatedResponse response = new RoomCreatedResponse(roomName, playerId);
        GameMessage responseMessage = new GameMessage(
                MessageType.ROOM_CREATED,
                JsonUtil.toJson(response)
        );

        try {
            sendMessage(responseMessage);
        } catch (IOException e) {
            System.err.println("Failed to send room created response: " + e.getMessage());
        }
    }

    private void handleRoomsList() {
        RoomsList response = new RoomsList(roomManager.getRoomNames());
        MessageType messageType = MessageType.ROOMS_LIST;
        GameMessage responseMessage = new GameMessage(
                messageType,
                JsonUtil.toJson(response));
        try {
            sendMessage(responseMessage);
        } catch (IOException e) {
            System.err.println("Failed to send join room response: " + e.getMessage());
        }
    }

    private void handleJoinRoom(GameMessage message) {
        JoinRoomRequest request = JsonUtil.fromJson(message.getPayloadAsString(), JoinRoomRequest.class);
        boolean success = roomManager.joinRoom(request.roomName(), this);

        MessageType responseType = success ? MessageType.ROOM_JOINED : MessageType.ROOM_NOT_FOUND;
        RoomInfo response = new RoomInfo(request.roomName(), playerId);
        GameMessage responseMessage = new GameMessage(responseType, JsonUtil.toJson(response));

        try {
            sendMessage(responseMessage);
        } catch (IOException e) {
            System.err.println("Failed to send join room response: " + e.getMessage());
        }
    }

    private void handleGameStart(GameMessage message) {
        StartGame startGame = JsonUtil.fromJson(message.getPayloadAsString(), StartGame.class);
        boolean success = roomManager.startGame(startGame.roomName(), startGame.clientId());

        if (!success) {
            sendError("Cannot start game");
        }
    }

    public void sendMessage(GameMessage message) throws IOException {
        byte[] data = message.toByteArray();
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    private void sendError(String errorMessage) {
        ErrorInfo error = new ErrorInfo(errorMessage);
        GameMessage errorMessageObj = new GameMessage(
                MessageType.ERROR,
                JsonUtil.toJson(error)
        );

        try {
            sendMessage(errorMessageObj);
        } catch (IOException e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            // Игнорируем ошибки при закрытии
        }
    }

    // Getters and Setters
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public boolean isConnected() { return connected; }
}

