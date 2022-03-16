package hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx;

import hu.tassiviktor.emulator.retro6502.common.Signal;
import hu.tassiviktor.emulator.retro6502.component.template.Component;
import hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.helper.InstructionTableBuilder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.Addressing.Mode.*;

/**
 * Execution layer
 */
public abstract class Cpu65xxCore implements Component {
    private final Logger logger;

    private static final int STACK_BASE = 0x0100;

    private static final int RESET_VECTOR = 0xFFFC;
    private static final int NMI_VECTOR = 0xFFFA;
    private static final int IRQ_VECTOR = 0xFFFE;

    protected volatile boolean isRunning = true; //CPU is running, not freezed..
    protected volatile boolean pause = false;

    protected final AtomicLong cycles = new AtomicLong(0);
    protected final AtomicInteger credit = new AtomicInteger(0);

    final Signal cont = new Signal();

    protected int regX; // Register X
    protected int regY; // Register Y
    protected int regA; // Accumulator

    protected int pc; // ProgramCounter - unsigned word
    protected int sp; // Stack Pointer  - unsigned word

    protected int regAddress; //Internal address register

    protected Flags flags = new Flags();

    protected boolean nmiRequested = false;
    protected boolean irqRequested = false;

    protected final Instruction[] iTable = new Instruction[255];

    Instruction currentInstruction;

    public Cpu65xxCore() {
        logger = Logger.getLogger(this.getClass().getSimpleName());
        InstructionTableBuilder.buildTable(iTable, this);
    }

    protected abstract byte readByteFromAddress(int pc);

    protected abstract int readWordFromAddress(int pc);

    protected abstract void writeByteToAddress(int pc, int data);

    @Override
    public void install() {

    }

    @Override
    public void powerUp() {
        reset();
    }

    private void reset() {
        regA = 0;
        regX = 0;
        regY = 0;
        flags.fromByte(0, false); //TODO what is the proper state of flags at reset?
        nmiRequested = false;
        irqRequested = false;
        sp = 0xFF - 2; //On reset 6502/6510 wastes 2 bytes with fake SP access.
        pc = readWordFromAddress(RESET_VECTOR);
        logger.info("Address from RESET_VECTOR: " + pc);
    }

    protected void cycle() throws InterruptedException {
        waitForCredit();

        if (nmiRequested || irqRequested) {
            //On interrupt, CPU is internally forced to execute a BRK
            currentInstruction = iTable[0x00];
        } else { //Normal cycle
            int opcode = readByteFromAddress(pc);
            currentInstruction = iTable[opcode];
            incrementPc(1);
        }

        currentInstruction.executeMethod.run();
        burnCycles(currentInstruction.cycles);
    }

    protected void burnCycles(int eCycles) {
        credit.addAndGet(-eCycles);
        cycles.addAndGet(eCycles);
    }

    private void waitForCredit() throws InterruptedException {
        if (credit.get() < 1) {
            cont.waitForSignal();
        }
        cont.clearSignal();
    }

    private boolean pageBoundaryCrossed(int address1, int address2) {
        return (address1 & 0xFF) != (address2 & 0xFF);
    }

    protected byte pop() {
        return readByteFromAddress((++this.sp & 0xFF) | STACK_BASE);
    }

    protected void push(byte data) {
        writeByteToAddress((this.sp-- & 0xFF) | STACK_BASE, data);
    }

    protected long setPc(int addr) {
        pc = addr & 0xFFFF;
        return pc;
    }

    public int incrementPc(int delta) {
        pc = (pc + delta) & 0xFFFF;
        return pc;
    }

    public int incrementPcAndGetOld(int delta) {
        int oldPc = pc;
        pc = (pc + delta) & 0xFFFF;
        return oldPc;
    }

    public void unsupportedOperation() {
        throw new IllegalStateException("Unsupported operation detected");
    }

