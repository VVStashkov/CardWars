// GameScene.java - дополненная версия с флюпом
package ru.itis.inf400.net.client;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ru.itis.inf400.net.dto.records.CardPlaceInGameType;
import ru.itis.inf400.net.dto.records.GameStateDto;
import ru.itis.inf400.net.dto.records.fullUpdate.CardDto;
import ru.itis.inf400.net.dto.records.fullUpdate.PlayerDto;
import ru.itis.inf400.net.dto.records.fullUpdate.WarriorDto;

import java.util.List;

public class GameScene extends BorderPane {

    private final GameClientApp app;
    private final ClientProcessor clientProcessor;
    private GameStateDto currentGameState;
    private int currentPlayerId;

    // UI элементы
    private Label roomLabel;
    private Label playerLabel;
    private Label turnLabel;
    private Label player1HpLabel;
    private Label player2HpLabel;
    private Label player1ActionPointsLabel;
    private Label player2ActionPointsLabel;

    // Поля игроков
    private VBox[] opponentFields = new VBox[4];
    private VBox[] playerFields = new VBox[4];

    // Колоды и сбросы
    private Label opponentDeckLabel;
    private Label opponentDropLabel;
    private Label playerDeckLabel;
    private Label playerDropLabel;

    // Карты на руке
    private HBox handCardsContainer;

    // Кнопки действий
    private Button takeCardButton;
    private Button attackButton;
    private Button quitButton;
    private Button putCardButton;
    private Button flupButton;

    // Выбранные элементы
    private CardDto selectedHandCard = null;
    private Integer selectedFieldIndex = null;

    public GameScene(GameClientApp app, ClientProcessor clientProcessor) {
        this.app = app;
        this.clientProcessor = clientProcessor;
        this.currentPlayerId = clientProcessor.getPlayerId();

        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(5));

        // Верхняя панель - информация о игре
        createTopPanel();

        // Центральная панель - игровое поле
        createCenterPanel();

