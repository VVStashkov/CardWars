package ru.itis.inf400.net.dto.records.fullUpdate;

public record WarriorDto(
        String name,
        String description,
        int cost,
        String fieldType,
        int placeCode,
        int position,
        int hp,
        int attack,
        boolean flupped) {
}
//если  position = 0 - карта на руке, [1, 4] - на поле, 5 - в сбросе