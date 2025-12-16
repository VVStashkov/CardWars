package ru.itis.inf400.net.dto;

public enum MessageType {
    // Управление комнатами
    CREATE_ROOM(0),
    JOIN_ROOM(1),
    ROOM_CREATED(2),
    ROOM_JOINED(3),
    ROOM_NOT_FOUND(4),
    ROOM_FULL(5),

    // Игровые события
    GAME_START(6),
    PLAYER_TURN(7),
    PLACE_CARD(8),
    USE_FLUP(9),
    ATTACK(10),
    END_TURN(11),
    GAME_OVER(12),

    // Обновления состояния
    STATE_UPDATE(13),
    ERROR(14),
    DISCONNECT(15);

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