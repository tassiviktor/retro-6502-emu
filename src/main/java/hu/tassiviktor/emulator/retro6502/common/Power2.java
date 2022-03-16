package hu.tassiviktor.emulator.retro6502.common;

/**
 * Avoid using math.pow
 */
public class Power2 {

    private static final int[] table;

    static {
        table = new int[32];
        for (int i = 0; i < table.length; i++) {
            table[i] = power(i);
        }
    }

    public static int of(int x){
        return table[x];
    }

    private static int power(int b)
    {
        int power = 1;

        for(int c = 0; c < b; c++)
            power *= 2;

        return power;
    }
}
