package ru.itis.inf400.net.server;

import ru.itis.inf400.net.dto.GameMessage;
import ru.itis.inf400.net.dto.MessageType;
import ru.itis.inf400.net.dto.records.PlayerJoined;
import ru.itis.inf400.net.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager {
    private final Map<String, Room> rooms;
    private final AtomicInteger roomIdCounter;

    public RoomManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.roomIdCounter = new AtomicInteger(1);
    }

    public String createRoom(ClientHandler creator) {
        String roomName = "room_" + roomIdCounter.getAndIncrement();
        Room room = new Room(roomName);
        room.addPlayer(creator);
        rooms.put(roomName, room);

        creator.setRoom(room);
        creator.setPlayerId(1);

        System.out.println("Room created: " + roomName + " by player " + creator.getPlayerId());
        return roomName;
    }

    public boolean joinRoom(String roomName, ClientHandler joiner) {
        Room room = rooms.get(roomName);
        if (room == null) {
            return false;
        }

        if (room.isFull()) {
            return false;
        }

        room.addPlayer(joiner);
        joiner.setRoom(room);
        joiner.setPlayerId(2);

        System.out.println("Player " + joiner.getPlayerId() + " joined room: " + roomName);

        // Уведомляем первого игрока о присоединении второго
        notifyPlayerJoined(room, joiner.getPlayerId());

        return true;
    }

    private void notifyPlayerJoined(Room room, int playerId) {
        // Отправляем уведомление первому игроку (playerId = 1)
        ClientHandler firstPlayer = room.getPlayer(1);
        if (firstPlayer != null) {
            try {
                PlayerJoined playerJoined = new PlayerJoined(playerId);
                GameMessage message = new GameMessage(
                        MessageType.PLAYER_JOINED,
                        JsonUtil.toJson(playerJoined)
                );
                firstPlayer.sendMessage(message);
                System.out.println("Notified player 1 about player " + playerId + " joined");
            } catch (IOException e) {
                System.err.println("Failed to send player joined notification: " + e.getMessage());
            }
        }
    }

    public boolean startGame(String roomName, int clientId) {
        Room room = rooms.get(roomName);
        if (room == null || !room.hasPlayer(clientId)) {
            return false;
        }

        // Проверяем, что в комнате 2 игрока
        if (room.getPlayerCount() < 2) {
            return false;
        }

        room.startGame();
        return true;
    }



    public Room getRoom(String roomName) {
        return rooms.get(roomName);
    }

    public List<String> getRoomNames() {
        return List.copyOf(rooms.keySet());
    }

    public void removeRoom(String roomName) {
        rooms.remove(roomName);
        System.out.println("Room removed: " + roomName);
    }
}