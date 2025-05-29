package ru.itis.inf400.Game;

import java.util.List;

public class Player {

    private int actionPoint = 2;
    private int hp = 25;
    private List<Card> deck;
    private List<Card> hand;
    private List<Field> fields;
    private List<Card> drop;

    public Player() {
    }

    public void attack(Player enemyPlayer) {
        for (int i = 0; i < fields.size(); ++i) {
            Warrior warrior = fields.get(i).getWarrior();
            if (warrior != null && !warrior.flupped) {
                Warrior enemyWarrior = enemyPlayer.getFields().get(i).getWarrior();
                if (enemyWarrior == null) {
                    warrior.attack(enemyPlayer);
                } else {
                    warrior.attack(enemyWarrior);
                    checkDeathOfWarrior(warrior, enemyPlayer);
                    enemyPlayer.checkDeathOfWarrior(enemyWarrior, this);

                }
            }
        }
        actionPoint = 2;
    }

    // enemyWarrior передаётся чтобы можно было сделать unflup
    public void checkDeathOfWarrior(Warrior warrior, Player enemyPlayer) {
        if (warrior.hp <= 0) {
            drop.add(warrior);
            fields.get(warrior.position).setWarrior(null);
            if (warrior instanceof Flupable && warrior.flupped) {
                Flupable FlupableWarrior = (Flupable) warrior;
                FlupableWarrior.unflup(this, enemyPlayer);
            }
        }
    }

    public void flupWarrior(int position, Player enemyPlayer) {
        Warrior warrior = fields.get(position).getWarrior();
        if (warrior instanceof Flupable ) {
            Flupable FlupableWarrior = (Flupable)  warrior;
            FlupableWarrior.flup(this, enemyPlayer);
        } else {
            System.out.println("Карту нельзя флюпнуть");
        }
    }
    public void flupBuilding(int position, Player enemyPlayer) {
        Building building = fields.get(position).getBuilding();
        if (building instanceof Flupable ) {
            Flupable FlupableBuilding = (Flupable)  building;
            FlupableBuilding.flup(this, enemyPlayer);
        } else {
            System.out.println("Карту нельзя флюпнуть");
        }
    }

    public void printActionChoose() {
        if (actionPoint >= 0) {
            System.out.println("1 - начать атаку \n" +
                    "2 - флюпнуть карту \n" +
                    "3 - использовать карту \n" +
                    "4 - посмотреть карты на руке \n" +
                    "5 - посмотреть карты на полях");
            //
        }
        if (actionPoint > 0) {
            System.out.println("6 - взять карту \n");

        }
    }
    public void printFlupableWarrior() {
        int i = 1;
        for(Field field : fields) {
            if (field.getWarrior() instanceof Flupable) {
                System.out.println(i + ") " + field.getWarrior().description);
            }
            ++i;
        }

    }
    public void printFlupableBuildings() {
        int i = 1;
        for(Field field : fields) {
            if (field.getBuilding() instanceof Flupable) {
                System.out.println(i + "поле) " + field.getBuilding().description);
            }
            ++i;
        }

    }

    public int getActionPoint() {
        return actionPoint;
    }

    public int getHp() {
        return hp;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<Card> getHand() {
        return hand;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<Card> getDrop() {
        return drop;
    }

    public void setActionPoint(int actionPoint) {
        this.actionPoint = actionPoint;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setDrop(List<Card> drop) {
        this.drop = drop;
    }
}
