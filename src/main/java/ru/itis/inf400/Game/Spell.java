package ru.itis.inf400.Game;

public abstract class Spell extends Card {
    abstract public void undo(Player player, Player enemyPlayer);
}
