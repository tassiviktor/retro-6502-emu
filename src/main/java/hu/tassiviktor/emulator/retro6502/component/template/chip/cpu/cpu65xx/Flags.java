package hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx;

public class Flags {

    public boolean
            negative,
            overflow,
            unused,
            brk,
            decimal,
            intDisabled,
            zero,
            carry
    ;

    public int getByte(){
        return (negative       ? 0b10000000 : 0)
                + (overflow    ? 0b01000000 : 0)
                + (unused      ? 0b00100000 : 0)
                + (brk         ? 0b00010000 : 0)
                + (decimal     ? 0b00001000 : 0)
                + (intDisabled ? 0b00000100 : 0)
                + (zero        ? 0b00000010 : 0)
                + (carry       ? 0b00000001 : 0)
                ;
    }

    public void fromByte(int data, boolean ignore) {
        negative    = (data & 0b10000000) != 0;
        overflow    = (data & 0b01000000) != 0;

        if(!ignore) {
            unused = (data & 0b00100000) != 0;
            brk    = (data & 0b00010000) != 0;
        }

        decimal     = (data & 0b00001000) != 0;
        intDisabled = (data & 0b00000100) != 0;
        zero        = (data & 0b00000010) != 0;
        carry       = (data & 0b00000001) != 0;
    }
}
