package ru.itis.inf400;

public abstract class Card {
    private String name;
    private String description;
    private int cost;

    public abstract void use(int position);
}
