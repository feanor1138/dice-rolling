package dice;

import java.util.Random;

public class Die {
    private int sides;
    private int value;
    private String name;

    public Die(String name, int sides) {
        this.name = name;
        this.sides = sides;
    }

    public String getName() {
        return name;
    }

    public int roll() {
        Random r = new Random();
        this.value = r.nextInt(sides) + 1;
        return this.value;
    }
}
