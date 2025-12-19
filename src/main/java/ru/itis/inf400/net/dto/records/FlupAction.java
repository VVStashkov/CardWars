package ru.itis.inf400.net.dto.records;

public record FlupAction(
        int clientId,
        String roomName,
        int fieldIndex,
        boolean isWarrior
) {}