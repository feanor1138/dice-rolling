package dice;

import java.util.Random;

public class Die {
    private int sides;
    private int value;
    public Die(int sides) {
        this.sides = sides;
    }

    public int roll() {
        Random r = new Random();
        this.value = r.nextInt(sides) + 1;
        return this.value;
    }
}
