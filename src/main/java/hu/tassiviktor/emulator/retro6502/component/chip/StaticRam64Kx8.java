package hu.tassiviktor.emulator.retro6502.component.chip;

import hu.tassiviktor.emulator.retro6502.component.template.chip.memory.AbstractSRam;

public class StaticRam64Kx8 extends AbstractSRam {

    private static final int ADDRESS_LINES = 16;
    private static final int DATA_WIDTH = 8;

    public StaticRam64Kx8() {
        super(ADDRESS_LINES, DATA_WIDTH);
    }

}
