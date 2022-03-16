package hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx;

public class Addressing {

    public enum Mode {
        IMP,  // Implied -?
        ACC,  // Accumulator
        IMM,  // Immediate
        ABS,  // Absolute
        IND,  // Indirect
        ABX, // Absolute,X
        ABY, // Absolute,Y
        INX, // Indexed indirect
        INY, // Indexed indirect
        ZRP,   // Zero Page
        ZPX,  // Zero Page,X
        ZPY,  // Zero Page,Y
        REL,  // Relative
    }

}
