// Game.java - адаптированная версия для сетевой игры
package ru.itis.inf400.Cards;

import ru.itis.inf400.Cards.Warriors.Buffer;
import ru.itis.inf400.Cards.Warriors.Healer;
import ru.itis.inf400.Cards.Warriors.Swordsman;
import ru.itis.inf400.net.dto.records.GameStateDto;
import ru.itis.inf400.net.dto.records.fullUpdate.CardDto;
import ru.itis.inf400.net.dto.records.fullUpdate.PlayerDto;
import ru.itis.inf400.net.dto.records.fullUpdate.WarriorDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Game implements Serializable {
    private Player serverPlayer;
    private Player clientPlayer;
    private int currentTurnPlayerId; // 1 - serverPlayer, 2 - clientPlayer
    private boolean gameOver;
    private Integer winnerId;

    // Конструкторы и базовые методы остаются

    public Game(Player serverPlayer, Player clientPlayer) {
        this.serverPlayer = serverPlayer;
        this.clientPlayer = clientPlayer;
        this.currentTurnPlayerId = 1; // Первым ходит serverPlayer
        this.gameOver = false;
        this.winnerId = null;
    }

    /**
     * Игрок берет карту из своей колоды
     */
    public boolean takeCard(int playerId) {
        Player player = getPlayerById(playerId);
        if (player == null || playerId != currentTurnPlayerId) {
            return false;
        }

        // Проверяем, есть ли карты в колоде
        if (player.getDeck().isEmpty()) {
            return false;
        }

        // Берем карту (логика из старого метода takeCard)

        Card card = player.getDeck().removeLast();
        player.getHand().add(card);
        player.setActionPoint(player.getActionPoint() - 1); // Стоимость действия
        return true;
    }

    /**
     * Игрок кладет карту с руки на поле
     */
    public boolean putCard(int playerId, int handIndex, int fieldIndex) {
        Player player = getPlayerById(playerId);
        if (playerId == 2) {
            fieldIndex = 3 - fieldIndex;
        }
        Player enemy = getOpponentById(playerId);

        if (player == null || playerId != currentTurnPlayerId) {
            return false;
        }

        // Проверяем индексы
        if (handIndex < 0 || handIndex >= player.getHand().size() ||
                fieldIndex < 0 || fieldIndex >= player.getFields().size()) {
            return false;
        }

        Card card = player.getHand().get(handIndex);

        // Проверяем достаточно ли action points
        if (player.getActionPoint() < card.cost) {
            return false;
        }

        // Проверяем тип поля (если карта требует определенного типа)
        Field field = player.getFields().get(fieldIndex);
        // TODO: Проверить совместимость типа карты и типа поля

        // Логика размещения карты
        if (card instanceof Warrior) {
            Warrior warrior = (Warrior) card;

            // Проверяем, есть ли уже воин на поле
            if (field.getWarrior() != null) {
                // Удаляем существующего воина в сброс
                player.getDrop().add(field.getWarrior());
            }

            // Размещаем воина
            field.setWarrior(warrior);
            warrior.position = fieldIndex;

            // Проверяем эффекты зданий
            if (field.getBuilding() != null) {
                field.getBuilding().doConstantAction(warrior);
            }

        } else if (card instanceof Building) {
            Building building = (Building) card;

            // Проверяем, есть ли уже здание на поле
            if (field.getBuilding() != null) {
                // Удаляем существующее здание в сброс
                player.getDrop().add(field.getBuilding());
            }

            // Размещаем здание
            field.setBuilding(building);
            building.position = fieldIndex;

        } else if (card instanceof Spell) {
            // TODO: Реализовать логику заклинаний
            // Пока просто удаляем карту из руки
            player.getDrop().add(card);
            card.put(player, enemy);
        }

        // Уменьшаем action points
        player.setActionPoint(player.getActionPoint() - card.cost);

        // Удаляем карту из руки
        player.getHand().remove(handIndex);

        return true;
    }

    /**
     * Использование способности карты (флюп)
     */
    public boolean useFlup(int playerId, int fieldIndex, boolean isWarrior) {
        Player player = getPlayerById(playerId);
        Player enemy = getOpponentById(playerId);

        if (player == null || playerId != currentTurnPlayerId) {
            return false;
        }

        Field field = player.getFields().get(fieldIndex);

        if (isWarrior) {
            Warrior warrior = field.getWarrior();
            if (warrior != null && warrior instanceof Flupable && !warrior.flupped) {
                Flupable flupable = (Flupable) warrior;
                flupable.flup(player, enemy);
                warrior.flupped = true;
                return true;
            }
        } else {
            Building building = field.getBuilding();
            if (building != null && building instanceof Flupable && !building.flupped) {
                Flupable flupable = (Flupable) building;
                flupable.flup(player, enemy);
                building.flupped = true;
                return true;
            }
        }

        return false;
    }

    /**
     * Атака игрока
     */
    public boolean attack(int playerId) {
        Player attacker = getPlayerById(playerId);
        Player defender = getOpponentById(playerId);

        if (attacker == null || defender == null || playerId != currentTurnPlayerId) {
            return false;
        }

        // Логика атаки из старого метода attack
        for (int i = 0; i < attacker.getFields().size(); i++) {
            Warrior warrior = attacker.getFields().get(i).getWarrior();
            if (warrior != null && !warrior.flupped) {
                Warrior enemyWarrior = defender.getFields().get(i).getWarrior();
                if (enemyWarrior == null) {
                    // Атака прямо по игроку
                    warrior.attack(defender);
                } else {
                    // Бой между воинами
                    warrior.attack(enemyWarrior);

                    // Проверяем смерть воинов
                    checkDeathOfWarrior(warrior, attacker, defender);
                    checkDeathOfWarrior(enemyWarrior, defender, attacker);

                    // Отмена заклинаний
                    attacker.undoSpells(defender);
                }
            }
        }

        // Сбрасываем флюпнутость всех карт
        resetFluppedCards(attacker);

        // берем карту

        if (!attacker.getDeck().isEmpty()) {
            Card card = attacker.getDeck().removeLast();
            attacker.getHand().add(card);
        }

        // Проверяем, не умер ли защищающийся игрок
        if (defender.getHp() <= 0) {
            gameOver = true;
            winnerId = playerId;
            return true;
        }

        // Передаем ход другому игроку
        switchTurn();

        return true;
    }

    /**
     * Выход игрока из игры
     */
    public boolean quitGame(int playerId) {
        if (!gameOver) {
            gameOver = true;
            winnerId = playerId == 1 ? 2 : 1;
            return true;
        }
        return false;
    }

    /**
     * Переключение хода
     */
    private void switchTurn() {
        currentTurnPlayerId = (currentTurnPlayerId == 1) ? 2 : 1;

        // Сбрасываем action points для нового хода
        Player currentPlayer = getPlayerById(currentTurnPlayerId);
        if (currentPlayer != null) {
            currentPlayer.setActionPoint(2);
        }
    }

    /**
     * Проверка смерти воина
     */
    private void checkDeathOfWarrior(Warrior warrior, Player owner, Player enemy) {
        if (warrior.hp <= 0) {
            owner.getDrop().add(warrior);
            owner.getFields().get(warrior.position).setWarrior(null);

            // Отменяем флюп, если был
            if (warrior instanceof Flupable && warrior.flupped) {
                ((Flupable) warrior).unFlup(owner, enemy);
            }
        }
    }

    /**
     * Сброс флюпнутости всех карт
     */
    private void resetFluppedCards(Player player) {
        for (Field field : player.getFields()) {
            if (field.getWarrior() != null) {
                field.getWarrior().flupped = false;
            }
            if (field.getBuilding() != null) {
                field.getBuilding().flupped = false;
            }
        }
    }

    /**
     * Преобразование состояния игры в DTO для отправки клиентам
     */
    public GameStateDto toGameStateDto() {
        List<PlayerDto> players = new ArrayList<>();

        // Преобразуем serverPlayer (playerId = 1)
        players.add(serverPlayer.convertPlayerToDto(1));

        // Преобразуем clientPlayer (playerId = 2)
        players.add(clientPlayer.convertPlayerToDto(2));

        return new GameStateDto(players, currentTurnPlayerId, gameOver, winnerId);
    }

    // Вспомогательные методы

    private Player getPlayerById(int playerId) {
        return playerId == 1 ? serverPlayer : playerId == 2 ? clientPlayer : null;
    }

    private Player getOpponentById(int playerId) {
        return playerId == 1 ? clientPlayer : playerId == 2 ? serverPlayer : null;
    }

    // Геттеры и сеттеры
    public Player getServerPlayer() {
        return serverPlayer;
    }

    public Player getClientPlayer() {
        return clientPlayer;
    }

    public int getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setServerPlayer(Player serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    public void setClientPlayer(Player clientPlayer) {
        this.clientPlayer = clientPlayer;
    }

    public void setCurrentTurnPlayerId(int currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }
}