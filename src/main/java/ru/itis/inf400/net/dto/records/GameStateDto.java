package ru.itis.inf400.net.dto.records;

import ru.itis.inf400.net.dto.records.fullUpdate.PlayerDto;

import java.util.List;

public record GameStateDto(
        List<PlayerDto> players,
        int currentPlayerId,
        boolean gameOver,
        Integer winnerId
) {}