package ru.itis.inf400.Game.Warriors;

import ru.itis.inf400.Game.Flupable;
import ru.itis.inf400.Game.Player;
import ru.itis.inf400.Game.Warrior;

public class Healer extends Warrior implements Flupable {

    public Healer() {
        hp = 4;
        attack = 0;
        cost = 1;
    }

    @Override
    public void flup(Player p1, Player p2) {

    }

    @Override
    public void unflup(Player p1, Player p2) {

    }

    @Override
    public int getFlupPosition() {
        return 0;
    }
}
