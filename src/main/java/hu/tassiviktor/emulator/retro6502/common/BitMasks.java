package hu.tassiviktor.emulator.retro6502.common;

public class BitMasks {

    private static final long[] bitMask = new long[31];

    static {

        bitMask[0] = 0;

        for (int i = 1; i < bitMask.length-1; i++) {
            bitMask[i] = Power2.of(i) - 1;
        }

    }
    public static long ofBits(int bits){
        return bitMask[bits];
    }
}
