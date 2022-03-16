package hu.tassiviktor.emulator.retro6502.component.chip;

import hu.tassiviktor.emulator.retro6502.component.template.chip.memory.AbstractRom;

public class Rom8kx8 extends AbstractRom {
    private static final int ADDRESS_LINES = 13;
    private static final int DATA_WIDTH = 8;

    public Rom8kx8(String imageFile) {
        super(ADDRESS_LINES, DATA_WIDTH, imageFile);
    }
}
