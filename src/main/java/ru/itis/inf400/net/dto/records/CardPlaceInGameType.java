package ru.itis.inf400.net.dto.records;

public enum CardPlaceInGameType {

    HAND(0),
    FIELD(1),
    DROP(2);

    private final int code;

    CardPlaceInGameType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CardPlaceInGameType fromCode(int code) {
        CardPlaceInGameType[] values = CardPlaceInGameType.values();

        if (code >= 0 && code < values.length) {
            return values[code];
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

}
