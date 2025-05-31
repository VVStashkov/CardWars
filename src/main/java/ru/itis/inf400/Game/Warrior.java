package ru.itis.inf400.Game;

public abstract class Warrior extends Card{
    protected int hp;
    protected int attack;
    protected int position;
    protected boolean flupped;

    protected void put(Player player, int position) {
        Field field = player.getFields().get(position);
        if (field.getWarrior() != null) {
            player.getDrop().add(field.getWarrior());
        }
        field.setWarrior(this);
        this.position = position;
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


}
