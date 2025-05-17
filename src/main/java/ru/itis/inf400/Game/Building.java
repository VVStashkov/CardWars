package ru.itis.inf400.Game;

public abstract class Building extends Card implements Flupable {
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
}
