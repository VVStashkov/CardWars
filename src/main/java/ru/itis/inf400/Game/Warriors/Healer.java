package ru.itis.inf400.Game.Warriors;

import ru.itis.inf400.Game.Flupable;
import ru.itis.inf400.Game.Player;
import ru.itis.inf400.Game.Warrior;

public class Healer extends Warrior implements Flupable {

    public Healer() {
        hp = 4;
        attack = 0;
        cost = 1;
        description = "целитель лечит соседних воинов на 2 хп, hp = 4, attack = 0, cost = 1";
    }

    @Override
    public void flup(Player player, Player enemyPlayer) {
        if (position < player.getFields().size() - 1) {
            Warrior rightW =  player.getFields().get(position - 1).getWarrior();
            if (rightW != null) {
                rightW.setHp(rightW.getHp() + 2);
            }
        }
        if (position > 0) {
            Warrior leftW = player.getFields().get(position + 1).getWarrior();
            if (leftW != null) {
                leftW.setHp(leftW.getHp() + 2);
            }
        }
    }

    @Override
    public void unFlup(Player player, Player enemyPlayer) {
        //отмены флюпа нет
    }

}
