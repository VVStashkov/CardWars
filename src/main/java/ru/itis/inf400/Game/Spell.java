package ru.itis.inf400.Game;

public abstract class Spell extends Card {
    //метод put у каждого дочернего класса свой
    abstract public void undo(Player player, Player enemyPlayer);
}
