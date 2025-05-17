package ru.itis.inf400.Game;

public interface Flupable {
    //p1 игрок, которому принадлежит карта, p2 - его соперник
    //position означает номер поля(твоего или соперника), на которое будет применена способность,
    //в случае если это значение не используется флюпом, указывать -1
    int flupPosition = -1;
    void flup(Player p1, Player p2, int flupPosition);
    void unflup(Player p1, Player p2, int flupPosition);
    int getFlupPosition();
}
