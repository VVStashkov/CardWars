package ru.itis.inf400.Cards;

import java.util.Scanner;

public abstract class Warrior extends Card{
    protected int hp;
    protected int attack;
    protected int position;
    protected boolean flupped;

    @Override
    public void put(Player player, Player enemyPlayer) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите номер поля, на которое поставите карту");
        position = sc.nextInt();
        sc.nextLine();
        Field field = player.getFields().get(position);
        if (field.getWarrior() != null) {
            player.getDrop().add(field.getWarrior());
        }
        field.setWarrior(this);
        Building building = field.getBuilding();
        if (building != null) {
            building.doConstantAction(this);
        }
    }

    public void attack(Player player) {
        player.setHp(player.getHp() - attack);
    }

    public void attack(Warrior warrior) {
        warrior.hp -= attack;
        hp -= warrior.attack;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFlupped() {
        return flupped;
    }

    public void setFlupped(boolean flupped) {
        this.flupped = flupped;
    }
}