    protected int getOperand() {
        switch (currentInstruction.addressing) {
            case IMP:
                //TODO: is IMP always means accu?
                throw new IllegalStateException("Imp. addressing must not get any operand.");

            case ACC:
                return regA;

            case IMM:
                return readByteFromAddress(incrementPcAndGetOld(1));

            case ABS:
                return readByteFromAddress(readWordFromAddress(incrementPcAndGetOld(2)));

            case IND:
                int pointer = readWordFromAddress(incrementPcAndGetOld(2));
                int indAddress = readWordFromAddress(pointer);
                return readByteFromAddress(indAddress);

            case ABX:
            case ABY:
                int delta = currentInstruction.addressing == ABX ? regX : regY;
                int absAddress = readWordFromAddress(incrementPcAndGetOld(2));
                int addressWithDelta = absAddress + delta;

                //TODO check xCycle is required or xCycle always effects with abs x/y
                if (pageBoundaryCrossed(absAddress, addressWithDelta) && currentInstruction.xCycle) {
                    burnCycles(1);
                }
                return readByteFromAddress(addressWithDelta);

            case INX:
                int tempIx = readByteFromAddress(incrementPcAndGetOld(1));
                byte lo = readByteFromAddress((tempIx + regX) & 0x00FF);
                byte hi = readByteFromAddress((tempIx + regX + 1) & 0x00FF);
                return readByteFromAddress(hi * 256 + lo);

            case INY:
                int tempIy = readByteFromAddress(incrementPcAndGetOld(1));
                int indYAddr = readByteFromAddress((tempIy) & 0x00FF) + 256 * readByteFromAddress((tempIy + 1) & 0x00FF);
                int indYAddrFinal = indYAddr + regY;
                if (pageBoundaryCrossed(indYAddr, indYAddrFinal)) {
                    burnCycles(1);
                }
                return readByteFromAddress(indYAddrFinal);

            case ZRP: // Read address as next operand, and read data from zero page address
                return readByteFromAddress(readByteFromAddress(incrementPcAndGetOld(1)) & 0xFF);

            case ZPX:
                return readByteFromAddress((readByteFromAddress(incrementPcAndGetOld(1)) + regX) & 0xFF);

            case ZPY:
                return readByteFromAddress((readByteFromAddress(incrementPcAndGetOld(1)) + regY) & 0xFF);

            case REL:
                byte offset = readByteFromAddress(incrementPcAndGetOld(1));
                if (pageBoundaryCrossed(pc, pc + offset)) {
                    burnCycles(1);
                }
                return readByteFromAddress(pc + offset);

            default:
                throw new IllegalStateException("Unimplemented addressing mode.");
        }
    }

    public void writeBackByte(int data){
        writeByteToAddress(regAddress, data);
    }

