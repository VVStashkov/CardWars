package ru.itis.inf400.Game;

public class Field {
    private FieldTypes type;
    private Warrior warrior;
    private Building building;
    private boolean inverted;

    public FieldTypes getType() {
        return type;
    }

    public void setType(FieldTypes type) {
        this.type = type;
    }

    public Warrior getWarrior() {
        return warrior;
    }

    public void setWarrior(Warrior warrior) {
        this.warrior = warrior;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
