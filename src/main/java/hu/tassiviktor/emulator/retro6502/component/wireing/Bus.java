package hu.tassiviktor.emulator.retro6502.component.wireing;

import hu.tassiviktor.emulator.retro6502.common.BitMasks;

public class Bus {

    private final int width;

    private long lines = 0; //Bus wire states are stores as number for faster emulation

    public Bus(int width) {
        this.width = width;
    }

    public long readAsNumber() {
        return lines & BitMasks.ofBits(width); // Truncating to bus width. Probably not necessary
    }

    public void writeAsNumber(long asNumber) {
        lines = asNumber & BitMasks.ofBits(width);
    }

    public void setListener(Runnable operate) {

    }
}
