package ru.itis.inf400.Game;

public abstract class Card {
    protected String name;
    protected String description;
    protected int cost;
    protected FieldTypes type;

    abstract protected void put(Player player, Player enemyPlayer);
    // в зависимости от реализации будем спрашивать позицию для применения карты
}