        // Нижняя панель - карты на руке и кнопки
        createBottomPanel();
    }

    private void createTopPanel() {
        HBox topPanel = new HBox(15);
        topPanel.setPadding(new Insets(8));
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        roomLabel = new Label("Комната: " + clientProcessor.getRoomName());
        roomLabel.setFont(new Font(12));

        playerLabel = new Label("Игрок ID: " + currentPlayerId);
        playerLabel.setFont(new Font(12));

        turnLabel = new Label("Ход игрока: -");
        turnLabel.setFont(new Font(12));

        player1HpLabel = new Label("HP 1: 25");
        player2HpLabel = new Label("HP 2: 25");
        player1ActionPointsLabel = new Label("AP 1: 2");
        player2ActionPointsLabel = new Label("AP 2: 2");

        player1HpLabel.setFont(new Font(11));
        player2HpLabel.setFont(new Font(11));
        player1ActionPointsLabel.setFont(new Font(11));
        player2ActionPointsLabel.setFont(new Font(11));

        HBox player1Info = new HBox(8);
        player1Info.getChildren().addAll(player1HpLabel, player1ActionPointsLabel);

        HBox player2Info = new HBox(8);
        player2Info.getChildren().addAll(player2HpLabel, player2ActionPointsLabel);

        topPanel.getChildren().addAll(
                roomLabel, new Separator(), playerLabel, new Separator(), turnLabel,
                new Separator(), player1Info, new Separator(), player2Info
        );

        setTop(topPanel);
    }

    private void createCenterPanel() {
        GridPane centerGrid = new GridPane();
        centerGrid.setPadding(new Insets(10));
        centerGrid.setHgap(15);
        centerGrid.setVgap(15);
        centerGrid.setAlignment(Pos.CENTER);

        // Противник (верхняя часть)
        Label opponentLabel = new Label("Противник");
        opponentLabel.setFont(new Font(14));
        opponentLabel.setStyle("-fx-font-weight: bold;");
        GridPane.setConstraints(opponentLabel, 0, 0);

        // Колода и сброс противника
        VBox opponentDeckBox = new VBox(3);
        opponentDeckBox.setAlignment(Pos.CENTER);
        opponentDeckLabel = new Label("Колода: 0");
        opponentDropLabel = new Label("Сброс: 0");
        opponentDeckLabel.setFont(new Font(10));
        opponentDropLabel.setFont(new Font(10));
        opponentDeckBox.getChildren().addAll(opponentDeckLabel, opponentDropLabel);
        GridPane.setConstraints(opponentDeckBox, 1, 0, 2, 1);

        // Поля противника
        HBox opponentFieldsContainer = new HBox(8);
        opponentFieldsContainer.setAlignment(Pos.CENTER);
        for (int i = 0; i < 4; i++) {
            opponentFields[i] = createFieldBox(i, true);
            opponentFieldsContainer.getChildren().add(opponentFields[i]);
        }
        GridPane.setConstraints(opponentFieldsContainer, 0, 1, 3, 1);

        // Разделитель (уменьшенная высота)
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        GridPane.setConstraints(separator, 0, 2, 3, 1);

        // Игрок (нижняя часть)
        Label playerLabel = new Label("Вы");
        playerLabel.setFont(new Font(14));
        playerLabel.setStyle("-fx-font-weight: bold;");
        GridPane.setConstraints(playerLabel, 0, 3);

        // Колода и сброс игрока
        VBox playerDeckBox = new VBox(3);
        playerDeckBox.setAlignment(Pos.CENTER);
        playerDeckLabel = new Label("Колода: 0");
        playerDropLabel = new Label("Сброс: 0");
        playerDeckLabel.setFont(new Font(10));
        playerDropLabel.setFont(new Font(10));
        playerDeckBox.getChildren().addAll(playerDeckLabel, playerDropLabel);
        GridPane.setConstraints(playerDeckBox, 1, 3, 2, 1);

        // Поля игрока
        HBox playerFieldsContainer = new HBox(8);
        playerFieldsContainer.setAlignment(Pos.CENTER);
        for (int i = 0; i < 4; i++) {
            playerFields[i] = createFieldBox(i, false);
            playerFieldsContainer.getChildren().add(playerFields[i]);
        }
        GridPane.setConstraints(playerFieldsContainer, 0, 4, 3, 1);

        centerGrid.getChildren().addAll(
                opponentLabel, opponentDeckBox, opponentFieldsContainer,
                separator, playerLabel, playerDeckBox, playerFieldsContainer
        );

        setCenter(centerGrid);
    }

    private VBox createFieldBox(int fieldIndex, boolean isOpponent) {
        VBox fieldBox = new VBox(3);
        fieldBox.setAlignment(Pos.CENTER);
        fieldBox.setPadding(new Insets(5));
        fieldBox.setStyle("-fx-border-color: #888; -fx-border-width: 2; -fx-border-radius: 5;");
        fieldBox.setPrefSize(130, 90); // Увеличили ширину, уменьшили высоту

        // Номер поля - для противника показываем зеркальные позиции
        int displayIndex = isOpponent ? (3 - fieldIndex) : fieldIndex;
        Label fieldNumberLabel = new Label("Поле " + (displayIndex + 1));
        fieldNumberLabel.setFont(new Font(11));

        // Поле для карты
        StackPane cardSlot = new StackPane();
        cardSlot.setPrefSize(110, 70); // Увеличили ширину, уменьшили высоту
        cardSlot.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #aaa; -fx-border-width: 1;");

        // Текст по умолчанию
        Text emptyText = new Text(isOpponent ? "Соперник" : "Пусто");
        emptyText.setFont(new Font(9));
        emptyText.setFill(Color.GRAY);
        cardSlot.getChildren().add(emptyText);

        // Контейнер для информации о карте
        VBox cardInfo = new VBox(1);
        cardInfo.setVisible(false);

        fieldBox.getChildren().addAll(fieldNumberLabel, cardSlot, cardInfo);

        // Обработка клика на поле (только для своих полей)
        if (!isOpponent) {
            final int fieldPos = fieldIndex; // Сохраняем реальный индекс
            fieldBox.setOnMouseClicked(e -> {
                handleFieldClick(fieldPos);
            });

            // Подсветка при наведении
            fieldBox.setOnMouseEntered(e -> {
                if (!isOpponent) {
                    fieldBox.setStyle("-fx-border-color: #007bff; -fx-border-width: 3; -fx-border-radius: 5;");
                }
            });

            fieldBox.setOnMouseExited(e -> {
                if (!isOpponent) {
                    fieldBox.setStyle("-fx-border-color: #888; -fx-border-width: 2; -fx-border-radius: 5;");
                }
            });
        }

        return fieldBox;
    }

    private void createBottomPanel() {
        VBox bottomPanel = new VBox(8);
        bottomPanel.setPadding(new Insets(8));
        bottomPanel.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ccc; -fx-border-width: 1 0 0 0;");

        // Заголовок карт на руке
        Label handLabel = new Label("Карты на руке:");
        handLabel.setFont(new Font(13));
        handLabel.setStyle("-fx-font-weight: bold;");

        // Контейнер для карт на руке
        handCardsContainer = new HBox(8);
        handCardsContainer.setAlignment(Pos.CENTER);
        handCardsContainer.setPrefHeight(100); // Уменьшили высоту

        // Прокрутка для карт на руке
        ScrollPane handScrollPane = new ScrollPane(handCardsContainer);
        handScrollPane.setFitToHeight(true);
        handScrollPane.setPrefHeight(110);
        handScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd;");

        // Панель кнопок
        HBox buttonPanel = new HBox(8);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(5, 0, 0, 0));

        takeCardButton = new Button("Взять карту");
        takeCardButton.setPrefWidth(100);
        takeCardButton.setOnAction(e -> clientProcessor.sendGetCard());

        putCardButton = new Button("Положить карту");
        putCardButton.setPrefWidth(110);
        putCardButton.setDisable(true);
        putCardButton.setOnAction(e -> {
            if (selectedHandCard != null && selectedFieldIndex != null) {
                clientProcessor.sendPutCard(getHandCardIndex(selectedHandCard), selectedFieldIndex);
                selectedHandCard = null;
                selectedFieldIndex = null;
                updateHandCards();
                updateFields();
            }
        });

        flupButton = new Button("Флюпнуть");
        flupButton.setPrefWidth(100);
        flupButton.setDisable(true);
        flupButton.setOnAction(e -> handleFlupButtonClick());

        attackButton = new Button("Атаковать");
        attackButton.setPrefWidth(100);
        attackButton.setOnAction(e -> clientProcessor.sendAttack());

        quitButton = new Button("Выйти");
        quitButton.setPrefWidth(80);
        quitButton.setOnAction(e -> {
            clientProcessor.sendQuitGame();
            app.showLobbyScene();
        });

        buttonPanel.getChildren().addAll(
                takeCardButton, putCardButton, flupButton, attackButton, quitButton
        );

        bottomPanel.getChildren().addAll(handLabel, handScrollPane, buttonPanel);
        setBottom(bottomPanel);
    }

    private void handleFieldClick(int fieldIndex) {
        selectedFieldIndex = fieldIndex;
        updateFields();
        updateButtons();

        if (selectedHandCard != null) {
            putCardButton.setDisable(false);
        }
    }

    private void handleFlupButtonClick() {
        if (selectedFieldIndex == null) {
            app.showAlert("Сначала выберите поле с картой для флюпа!");
            return;
        }

        // Определяем, что находится на выбранном поле
        boolean isWarrior = false;
        PlayerDto currentPlayer = getCurrentPlayer();

        if (currentPlayer != null) {
            // Проверяем, есть ли воин на выбранном поле
            for (WarriorDto warrior : currentPlayer.warriors()) {
                if (warrior.placeCode() == CardPlaceInGameType.FIELD.getCode() &&
                        warrior.position() == selectedFieldIndex) {
                    isWarrior = true;
                    break;
                }
            }
        }

        // Если на поле нет карты
        if (!isWarrior) {
            // Проверяем, есть ли здание на выбранном поле
            boolean hasBuilding = false;
            if (currentPlayer != null) {
                for (CardDto card : currentPlayer.otherCard()) {
                    if (card.placeCode() == CardPlaceInGameType.FIELD.getCode() &&
                            card.position() == selectedFieldIndex) {
                        hasBuilding = true;
                        break;
                    }
                }
            }

            if (!hasBuilding) {
                app.showAlert("На выбранном поле нет карты для флюпа!");
                return;
            }
        }

        // Отправляем запрос на флюп
        clientProcessor.sendFlupAction(selectedFieldIndex, isWarrior);

        // Сбрасываем выбранное поле после отправки
        selectedFieldIndex = null;
        updateFields();
        updateButtons();
    }

    private PlayerDto getCurrentPlayer() {
        if (currentGameState == null) return null;

        for (PlayerDto player : currentGameState.players()) {
            if (player.id() == currentPlayerId) {
                return player;
            }
        }
        return null;
    }

    public void updateGameState(GameStateDto gameState) {
        this.currentGameState = gameState;

        Platform.runLater(() -> {
            updatePlayerInfo();
            updateFields();
            updateDecksAndDrops();
            updateHandCards();
            updateButtons();
        });
    }

    private void updatePlayerInfo() {
        if (currentGameState == null) return;

        // Обновляем информацию о игроках
        for (PlayerDto player : currentGameState.players()) {
            if (player.id() == 1) {
                player1HpLabel.setText("HP 1: " + player.hp());
                player1ActionPointsLabel.setText("AP 1: " + player.actionPoint());
            } else if (player.id() == 2) {
                player2HpLabel.setText("HP 2: " + player.hp());
                player2ActionPointsLabel.setText("AP 2: " + player.actionPoint());
            }

            // Обновляем информацию о текущем игроке
            if (player.id() == currentPlayerId) {
                playerLabel.setText("Игрок " + currentPlayerId + " (HP: " + player.hp() + ", AP: " + player.actionPoint() + ")");
            }
        }

        // Обновляем информацию о ходе
        turnLabel.setText("Ход игрока: " + currentGameState.currentPlayerId());

        // Подсвечиваем текущего игрока
        if (currentGameState.currentPlayerId() == currentPlayerId) {
            turnLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
        } else {
            turnLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
        }
    }

    private void updateFields() {
        if (currentGameState == null) return;

        // Определяем, кто является противником, а кто игроком
        PlayerDto currentPlayer = null;
        PlayerDto opponentPlayer = null;

        for (PlayerDto player : currentGameState.players()) {
            if (player.id() == currentPlayerId) {
                currentPlayer = player;
            } else {
                opponentPlayer = player;
            }
        }

        if (currentPlayer == null || opponentPlayer == null) return;

        // Обновляем поля противника
        updatePlayerFields(opponentPlayer, opponentFields, true);

        // Обновляем поля игрока
        updatePlayerFields(currentPlayer, playerFields, false);
    }

    private void updatePlayerFields(PlayerDto player, VBox[] fields, boolean isOpponent) {
        // Сначала очищаем все поля
        for (int i = 0; i < 4; i++) {
            VBox fieldBox = fields[i];
            StackPane cardSlot = (StackPane) fieldBox.getChildren().get(1);
            VBox cardInfo = (VBox) fieldBox.getChildren().get(2);

            // Очищаем слот карты
            cardSlot.getChildren().clear();
            cardInfo.getChildren().clear();
            cardInfo.setVisible(false);

            // Добавляем текст по умолчанию
            Text emptyText = new Text("Пусто");
            emptyText.setFont(new Font(9));
            emptyText.setFill(Color.GRAY);
            cardSlot.getChildren().add(emptyText);

            // Сбрасываем стиль
            if (!isOpponent) {
                if (selectedFieldIndex != null && selectedFieldIndex == i) {
                    fieldBox.setStyle("-fx-border-color: #ff9900; -fx-border-width: 3; -fx-border-radius: 5;");
                } else {
                    fieldBox.setStyle("-fx-border-color: #888; -fx-border-width: 2; -fx-border-radius: 5;");
                }
            }
        }

        // Заполняем воинами на полях
        for (WarriorDto warrior : player.warriors()) {
            if (warrior.placeCode() == CardPlaceInGameType.FIELD.getCode() && warrior.position() >= 0 && warrior.position() <= 3) {
                // ВАЖНО: Для противника инвертируем позицию (0->3, 1->2, 2->1, 3->0)
                int fieldIndex = isOpponent ? (3 - warrior.position()) : warrior.position();

                if (fieldIndex >= 0 && fieldIndex < 4) {
                    VBox fieldBox = fields[fieldIndex];
                    StackPane cardSlot = (StackPane) fieldBox.getChildren().get(1);
                    VBox cardInfo = (VBox) fieldBox.getChildren().get(2);

                    // Очищаем слот
                    cardSlot.getChildren().clear();

                    // Создаем визуальное представление карты
                    VBox cardVisual = createCardVisual(warrior.name(), warrior.hp(), warrior.attack(), warrior.flupped());
                    cardSlot.getChildren().add(cardVisual);

                    // Добавляем информацию о карте
                    Label nameLabel = new Label(abbreviate(warrior.name(), 12));
                    nameLabel.setFont(new Font(9));
                    Label hpLabel = new Label("HP: " + warrior.hp());
                    hpLabel.setFont(new Font(8));
                    Label attackLabel = new Label("АТК: " + warrior.attack());
                    attackLabel.setFont(new Font(8));

                    if (warrior.flupped()) {
                        nameLabel.setStyle("-fx-text-fill: #ff9900; -fx-font-weight: bold;");
                    }

                    cardInfo.getChildren().addAll(nameLabel, hpLabel, attackLabel);
                    cardInfo.setVisible(true);
                }
            }
        }

        // Заполняем другие карты на полях (здания)
        for (CardDto card : player.otherCard()) {
            if (card.placeCode() == CardPlaceInGameType.FIELD.getCode() && card.position() >= 0 && card.position() <= 3) {
                // ВАЖНО: Для противника инвертируем позицию (0->3, 1->2, 2->1, 3->0)
                int fieldIndex = isOpponent ? (3 - card.position()) : card.position();

                if (fieldIndex >= 0 && fieldIndex < 4) {
                    VBox fieldBox = fields[fieldIndex];
                    StackPane cardSlot = (StackPane) fieldBox.getChildren().get(1);
                    VBox cardInfo = (VBox) fieldBox.getChildren().get(2);

                    // Если на поле уже есть воин, добавляем здание как дополнительную карту
                    if (cardSlot.getChildren().size() > 0 && cardSlot.getChildren().get(0) instanceof VBox) {
                        // Уже есть воин, добавляем иконку здания
                        Label buildingIcon = new Label("🏠");
                        buildingIcon.setFont(new Font(16));
                        cardSlot.getChildren().add(buildingIcon);

                        // Обновляем информацию
                        Label buildingLabel = new Label("Здание: " + abbreviate(card.name(), 10));
                        buildingLabel.setFont(new Font(8));
                        cardInfo.getChildren().add(buildingLabel);
                    } else {
                        // Нет воина, отображаем здание как основную карту
                        cardSlot.getChildren().clear();

                        VBox cardVisual = createCardVisual(card.name(), 0, 0, card.flupped());
                        cardSlot.getChildren().add(cardVisual);

                        // Обновляем информацию
                        Label nameLabel = new Label(abbreviate(card.name(), 12));
                        nameLabel.setFont(new Font(9));
                        Label typeLabel = new Label("Здание");
                        typeLabel.setFont(new Font(8));

                        if (card.flupped()) {
                            nameLabel.setStyle("-fx-text-fill: #ff9900; -fx-font-weight: bold;");
                        }

                        cardInfo.getChildren().addAll(nameLabel, typeLabel);
                        cardInfo.setVisible(true);
                    }
                }
            }
        }
    }

    private VBox createCardVisual(String name, int hp, int attack, boolean flupped) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(4));
        card.setPrefSize(100, 65); // Увеличили ширину, уменьшили высоту

        // Фон карты
        Rectangle background = new Rectangle(100, 65);
        if (flupped) {
            background.setFill(Color.GOLD);
        } else if (hp > 0) {
            background.setFill(Color.LIGHTBLUE);
        } else {
            background.setFill(Color.LIGHTGRAY);
        }
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1);

        // Текст на карте
        VBox textContainer = new VBox(2);
        textContainer.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(abbreviate(name, 12)); // Увеличили лимит до 12 символов
        nameLabel.setFont(new Font(9));
        nameLabel.setStyle("-fx-font-weight: bold;");

        if (hp > 0) {
            Label statsLabel = new Label("HP:" + hp + " ATK:" + attack);
            statsLabel.setFont(new Font(8));
            textContainer.getChildren().addAll(nameLabel, statsLabel);
        } else {
            textContainer.getChildren().add(nameLabel);
        }

        StackPane stack = new StackPane();
        stack.getChildren().addAll(background, textContainer);
        card.getChildren().add(stack);

        return card;
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private void updateDecksAndDrops() {
        if (currentGameState == null) return;

        for (PlayerDto player : currentGameState.players()) {
            if (player.id() == currentPlayerId) {
                playerDeckLabel.setText("Колода: " + player.amountOfRemainingCardInDeck());
                playerDropLabel.setText("Сброс: " + player.amountOfCardInDrop());
            } else {
                opponentDeckLabel.setText("Колода: " + player.amountOfRemainingCardInDeck());
                opponentDropLabel.setText("Сброс: " + player.amountOfCardInDrop());
            }
        }
    }

    private void updateHandCards() {
        handCardsContainer.getChildren().clear();

        if (currentGameState == null) return;

        // Находим текущего игрока
        PlayerDto currentPlayer = null;
        for (PlayerDto player : currentGameState.players()) {
            if (player.id() == currentPlayerId) {
                currentPlayer = player;
                break;
            }
        }

        if (currentPlayer == null) return;

        // Отображаем воинов на руке
        for (WarriorDto warrior : currentPlayer.warriors()) {
            if (warrior.placeCode() == CardPlaceInGameType.HAND.getCode()) {
                VBox cardVisual = createCardVisual(warrior.name(), warrior.hp(), warrior.attack(), warrior.flupped());

                // Создаем контейнер для карты с обработчиком клика
                StackPane cardContainer = new StackPane(cardVisual);
                cardContainer.setOnMouseClicked(e -> handleHandCardClick(warrior));

                // Подсветка при наведении
                cardContainer.setOnMouseEntered(e -> {
                    cardContainer.setStyle("-fx-border-color: #007bff; -fx-border-width: 2;");
                });

                cardContainer.setOnMouseExited(e -> {
                    cardContainer.setStyle("-fx-border-color: transparent;");
                });

                // Подсветка выбранной карты
                if (selectedHandCard != null && selectedHandCard.name().equals(warrior.name())) {
                    cardContainer.setStyle("-fx-border-color: #ff9900; -fx-border-width: 3;");
                }

                handCardsContainer.getChildren().add(cardContainer);
            }
        }

        // Отображаем другие карты на руке
        for (CardDto card : currentPlayer.otherCard()) {
            if (card.placeCode() == CardPlaceInGameType.HAND.getCode()) {
                VBox cardVisual = createCardVisual(card.name(), 0, 0, card.flupped());

                StackPane cardContainer = new StackPane(cardVisual);
                cardContainer.setOnMouseClicked(e -> handleHandCardClick(card));

                cardContainer.setOnMouseEntered(e -> {
                    cardContainer.setStyle("-fx-border-color: #007bff; -fx-border-width: 2;");
                });

                cardContainer.setOnMouseExited(e -> {
                    cardContainer.setStyle("-fx-border-color: transparent;");
                });

                if (selectedHandCard != null && selectedHandCard.name().equals(card.name())) {
                    cardContainer.setStyle("-fx-border-color: #ff9900; -fx-border-width: 3;");
                }

                handCardsContainer.getChildren().add(cardContainer);
            }
        }
    }

    private void handleHandCardClick(CardDto card) {
        selectedHandCard = card;

        // Сбрасываем выделение всех карт
        for (javafx.scene.Node node : handCardsContainer.getChildren()) {
            if (node instanceof StackPane) {
                node.setStyle("-fx-border-color: transparent;");
            }
        }

        // Подсвечиваем выбранную карту
        int cardIndex = getHandCardIndex(card);
        if (cardIndex >= 0 && cardIndex < handCardsContainer.getChildren().size()) {
            handCardsContainer.getChildren().get(cardIndex).setStyle("-fx-border-color: #ff9900; -fx-border-width: 3;");
        }

        // Активируем кнопку "Положить карту", если выбрано поле
        if (selectedFieldIndex != null) {
            putCardButton.setDisable(false);
        }

        // Обновляем состояние кнопок
        updateButtons();

        // Показываем детальную информацию о карте
        showCardDetails(card);
    }

    private void handleHandCardClick(WarriorDto warrior) {
        // Преобразуем WarriorDto в CardDto для обработки
        CardDto card = new CardDto(
                warrior.name(),
                warrior.description(),
                warrior.cost(),
                warrior.fieldType(),
                warrior.placeCode(),
                warrior.position(),
                warrior.flupped()
        );
        handleHandCardClick(card);
    }

    private int getHandCardIndex(CardDto card) {
        return card.position();
    }

    private void showCardDetails(CardDto card) {
        // Создаем диалог с детальной информацией о карте
        Alert cardDetails = new Alert(Alert.AlertType.INFORMATION);
        cardDetails.setTitle("Информация о карте");
        cardDetails.setHeaderText(card.name());

        String content = "Описание: " + card.description() + "\n" +
                "Стоимость: " + card.cost() + " AP\n" +
                "Тип поля: " + card.fieldType();

        cardDetails.setContentText(content);
        cardDetails.showAndWait();
    }

    private void updateButtons() {
        if (currentGameState == null) return;

        // Проверяем, чей сейчас ход
        boolean isMyTurn = (currentGameState.currentPlayerId() == currentPlayerId);

        // Активируем/деактивируем кнопки в зависимости от хода
        takeCardButton.setDisable(!isMyTurn);
        attackButton.setDisable(!isMyTurn);
        putCardButton.setDisable(!isMyTurn || selectedHandCard == null || selectedFieldIndex == null);

        // Кнопка "Флюпнуть" активна, если наш ход и выбрано поле
        flupButton.setDisable(!isMyTurn || selectedFieldIndex == null);
    }
}