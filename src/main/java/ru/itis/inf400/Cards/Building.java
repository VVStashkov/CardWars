package ru.itis.inf400.Cards;

public abstract class Building extends Card{
    protected int position;
    @Override
    protected void put(Player player,  Player enemyPlayer) {
        Field field = player.getFields().get(position);
        if (field.getBuilding() != null) {
            player.getDrop().add(field.getBuilding());
        }
        field.setBuilding(this);
        this.position = position;

    }
    //изменяет атрибуты warrior в зависимости от свойств строения
    abstract public void doConstantAction(Warrior warrior);
}
