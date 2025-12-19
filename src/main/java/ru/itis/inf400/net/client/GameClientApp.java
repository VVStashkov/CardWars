package ru.itis.inf400.net.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.itis.inf400.net.dto.records.GameState;
import ru.itis.inf400.net.dto.records.GameStateDto;
import ru.itis.inf400.net.dto.records.PlayerJoined;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class GameClientApp extends Application {

    private Stage primaryStage;
    private ClientProcessor clientProcessor;
    private Socket socket;

    private int playerId;
    private String currentRoom;
    private boolean isCreator;

    private String serverHost = "localhost";
    private int serverPort = 5555;

    // Для списка комнат
    private ObservableList<String> availableRooms = FXCollections.observableArrayList();

    private Button startButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Card Game Client");
        showConnectScene();
        primaryStage.show();
    }

    private void showConnectScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Подключение к серверу");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField hostField = new TextField(serverHost);
        hostField.setPromptText("Адрес сервера");

        TextField portField = new TextField(String.valueOf(serverPort));
        portField.setPromptText("Порт");

        Button connectButton = new Button("Подключиться");
        connectButton.setDefaultButton(true);

        Label statusLabel = new Label();

        connectButton.setOnAction(e -> {
            try {
                serverHost = hostField.getText();
                serverPort = Integer.parseInt(portField.getText());

                connectButton.setDisable(true);
                statusLabel.setText("Подключаемся...");

                // Подключаемся в отдельном потоке, чтобы не блокировать UI
                new Thread(() -> {
                    try {
                        socket = new Socket(serverHost, serverPort);
                        clientProcessor = new ClientProcessor(this, socket);

                        Platform.runLater(() -> {
                            statusLabel.setText("Успешно подключено!");
                            showLobbyScene();
                        });

                        // Запрашиваем список комнат
                        clientProcessor.sendGetRooms();

                    } catch (IOException ex) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Ошибка подключения: " + ex.getMessage());
                            connectButton.setDisable(false);
                        });
                    }
                }).start();

            } catch (NumberFormatException ex) {
                statusLabel.setText("Неверный порт!");
            }
        });

        root.getChildren().addAll(
                titleLabel, hostField, portField, connectButton, statusLabel
        );

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
    }

    void showLobbyScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Игровое лобби");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Панель создания комнаты
        VBox createPanel = new VBox(10);
        createPanel.setAlignment(Pos.CENTER);
        Label createLabel = new Label("Создать новую комнату:");
        Button createRoomButton = new Button("Создать комнату");

        createRoomButton.setOnAction(e -> {
            createRoomButton.setDisable(true);
            clientProcessor.sendCreateRoom();
        });

        createPanel.getChildren().addAll(createLabel, createRoomButton);

        // Панель списка комнат
        VBox roomsPanel = new VBox(10);
        roomsPanel.setAlignment(Pos.CENTER);
        Label roomsLabel = new Label("Доступные комнаты:");

        ListView<String> roomsListView = new ListView<>(availableRooms);
        roomsListView.setPrefHeight(150);

        Button refreshButton = new Button("Обновить список");
        Button joinButton = new Button("Присоединиться к выбранной");

        refreshButton.setOnAction(e -> {
            clientProcessor.sendGetRooms();
        });

        joinButton.setOnAction(e -> {
            String selectedRoom = roomsListView.getSelectionModel().getSelectedItem();
            if (selectedRoom == null || selectedRoom.isEmpty()) {
                showAlert("Выберите комнату для присоединения");
                return;
            }

            joinButton.setDisable(true);
            clientProcessor.sendJoinRoom(selectedRoom);
        });

        roomsPanel.getChildren().addAll(roomsLabel, roomsListView, refreshButton, joinButton);

        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> {
            try {
                if (clientProcessor != null) {
                    clientProcessor.disconnect();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                // Игнорируем
            }
            showConnectScene();
        });

        root.getChildren().addAll(
                titleLabel, createPanel, roomsPanel, backButton
        );

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
    }

    private void showWaitingScene(String roomName, boolean isCreator) {
        this.isCreator = isCreator;

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Комната: " + roomName);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label playerLabel = new Label("Вы - " + (isCreator ? "Создатель комнаты" : "Присоединившийся игрок"));
        playerLabel.setStyle("-fx-font-size: 14px;");

        Label statusLabel = new Label(
                isCreator ?
                        "Ожидание второго игрока..." :
                        "Ожидание начала игры..."
        );
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: blue;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        if (isCreator) {
            startButton = new Button("Начать игру");
            startButton.setDisable(true); // Изначально неактивна
            startButton.setOnAction(e -> {
                startButton.setDisable(true);
                clientProcessor.sendGameStart();
            });
        }

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> {
            clientProcessor.sendQuitGame();
            showLobbyScene();
        });

        if (isCreator) {
            buttonBox.getChildren().addAll(startButton, cancelButton);
        } else {
            buttonBox.getChildren().add(cancelButton);
        }

        root.getChildren().addAll(
                titleLabel, playerLabel, statusLabel, buttonBox
        );

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
    }

    public void activateStartButton() {
        Platform.runLater(() -> {
            if (startButton != null) {
                startButton.setDisable(false);
                startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                showAlert("Второй игрок присоединился! Можно начинать игру.");
            }
        });
    }

    private void showGameScene(GameStateDto gameState) {
        GameScene gameScene = new GameScene(this, clientProcessor);
        gameScene.updateGameState(gameState);

        Scene scene = new Scene(gameScene, 1200, 800);
        primaryStage.setScene(scene);
    }

    private void showGameOverScene(int winnerId) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Игра окончена!");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label resultLabel;
        if (winnerId == playerId) {
            resultLabel = new Label("Поздравляем! Вы победили!");
            resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: green;");
        } else {
            resultLabel = new Label("Вы проиграли. Победил игрок " + winnerId);
            resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
        }

        Button returnButton = new Button("Вернуться в лобби");
        returnButton.setOnAction(e -> {
            showLobbyScene();
        });

        root.getChildren().addAll(titleLabel, resultLabel, returnButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onRoomCreated(String roomName, int playerId) {
        Platform.runLater(() -> {
            this.playerId = playerId;
            this.currentRoom = roomName;
            showWaitingScene(roomName, true);
        });
    }

    public void onRoomJoined(String roomName, int playerId) {
        Platform.runLater(() -> {
            this.playerId = playerId;
            this.currentRoom = roomName;
            showWaitingScene(roomName, false);
        });
    }

    public void onRoomNotFound(String roomName) {
        Platform.runLater(() -> {
            showAlert("Комната не найдена: " + roomName);
            showLobbyScene();
        });
    }

    public void onRoomFull(String roomName) {
        Platform.runLater(() -> {
            showAlert("Комната переполнена: " + roomName);
            showLobbyScene();
        });
    }

    public void onRoomsList(List<String> rooms) {
        Platform.runLater(() -> {
            availableRooms.clear();
            availableRooms.addAll(rooms);
        });
    }

    public void onGameStateUpdate(GameStateDto gameState) {
        Platform.runLater(() -> {
            if (primaryStage.getScene() != null &&
                    primaryStage.getScene().getRoot() instanceof GameScene) {
                // Обновляем существующую игровую сцену
                GameScene gameScene = (GameScene) primaryStage.getScene().getRoot();
                gameScene.updateGameState(gameState);
            } else {
                // Создаем новую игровую сцену
                showGameScene(gameState);
            }
        });
    }

    public void onGameOver(int winnerId) {
        Platform.runLater(() -> {
            showGameOverScene(winnerId);
        });
    }

    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            showAlert("Ошибка: " + errorMessage);
        });
    }

    public void onPlayerTurn(int playerId) {
        Platform.runLater(() -> {
            System.out.println("Сейчас ход игрока: " + playerId);
        });
    }

    public void onPlayerJoined(PlayerJoined playerJoined) {
        Platform.runLater(() -> {
            System.out.println("Player " + playerJoined.playerId() + " joined the room");
            if (isCreator) {
                // Если мы создатель комнаты, активируем кнопку "Начать игру"
                activateStartButton();
            }
        });
    }

    @Override
    public void stop() {
        if (clientProcessor != null) {
            clientProcessor.disconnect();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}