package ru.itis.inf400.Cards;

import ru.itis.inf400.Cards.Warriors.Buffer;
import ru.itis.inf400.Cards.Warriors.Healer;
import ru.itis.inf400.Cards.Warriors.Swordsman;
import ru.itis.inf400.net.dto.records.CardPlaceInGameType;
import ru.itis.inf400.net.dto.records.fullUpdate.CardDto;
import ru.itis.inf400.net.dto.records.fullUpdate.PlayerDto;
import ru.itis.inf400.net.dto.records.fullUpdate.WarriorDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Player implements Serializable {

    private int actionPoint = 2;
    private int hp = 25;
    private List<Card> deck = new LinkedList<>();
    private List<Card> hand = new ArrayList<>();
    private List<Field> fields;
    private List<Card> drop = new ArrayList<>();
    private List<Spell> usedSpells = new ArrayList<>();

    public Player() {
    }

    public static Player createPlayer(int playerId) {
        Player player = new Player();

        // Устанавливаем начальные значения
        player.setHp(25);
        player.setActionPoint(2);

        // Создаем поля для игрока
        List<Field> fields = createDefaultFields(playerId);
        player.setFields(fields);

        // Создаем колоду и перемешиваем
        List<Card> deck = createDefaultDeck();
        player.setDeck(deck);

        // Раздаем начальные карты (например, 5 карт)
        for (int i = 0; i < 5; i++) {
            if (!player.getDeck().isEmpty()) {
                Card card = player.getDeck().removeLast();
                player.getHand().add(card);
            }
        }

        // Инициализируем другие коллекции
        player.setHand(new ArrayList<>());
        player.setDrop(new ArrayList<>());

        return player;
    }

    /**
     * Создает поля для игрока
     * В зависимости от playerId можно создать разные типы полей
     */
    private static List<Field> createDefaultFields(int playerId) {
        List<Field> fields = new ArrayList<>(4);

        // Для первого игрока - одни типы полей, для второго - другие
        // Или одинаковые для обоих, в зависимости от логики игры
        if (playerId == 1) {
            // Игрок 1 (server player)
            fields.add(new Field(FieldTypes.BLUEFIELD));
            fields.add(new Field(FieldTypes.BLUEFIELD));
            fields.add(new Field(FieldTypes.BLUEFIELD));
            fields.add(new Field(FieldTypes.BLUEFIELD));
        } else {
            // Игрок 2 (client player)
            fields.add(new Field(FieldTypes.CORNFIELD));
            fields.add(new Field(FieldTypes.CORNFIELD));
            fields.add(new Field(FieldTypes.CORNFIELD));
            fields.add(new Field(FieldTypes.CORNFIELD));
        }

        return fields;
    }
    /**
     * Инициализация дефолтной колоды
     */
    public static List<Card> createDefaultDeck() {
        List<Card> deck = new LinkedList<>();
        Random random = new Random();
        int num = random.nextInt(5,7);
        for (int i = 0; i < num; i++) {
            deck.add(new Swordsman());
        }
        num = random.nextInt(5,7);
        for (int i = 0; i < num; i++) {
            deck.add(new Buffer());
        }
        num = random.nextInt(5,7);
        for (int i = 0; i < num; i++) {
            deck.add(new Healer());
        }
        Collections.shuffle(deck);
        return deck;
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
                    undoSpells(enemyPlayer);
                    //прописать отмену заклинаний

                }
            }
        }
        actionPoint = 3;
        takeCard();
    }

    // enemyWarrior передаётся чтобы можно было сделать unflup
    public void checkDeathOfWarrior(Warrior warrior, Player enemyPlayer) {
        if (warrior.hp <= 0) {
            drop.add(warrior);
            fields.get(warrior.position).setWarrior(null);
            if (warrior instanceof Flupable && warrior.flupped) {
                Flupable FlupableWarrior = (Flupable) warrior;
                FlupableWarrior.unFlup(this, enemyPlayer);
            }
        }
    }
    public void undoSpells(Player enemyPlayer) {
        for (Spell spell : usedSpells) {
            spell.undo(this, enemyPlayer);
        }
        usedSpells = new ArrayList<>();
    }
    public void put(int num, Player enemyPlayer) {
        Card card = hand.remove(num);
        if (card instanceof Spell) {
            usedSpells.add((Spell) card);
        }
        card.put(this,enemyPlayer);
        actionPoint -= card.cost;
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

    public void printHand() {
        int i = 1;
        for(Card card : hand) {
            System.out.println(i++ + ") " + card.description);
        }
    }
    public void takeCard(){
        hand.add(deck.getLast());
        deck.removeLast();
        actionPoint--;
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

    public PlayerDto convertPlayerToDto(int playerId) {
        List<String> fieldTypes = getFields().stream()
                .map(field -> field.getType().name())
                .collect(Collectors.toList());

        List<WarriorDto> warriors = new ArrayList<>();
        List<CardDto> otherCards = new ArrayList<>();

        // Обрабатываем карты на полях
        for (int i = 0; i < getFields().size(); i++) {
            Field field = getFields().get(i);

            // Воины на поле
            if (field.getWarrior() != null) {
                Warrior warrior = field.getWarrior();
                warriors.add(new WarriorDto(
                        warrior.name,
                        warrior.description,
                        warrior.cost,
                        warrior.type != null ? warrior.type.name() : "NONE",
                        CardPlaceInGameType.FIELD.getCode(),
                        i,
                        warrior.hp,
                        warrior.attack,
                        warrior.flupped
                ));
            }

            // Здания на поле
            if (field.getBuilding() != null) {
                Building building = field.getBuilding();
                otherCards.add(new CardDto(
                        building.name,
                        building.description,
                        building.cost,
                        building.type != null ? building.type.name() : "NONE",
                        CardPlaceInGameType.FIELD.getCode(),
                        i,
                        building.flupped
                ));
            }
        }

        // Карты в руке
        for (int i = 0; i < getHand().size(); i++) {
            Card card = getHand().get(i);
            if (card instanceof Warrior) {
                Warrior warrior = (Warrior) card;
                warriors.add(new WarriorDto(warrior.name,
                                warrior.description,
                                warrior.cost,
                                warrior.type != null ? warrior.type.name() : "NONE",
                                CardPlaceInGameType.FIELD.getCode(),
                                i,
                                warrior.hp,
                                warrior.attack,
                                warrior.flupped
                        ));
            } else {
                otherCards.add(new CardDto(card.name,
                        card.description,
                        card.cost,
                        card.type != null ? card.type.name() : "NONE",
                        CardPlaceInGameType.FIELD.getCode(),
                        i,
                        card.flupped
                ));
            }
        }

        // Карты в сбросе
        for (Card card : getDrop()) {
            otherCards.add(new CardDto(
                    card.name,
                    card.description,
                    card.cost,
                    card.type != null ? card.type.name() : "NONE",
                    CardPlaceInGameType.DROP.getCode(),
                    getDrop().indexOf(card),
                    false
            ));
        }

        return new PlayerDto(
                playerId,
                getActionPoint(),
                getHp(),
                fieldTypes,
                warriors,
                otherCards,
                getDeck().size(),
                getDrop().size()
        );
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
