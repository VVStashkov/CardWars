package ru.itis.inf400.Game;

public abstract class Building extends Card{
    protected int position;
    protected boolean flupped;
    protected void put(Player player, int position) {
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
