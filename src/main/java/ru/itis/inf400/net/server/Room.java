package ru.itis.inf400.net.server;

import ru.itis.inf400.Cards.Game;
import ru.itis.inf400.Cards.Player;
import ru.itis.inf400.net.dto.GameMessage;
import ru.itis.inf400.net.dto.MessageType;
import ru.itis.inf400.net.dto.records.*;
import ru.itis.inf400.net.dto.records.GameOver;
import ru.itis.inf400.net.util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Room {
    private final String name;
    private final Map<Integer, ClientHandler> players;
    private Game game;
    private volatile boolean gameStarted;
    private int currentTurnPlayerId;

    public Room(String name) {
        this.name = name;
        this.players = new HashMap<>();
        this.gameStarted = false;
    }

    public synchronized void addPlayer(ClientHandler player) {
        if (players.size() >= 2) {
            throw new IllegalStateException("Room is full");
        }

        int playerId = players.size() + 1;
        players.put(playerId, player);

        // Уведомляем игрока о его id
        PlayerInfo playerInfo = new PlayerInfo(playerId, name);
        GameMessage message = new GameMessage(MessageType.ROOM_JOINED, JsonUtil.toJson(playerInfo));
        sendToPlayer(playerId, message);

        System.out.println("Player " + playerId + " added to room " + name);
    }

    public void startGame() {
        if (gameStarted) {
            return;
        }

        gameStarted = true;

        // Создаем игровых персонажей (объекты Player)
        Player player1 = Player.createPlayer(1);
        Player player2 = Player.createPlayer(2);

        // Инициализируем игру с двумя игроками
        game = new Game(player1, player2);
        game.setCurrentTurnPlayerId(1);

        currentTurnPlayerId = 1;

        // Отправляем сообщение о начале игры
        GameStartInfo startInfo = new GameStartInfo(name, currentTurnPlayerId);
        GameMessage startMessage = new GameMessage(
                MessageType.GAME_START,
                JsonUtil.toJson(startInfo)
        );
        broadcast(startMessage);

        // Отправляем начальное состояние игры
        updateGameState();

        System.out.println("Game started in room: " + name);
        System.out.println("First player to move: " + currentTurnPlayerId);
    }

    public synchronized void processAction(GameMessage message, ClientHandler sender) {
        if (!gameStarted) {
            handlePreGameAction(message, sender);
            return;
        }

        // Проверяем, что ход делает текущий игрок
        if (sender.getPlayerId() != currentTurnPlayerId) {
            sendError(sender, "Not your turn");
            return;
        }

        try {
            switch (message.getType()) {
                case GET_CARD:
                    handleGetCard(message, sender);
                    break;
                case PUT_CARD:
                    handlePutCard(message, sender);
                    break;
                case USE_FLUP:
                    handleUseFlup(message, sender);
                    break;
                case ATTACK:
                    handleAttack(message, sender);
                    break;
                case QUIT_GAME:
                    handleQuitGame(message, sender);
                    break;
                default:
                    sendError(sender, "Unknown action type");
            }
        } catch (Exception e) {
            sendError(sender, "Error processing action: " + e.getMessage());
        }
    }

    private void handleGetCard(GameMessage message, ClientHandler sender) {
        GetCard getCard = JsonUtil.fromJson(message.getPayloadAsString(), GetCard.class);

        if (!validatePlayerAction(sender, getCard.clientId())) {
            return;
        }

        // Выполняем действие через игровую логику
        boolean success = game.takeCard(getCard.clientId());
        if (!success) {
            sendError(sender, "Cannot take card");
            return;
        }

        // Отправляем обновленное состояние
        updateGameState();
    }

    private void handlePutCard(GameMessage message, ClientHandler sender) {
        PutCard putCard = JsonUtil.fromJson(message.getPayloadAsString(), PutCard.class);

        if (!validatePlayerAction(sender, putCard.clientId())) {
            return;
        }

        // Выполняем действие
        boolean success = game.putCard(
                putCard.clientId(),
                putCard.positionInHand(),
                putCard.requiredPositionOnField()
        );

        if (!success) {
            sendError(sender, "Cannot place card");
            return;
        }

        // Отправляем обновленное состояние
        updateGameState();
    }

    private void handleUseFlup(GameMessage message, ClientHandler sender) {
        FlupAction flup = JsonUtil.fromJson(message.getPayloadAsString(), FlupAction.class);
        boolean success = game.useFlup(flup.clientId(), flup.fieldIndex(), flup.isWarrior());

        if (!success) {
            sendError(sender, "Cannot place card");
            return;
        }

        // Отправляем обновленное состояние
        updateGameState();
    }

    private void handleAttack(GameMessage message, ClientHandler sender) {
        Attack attack = JsonUtil.fromJson(message.getPayloadAsString(), Attack.class);

        if (!validatePlayerAction(sender, attack.clientId())) {
            return;
        }

        // Выполняем действие
        boolean success = game.attack(attack.clientId());
        if (!success) {
            sendError(sender, "Cannot attack");
            return;
        }

        // Проверяем окончание игры
        if (game.isGameOver()) {
            handleGameOver(game.getWinnerId());
            return;
        }

        // Отправляем обновленное состояние
        updateGameState();

        // Уведомляем о смене хода
        switchTurn();
    }

    private void handleQuitGame(GameMessage message, ClientHandler sender) {
        QuitGame quitGame = JsonUtil.fromJson(message.getPayloadAsString(), QuitGame.class);

        // Выполняем выход из игры
        boolean success = game.quitGame(quitGame.clientId());
        if (success) {
            handleGameOver(game.getWinnerId());
        }
    }

    private void sendPlayerTurnNotification() {

        PlayerTurn playerTurn = new PlayerTurn(game.getCurrentTurnPlayerId());
        GameMessage turnMessage = new GameMessage(
                MessageType.PLAYER_TURN,
                JsonUtil.toJson(playerTurn)
        );

        broadcast(turnMessage);
    }

    private void handleGameOver(int winnerId) {
        GameOver gameOver = new GameOver(winnerId);
        GameMessage gameOverMessage = new GameMessage(
                MessageType.GAME_OVER,
                JsonUtil.toJson(gameOver)
        );

        broadcast(gameOverMessage);
        closeRoom();
    }

    private void handlePreGameAction(GameMessage message, ClientHandler sender) {
        switch (message.getType()) {
            case GAME_START:
                StartGame startGame = JsonUtil.fromJson(message.getPayloadAsString(), StartGame.class);
                // Проверяем, что игрок может начать игру
                if (players.size() == 2 && players.containsKey(startGame.clientId())) {
                    startGame();
                }
                break;
        }
    }

    private boolean validatePlayerAction(ClientHandler sender, int clientId) {
        return sender.getPlayerId() == clientId &&
                players.containsKey(clientId) &&
                clientId == currentTurnPlayerId;
    }

    private void updateGameState() {
        // Преобразуем состояние игры в DTO
        GameStateDto gameStateDto = game.toGameStateDto();

        GameMessage updateMessage = new GameMessage(
                MessageType.STATE_UPDATE,
                JsonUtil.toJson(gameStateDto)
        );

        broadcast(updateMessage);
    }

    private void switchTurn() {
        currentTurnPlayerId = (currentTurnPlayerId == 1) ? 2 : 1;

        // Уведомляем игроков о смене хода
        PlayerTurn playerTurn = new PlayerTurn(currentTurnPlayerId);
        GameMessage turnMessage = new GameMessage(
                MessageType.PLAYER_TURN,
                JsonUtil.toJson(playerTurn)
        );

        broadcast(turnMessage);
    }

    private boolean checkGameOver() {
        // Проверяем условия окончания игры
        // boolean gameOver = game.isGameOver(); // TODO: Реализовать в Game
        boolean gameOver = false;

        if (gameOver) {
            // int winnerId = game.getWinnerId(); // TODO: Реализовать в Game
            int winnerId = 1;

            GameOver gameOverDto = new GameOver(winnerId);
            GameMessage gameOverMessage = new GameMessage(
                    MessageType.GAME_OVER,
                    JsonUtil.toJson(gameOverDto)
            );

            broadcast(gameOverMessage);
            closeRoom();
            return true;
        }

        return false;
    }

    private void sendError(ClientHandler player, String errorMessage) {
        ErrorInfo error = new ErrorInfo(errorMessage);
        GameMessage errorMessageObj = new GameMessage(
                MessageType.ERROR,
                JsonUtil.toJson(error)
        );

        try {
            player.sendMessage(errorMessageObj);
        } catch (IOException e) {
            System.err.println("Failed to send error to player: " + e.getMessage());
        }
    }

    public void broadcast(GameMessage message) {
        for (ClientHandler player : players.values()) {
            try {
                player.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to send message to player: " + e.getMessage());
            }
        }
    }

    public void sendToPlayer(int playerId, GameMessage message) {
        ClientHandler player = players.get(playerId);
        if (player != null) {
            try {
                player.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to send message to player " + playerId + ": " + e.getMessage());
            }
        }
    }

    public int getOpponentId(int playerId) {
        return (playerId == 1) ? 2 : 1;
    }

    public void closeRoom() {
        // Закрываем соединения с игроками
        for (ClientHandler player : players.values()) {
            player.disconnect();
        }
        players.clear();
        gameStarted = false;

        System.out.println("Room closed: " + name);
    }

    // Getters
    public ClientHandler getPlayer(int playerId) {
        return players.get(playerId);
    }

    public String getName() {
        return name;
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    public boolean hasPlayer(int playerId) {
        return players.containsKey(playerId);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
}

