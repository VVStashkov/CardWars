package ru.itis.inf400.Game;

import java.io.Serializable;

public class Field implements Serializable {
    private FieldTypes type;
    private Warrior warrior;
    private Building building;

    public Field(FieldTypes type) {
        this.type = type;
    }

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

}
