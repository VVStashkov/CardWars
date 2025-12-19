package ru.itis.inf400.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 5555;
    private final RoomManager roomManager;
    private final ExecutorService executorService;
    private volatile boolean running;

    public GameServer() {
        this.roomManager = new RoomManager();
        this.executorService = Executors.newCachedThreadPool();
        this.running = true;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("GameServer started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Создаем обработчик для клиента
                ClientHandler clientHandler = new ClientHandler(clientSocket, roomManager);
                executorService.submit(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}