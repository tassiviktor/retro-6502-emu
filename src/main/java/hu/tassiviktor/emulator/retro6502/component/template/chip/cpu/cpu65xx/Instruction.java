package hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx;

import hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.helper.Mnemonics;

public class Instruction {
    public final byte code;
    public final Mnemonics mnemonic;
    public final Addressing.Mode addressing;
    public final int length;
    public final int cycles;
    public final boolean xCycle;
    public final Runnable executeMethod;


    public Instruction(byte code, Mnemonics mnemonic, Addressing.Mode addressingMode, int length, int cycles, boolean xCycle, Runnable executeMethod) {
        this.code = code;
        this.mnemonic = mnemonic;
        this.addressing = addressingMode;
        this.length = length;
        this.cycles = cycles;
        this.xCycle = xCycle;
        this.executeMethod = executeMethod;
    }

}
