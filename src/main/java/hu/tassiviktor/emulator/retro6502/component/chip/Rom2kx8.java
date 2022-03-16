package hu.tassiviktor.emulator.retro6502.component.chip;

import hu.tassiviktor.emulator.retro6502.component.template.chip.memory.AbstractRom;

public class Rom2kx8 extends AbstractRom {
    private static final int ADDRESS_LINES = 11;
    private static final int DATA_WIDTH = 8;

    public Rom2kx8(String imageFile) {
        super(ADDRESS_LINES, DATA_WIDTH, imageFile);
    }
}
