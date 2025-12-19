// MessageType.java - добавим новое сообщение
package ru.itis.inf400.net.dto;

public enum MessageType {
    // Управление комнатами
    CREATE_ROOM(0),
    JOIN_ROOM(1),
    GET_ROOMS(2),
    ROOM_CREATED(3),
    ROOM_JOINED(4),
    ROOM_NOT_FOUND(5),
    ROOM_FULL(6),
    ROOMS_LIST(7),
    PLAYER_JOINED(8),  // Новое: уведомление о присоединении игрока

    // Игровые события
    GAME_START(9),
    DROP_CARD(10),
    PUT_CARD(11),
    USE_FLUP(12),
    ATTACK(13),
    GET_CARD(14),
    GAME_OVER(15),
    QUIT_GAME(16),
    PLAYER_TURN(17),

    // Обновления состояния
    STATE_UPDATE(18),
    ERROR(19),
    DISCONNECT(20);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}