    /**
     * Add data to Accumulator with carry
     */
    public void adc() {
        int operand = getOperand();
        int temp;
        if (flags.decimal) {
            temp = 10 * (regA & 0xf0) + (regA & 0x0f) + (10 * (operand & 0xf0) + (operand & 0x0f));
            temp = ((temp / 10) << 4) + temp % 10;
        } else {
            temp = operand + regA + (flags.carry ? 1 : 0);
            flags.overflow = (((regA ^ operand) & 0x80) == 0) && (((regA ^ temp) & 0x80) != 0);
        }
        flags.carry = temp > 0xFF;
        regA = (temp & 0xFF);

        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    /**
     * Logical AND with Accumulator
     */
    public void and() {
        regA &= getOperand();

        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    /**
     * Shift Left One Bit (Memory)
     */
    public void asl() {
        int temp = getOperand() ;
        flags.carry = temp >= 0x80;
        flags.zero = temp == 0;
        flags.negative = temp >= 0x80;

        if (currentInstruction.addressing == ACC) {
            regA = (byte) ((temp << 1) & 0xFF);
        } else {
            writeBackByte(temp << 1);
        }
    }

    /**
     * Branch on condition
     */
    public void branchOn(){
        boolean condition;

        switch (currentInstruction.mnemonic){
            case BCC:
                condition = !flags.carry;
                break;
            case BCS:
                condition = flags.carry;
                break;
            case BEQ:
                condition = flags.zero;
                break;
            case BMI:
                condition = flags.negative;
                break;
            case BNE:
                condition = !flags.zero;
                break;
            case BPL:
                condition = !flags.negative;
                break;
            case BVC:
                condition = !flags.overflow;
                break;
            case BVS:
                condition = flags.overflow;
                break;
            default:
                condition = false;
        }

        if(condition) {
            incrementPc((byte) getOperand());
        }
    }

    /**
     * Test Bits in Memory with Accumulator
     */
    public void bit(){
        int temp = getOperand();
        flags.overflow = (temp & 0x40) > 0;
        flags.zero = temp == 0;
        flags.negative = temp >= 0x80;
    }

    /**
     * Interrupt
     * BRK follows another byte on software interrupt.
     */
    public void brk() {
        if (!nmiRequested && !irqRequested) {
            incrementPc(1);
        }

        push((byte) ((pc & 0xFF00) >> 8));
        push((byte) (pc & 0x00FF));

        //True on software interrupt
        flags.unused = true;
        flags.brk = !(nmiRequested || irqRequested);
        push((byte) flags.getByte());

        flags.intDisabled = true;

        setPc(readWordFromAddress(nmiRequested ? NMI_VECTOR : IRQ_VECTOR));

        //Nmi served first!
        if (nmiRequested) {
            nmiRequested = false;
        } else if (irqRequested) {
            irqRequested = false;
        }
    }

    /**
     * Clear carry flag
     */
    public void clc(){
        flags.carry = false;
    }

    /**
     * Clear decimal flag
     */
    public void cld(){
        flags.decimal = false;
    }

    /**
     * Clear interrupt flag
     */
    public void cli(){
        flags.brk = false;
    }

    /**
     * Clear overflow flag
     */
    public void clv(){
        flags.overflow = false;
    }

    /**
     * Compare. CMP, CPX, CPY Memory with Accumulator
     */
    public void compare(){
        int data = getOperand();
        int compareWith;
        switch (currentInstruction.mnemonic){
            case CMP:
                compareWith = regA;
                break;
            case CPX:
                compareWith = regX;
                break;
            case CPY:
                compareWith = regY;
                break;
            default:
                throw new UnsupportedOperationException("An operation is not a valid branch operation. "+currentInstruction.mnemonic);
        }
        flags.carry = compareWith >= data;
        flags.zero = compareWith == data;
        flags.negative = ((compareWith - data) & 0xff) >= 0x80;
    }

    public void dec(){
        int data = ((getOperand() & 0xff) - 1) & 0xFF;
        writeBackByte(data);
        flags.zero = data == 0;
        flags.negative = data >= 0x80;
    }

    public void dex(){
        regX--;
        regX &= 0xFF;
        flags.zero = regX == 0;
        flags.negative = regX >= 0x80;
    }

    public void dey(){
        regY--;
        regY &= 0xFF;
        flags.zero = regY == 0;
        flags.negative = regY >= 0x80;
    }

    /**
     * Exclusive-OR Memory with Accumulator
     */
    public void eor(){
        regA ^= getOperand();
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    /**
     * Increment Memory by One
     */
    public final void inc() {
        int data = ((getOperand() & 0xFF) + 1) & 0xFF;
        writeBackByte(data);
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    /**
     * Increment X register by One
     */
    public final void inx() {
        regX++;
        regX &= 0xFF;
        flags.zero = regX == 0;
        flags.negative = regX >= 0x80;
    }

    /**
     * Increment Y register by One
     */
    public void iny(){
        regY++;
        regY &= 0xFF;
        flags.zero = regY == 0;
        flags.negative = regY >= 0x80;
    }

    public void jmp(){
        int address;
        if(currentInstruction.addressing.equals(ABS)){
            address = readWordFromAddress(pc);
        } else if(currentInstruction.addressing.equals(IND)){
            address = readWordFromAddress(readWordFromAddress(pc));
        } else {
            throw new UnsupportedOperationException("Invalid addressing mode.");
        }
        setPc(address);
    }

    public void jsr(){
        int address = readWordFromAddress(pc);
        push((byte) (((pc + 2) & 0xff00) >> 8));
        push((byte) ((pc + 2) & 0x00ff));
        setPc(address);
    }

    public void ld() {
        int data = getOperand();
        switch (currentInstruction.mnemonic){
            case LDA:
                regA = data;
                break;
            case LDX:
                regX = data;
                break;
            case LDY:
                regY = data;
                break;
        }
        flags.zero = data == 0;
        flags.negative = data >= 0x80;
    }

    public void lsr() {
        int data = getOperand();
        flags.carry = (data & 0x01) != 0;
        if(currentInstruction.addressing == ACC) {
            regA = data >> 1;
        } else {
            writeBackByte(data >> 1);
        }
        flags.zero = data == 0;
        flags.negative = false;
    }

    public void nop(){
        // ...
    }

    public void ora(){
        regA |= getOperand();
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    public void pha(){
        push((byte)regA);
    }

    public void php(){
        push((byte)(flags.getByte() | 0b00110000));
        //Some implementation clears break and undefined flag, but documentation said it's remain intact.
    }

    public void pla(){
        regA = pop() & 0xFF;
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    public void plp(){
        flags.fromByte(pop(),true);
    }

    public void rol(){
        int data = getOperand();
        data = (data << 1) | (flags.carry ? 1 : 0);
        flags.carry = (data & 0xFF00) > 0;
        data &= 0xFF;
        if(currentInstruction.addressing == ACC) {
            regA = data;
        } else {
            writeBackByte(data);
        }
        flags.zero = data == 0;
        flags.negative = data >= 0x80;
    }

    public void ror(){
        int data = getOperand();
        boolean oldCarry = flags.carry;
        flags.carry = (data & 0x01) != 0;
        data = (data >> 1) | (oldCarry ? 0x80 :0);
        if(currentInstruction.addressing == ACC) {
            regA = data;
        } else {
            writeBackByte(data);
        }
        flags.zero = data == 0;
        flags.negative = data >= 0x80;
    }

    /**
     * Return from interrupt
     */
    public void rti() {
        plp();
        int lo = readByteFromAddress(pop());
        int hi = readByteFromAddress(pop());

        setPc((hi << 8) + lo);
    }

    /**
     * Return from subroutine
     */
    public void rts() {

        int lo = readByteFromAddress(pop());
        int hi = readByteFromAddress(pop());

        //RTS increments PC
        setPc((hi << 8) + lo + 1);
    }

    public void sbc(){
        int tmp;
        int data = getOperand();

        if (flags.decimal) {
            tmp = 10 * (regA & 0xf0) + (regA & 0x0f) - (10 * (data & 0xf0) + (data & 0x0f));
            tmp = ((tmp / 10) << 4) + tmp % 10;
        } else {
            tmp = regA - data - (flags.carry ? 0 : 1);
        }

        flags.overflow = (((regA ^ tmp) & 0x80) != 0) && (((regA ^ data) & 0x80) != 0);
        flags.carry = tmp >= 0;
        regA = tmp & 0xff;
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    public void sec(){
        flags.carry = true;
    }

    public void sed(){
        flags.decimal = true;
    }

    public void sei(){
        flags.intDisabled = true;
    }

    public void sta(){
        writeByteToAddress(readWordFromAddress(incrementPcAndGetOld(2)), regA);
    }

    public void stx(){
        writeByteToAddress(readWordFromAddress(incrementPcAndGetOld(2)), regX);
    }

    public void sty(){
        writeByteToAddress(readWordFromAddress(incrementPcAndGetOld(2)), regY);
    }

    public void tax(){
        regX = regA;
        flags.zero = regX == 0;
        flags.negative = regX >= 0x80;
    }

    public void tay(){
        regY = regA;
        flags.zero = regY == 0;
        flags.negative = regY >= 0x80;
    }

    public void tsx(){
        regX = sp & 0xFF;
        flags.zero = regX == 0;
        flags.negative = regX >= 0x80;
    }

    public void txa(){
        regA = regX;
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }

    public void txs(){
        sp = regX;
    }

    public void tya(){
        regA = regY;
        flags.zero = regA == 0;
        flags.negative = regA >= 0x80;
    }
}
