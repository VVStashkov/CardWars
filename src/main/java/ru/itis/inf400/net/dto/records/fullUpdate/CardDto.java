package ru.itis.inf400.net.dto.records.fullUpdate;

public record CardDto(
        String name,
        String description,
        int cost,
        String fieldType,
        int placeCode,
        int position,
        boolean flupped) {
}
