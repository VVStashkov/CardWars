package ru.itis.inf400.Game.Warriors;

import ru.itis.inf400.Game.Flupable;
import ru.itis.inf400.Game.Player;
import ru.itis.inf400.Game.Warrior;

public class Buffer extends Warrior implements Flupable {

    public Buffer() {
        hp = 4;
        attack = 1;
        cost = 1;
        description = "баффер увеличивает атаку соседних воинов на 1, hp = 4, attack = 1, cost = 1";
    }
    @Override
    public void flup(Player player, Player enemyPlayer) {
        if (position < player.getFields().size() - 1) {
            Warrior rightW =  player.getFields().get(position - 1).getWarrior();
            if (rightW != null) {
                rightW.setAttack(rightW.getAttack() + 1);
            }
        }
        if (position > 0) {
            Warrior leftW =  player.getFields().get(position + 1).getWarrior();
            if (leftW != null) {
                leftW.setAttack(leftW.getAttack() + 1);
            }
        }
    }

    @Override
    public void unFlup(Player player, Player enemyPlayer) {
        if (position < player.getFields().size() - 1) {
            Warrior rightW =  player.getFields().get(position - 1).getWarrior();
            if (rightW != null) {
                rightW.setAttack(rightW.getAttack() - 1);
            }
        }
        if (position > 0) {
            Warrior leftW =  player.getFields().get(position + 1).getWarrior();
            if (leftW != null) {
                leftW.setAttack(leftW.getAttack() - 1);
            }
        }
    }
}
