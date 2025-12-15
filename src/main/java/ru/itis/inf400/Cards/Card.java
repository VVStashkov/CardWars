package ru.itis.inf400.Cards;

import java.io.Serializable;

public abstract class Card implements Serializable {
    protected String name;
    protected String description;
    protected int cost;
    protected FieldTypes type;

    abstract protected void put(Player player, Player enemyPlayer);
    // в зависимости от реализации будем спрашивать позицию для применения карты


    public String getDescription() {
        return description;
    }
}
