package ru.itis.inf400.net.dto.records;

public record PutCard(
        int clientId,
        String roomName,
        int positionInHand,
        int requiredPositionOnField
) {
}
