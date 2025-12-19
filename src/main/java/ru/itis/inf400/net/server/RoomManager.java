package ru.itis.inf400.net.server;

import ru.itis.inf400.net.dto.GameMessage;
import ru.itis.inf400.net.dto.MessageType;
import ru.itis.inf400.net.dto.records.StartGame;
import ru.itis.inf400.net.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager {
    private final Map<String, Room> rooms;
    private final AtomicInteger roomIdCounter;
    private final Map<Integer, ClientHandler> connectedClients;

    public RoomManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.roomIdCounter = new AtomicInteger(1);
        this.connectedClients = new ConcurrentHashMap<>();
    }

    public String createRoom(ClientHandler creator) {
        String roomName = "room_" + roomIdCounter.getAndIncrement();
        Room room = new Room(roomName);
        room.addPlayer(creator);
        rooms.put(roomName, room);

        creator.setRoom(room);
        creator.setPlayerId(1); // Первый игрок получает id = 1

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
        joiner.setPlayerId(2); // Второй игрок получает id = 2

        System.out.println("Player " + joiner.getPlayerId() + " joined room: " + roomName);
        return true;
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

    public void registerClient(int clientId, ClientHandler handler) {
        connectedClients.put(clientId, handler);
    }

    public void unregisterClient(int clientId) {
        connectedClients.remove(clientId);
    }
}