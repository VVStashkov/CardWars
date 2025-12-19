package ru.itis.inf400.net.dto.records;

import ru.itis.inf400.net.dto.records.fullUpdate.PlayerDto;
import java.util.List;

public record GameState(
        List<PlayerDto> players,
        int currentPlayerId
) {}