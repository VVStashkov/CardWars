// MessageType.java - обновленная версия
package ru.itis.inf400.net.dto;

public enum MessageType {
    // Управление комнатами
    CREATE_ROOM(0),
    JOIN_ROOM(1),
    GET_ROOMS(2),      // Новый тип: запрос списка комнат
    ROOM_CREATED(3),
    ROOM_JOINED(4),
    ROOM_NOT_FOUND(5),
    ROOM_FULL(6),
    ROOMS_LIST(7),     // Новый тип: ответ со списком комнат

    // Игровые события
    GAME_START(8),
    DROP_CARD(9),
    PUT_CARD(10),
    USE_FLUP(11),
    ATTACK(12),
    GET_CARD(13),
    GAME_OVER(14),
    QUIT_GAME(15),
    PLAYER_TURN(16),

    // Обновления состояния
    STATE_UPDATE(17),
    ERROR(18),
    DISCONNECT(19);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static MessageType fromCode(int code) {
        MessageType[] values = MessageType.values();

        if (code >= 0 && code < values.length) {
            return values[code];
        }

        throw new IllegalArgumentException("Unknown code: " + code);
    }
}