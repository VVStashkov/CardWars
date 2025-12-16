package ru.itis.inf400.net.dto.records.fullUpdate;

import java.util.List;

public record PlayerDto(
        int id,
        int actionPoint,
        int hp,
        List<String> fieldTypes,
        List<WarriorDto> warriors,
        List<CardDto> otherCard,
        int amountOfRemainingCardInDeck,
        int amountOfCardInDrop
) {
}
