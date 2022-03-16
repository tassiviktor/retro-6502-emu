package hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.helper;

import hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.Addressing;
import hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.Cpu65xxCore;
import hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.Instruction;


import static hu.tassiviktor.emulator.retro6502.component.template.chip.cpu.cpu65xx.helper.Mnemonics.*;

/*
    https://www.masswerk.at/6502/6502_instruction_set.html
 */
public class InstructionTableBuilder {

    private InstructionTableBuilder() {
        //Utility Class
    }

    /**
     *
     * See https://www.masswerk.at/6502/6502_instruction_set.html
     * @param iTable
     * @param cpu
     */
    public static void buildTable(Instruction[] iTable, Cpu65xxCore cpu) {
        initTable(iTable, cpu);

        // * marks extra cycles on page boundary crossing.
        // ** add 1 to cycles if branch occurs on same page
        //    add 2 to cycles if branch occurs to different page

        /*  Add Memory to Accumulator with Carry

            A + M + C -> A, C

            Affected flags:
            N	Z	C	I	D	V
            +	+	+	-	-	+

            addressing	    assembler	    opc	bytes	cycles
            --------------------------------------------------
            immediate	    ADC #oper	    69	2	    2
            zeropage	    ADC oper	    65	2	    3
            zeropage,X	    ADC oper,X	    75	2	    4
            absolute	    ADC oper	    6D	3	    4
            absolute,X	    ADC oper,X	    7D	3	    4*
            absolute,Y	    ADC oper,Y	    79	3	    4*
            (indirect,X)	ADC (oper,X)	61	2	    6
            (indirect),Y	ADC (oper),Y	71	2	    5*
         */
        iTable[0x69] = new Instruction((byte) 0x69, ADC, Addressing.Mode.IMM,2,2,false,cpu::adc);
        iTable[0x65] = new Instruction((byte) 0x65, ADC, Addressing.Mode.ZRP,2,3,false,cpu::adc);
        iTable[0x75] = new Instruction((byte) 0x75, ADC, Addressing.Mode.ZPX,2,4,false,cpu::adc);
        iTable[0x6D] = new Instruction((byte) 0x6D, ADC, Addressing.Mode.ABS,3,4,false,cpu::adc);
        iTable[0x7D] = new Instruction((byte) 0x7D, ADC, Addressing.Mode.ABX,3,4,true,cpu::adc);
        iTable[0x79] = new Instruction((byte) 0x79, ADC, Addressing.Mode.ABY,3,4,true,cpu::adc);
        iTable[0x61] = new Instruction((byte) 0x61, ADC, Addressing.Mode.INX,2,6,false,cpu::adc);
        iTable[0x71] = new Instruction((byte) 0x71, ADC, Addressing.Mode.INX,2,5,true,cpu::adc);

        /*  AND Memory with Accumulator
            A AND M -> A

            Affected flags
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	    assembler	    opc	bytes   cycles
            --------------------------------------------------
            immediate	    AND #oper	    29	2	    2
            zeropage	    AND oper	    25	2	    3
            zeropage,X	    AND oper,X	    35	2	    4
            absolute	    AND oper	    2D	3	    4
            absolute,X	    AND oper,X	    3D	3	    4*
            absolute,Y	    AND oper,Y	    39	3	    4*
            (indirect,X)	AND (oper,X)	21	2	    6
            (indirect),Y	AND (oper),Y	31	2	    5*
         */
        iTable[0x29] = new Instruction((byte) 0x29, AND, Addressing.Mode.IMM,2,2,false,cpu::and);
        iTable[0x25] = new Instruction((byte) 0x25, AND, Addressing.Mode.ZRP,2,3,false,cpu::and);
        iTable[0x35] = new Instruction((byte) 0x35, AND, Addressing.Mode.ZPX,2,4,false,cpu::and);
        iTable[0x2D] = new Instruction((byte) 0x2D, AND, Addressing.Mode.ABS,3,4,false,cpu::and);
        iTable[0x3D] = new Instruction((byte) 0x3D, AND, Addressing.Mode.ABX,3,4,true,cpu::and);
        iTable[0x39] = new Instruction((byte) 0x39, AND, Addressing.Mode.ABY,3,4,true,cpu::and);
        iTable[0x21] = new Instruction((byte) 0x21, AND, Addressing.Mode.INX,2,6,false,cpu::and);
        iTable[0x31] = new Instruction((byte) 0x31, AND, Addressing.Mode.INX,2,5,true,cpu::and);

        /*  ASL - Shift Left One Bit (Memory or Accumulator)
            C <- [76543210] <- 0
            Affected flags:

            N	Z	C	I	D	V
            +	+	+	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            accumulator	ASL A	    0A	1	    2
            zeropage	ASL oper	06	2	    5
            zeropage,X	ASL oper,X	16	2	    6
            absolute	ASL oper	0E	3	    6
            absolute,X	ASL oper,X	1E	3	    7
        */
        iTable[0x0A] = new Instruction((byte) 0x0A, ASL, Addressing.Mode.ACC,1,2,false,cpu::asl);
        iTable[0x06] = new Instruction((byte) 0x06, ASL, Addressing.Mode.ZRP,2,5,false,cpu::asl);
        iTable[0x16] = new Instruction((byte) 0x16, ASL, Addressing.Mode.ZPX,2,6,false,cpu::asl);
        iTable[0x0E] = new Instruction((byte) 0x0E, ASL, Addressing.Mode.ABS,3,6,false,cpu::asl);
        iTable[0x1E] = new Instruction((byte) 0x1E, ASL, Addressing.Mode.ABX,3,7,false,cpu::asl);

        /*  BCC - Branch on Carry Clear
            branch on C = 0
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BCC oper	90	2	    2**

        */
        iTable[0x90] = new Instruction((byte) 0x90, BCC, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /*  BCC - Branch on Carry Set
            branch on C = 1
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BCS oper	B0	2	    2**

        */
        iTable[0xB0] = new Instruction((byte) 0xB0, BCS, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /*  BCC - Branch on Zero
            branch on Z = 1
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BEQ oper	F0	2	    2**

        */
        iTable[0xF0] = new Instruction((byte) 0xF0, BCS, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /*  BIT - Test Bits in Memory with Accumulator
            bits 7 and 6 of operand are transferred to bit 7 and 6 of SR (N,V);
            the zero-flag is set to the result of operand AND accumulator.

            A AND M, M7 -> N, M6 -> V
            Affected flags:

            N	Z	C	I	D	V
            M7	+	-	-	-	M6

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            zeropage	BIT oper	24	2	    3
            absolute	BIT oper	2C	3	    4
         */
        iTable[0x24] = new Instruction((byte) 0x24, BIT, Addressing.Mode.IMM,2,3,false,cpu::bit);
        iTable[0x2C] = new Instruction((byte) 0x2C, BIT, Addressing.Mode.IMM,3,4,false,cpu::bit);

        /*  BMI - Branch on Result minus
            branch on N = 1
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BMI oper	30	2	    2**

        */
        iTable[0x30] = new Instruction((byte) 0x30, BMI, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /*  BNE - Branch on Result not Zero
            branch on Z = 0
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BNE oper	D0	2	    2**

        */
        iTable[0xD0] = new Instruction((byte) 0xD0, BNE, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /*  BPL - Branch on Result Plus
            branch on N = 0
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BPL oper	10	2	    2**

        */
        iTable[0x10] = new Instruction((byte) 0x10, BPL, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /* BRK - Force Break

            BRK initiates a software interrupt similar to a hardware interrupt (IRQ). The return address pushed
            to the stack is PC+2, providing an extra byte of spacing for a break mark (identifying a reason for the
            break.)
            The status register will be pushed to the stack with the break flag set to 1. However, when retrieved
            during RTI or by a PLP instruction, the break flag will be ignored. The interrupt disable flag is
            not set automatically.

            interrupt,
            push PC+2, push SR

            Affected flags

            N	Z	C	I	D	V
            -	-	-	1	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    BRK	        00	1	    7

        */
        iTable[0x00] = new Instruction((byte) 0x00, BRK, Addressing.Mode.IMP,2,7,false,cpu::brk);

        /*  BVC - Branch on Overflow Clear
            branch on V = 0
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BVC oper	50	2	    2**

        */
        iTable[0x50] = new Instruction((byte) 0x50, BVC, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /*  BVS - Branch on Overflow Set
            branch on V = 1
            Affected flags:

            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            relative	BVS oper	70	2	    2**

        */
        iTable[0x70] = new Instruction((byte) 0x70, BVS, Addressing.Mode.REL,2,2,false,cpu::branchOn);

        /* CLC - Clear Carry Flag
            0 -> C
            Affected flags:

            N	Z	C	I	D	V
            -	-	0	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    CLC	        18	1	    2
        */
        iTable[0x18] = new Instruction((byte) 0x18, CLC, Addressing.Mode.IMP,1,2,false,cpu::clc);

        /* CLD - Clear Decimal Mode
            0 -> D

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	0	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    CLD	        D8	1	    2
        */
        iTable[0xD8] = new Instruction((byte) 0xD8, CLD, Addressing.Mode.IMP,1,2,false,cpu::cld);

        /* CLI - Clear Interrupt Disable Bit
            0 -> I

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	0	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    CLI	        58	1	    2

        */
        iTable[0x18] = new Instruction((byte) 0x58, CLI, Addressing.Mode.IMP,1,2,false,cpu::cli);

        /* CLV - Clear Overflow Flag
            0 -> V

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	0

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    CLV	        B8	1	    2
        */
        iTable[0xB8] = new Instruction((byte) 0xB8, CLV, Addressing.Mode.IMP,1,2,false,cpu::clv);

        /*  CMP - Compare Memory with Accumulator
            A - M

            Affected rows:
            N	Z	C	I	D	V
            +	+	+	-	-	-

            addressing	    assembler	    opc	bytes	cycles
            --------------------------------------------------
            immediate	    CMP #oper	    C9	2	    2
            zeropage	    CMP oper	    C5	2	    3
            zeropage,X	    CMP oper,X	    D5	2	    4
            absolute	    CMP oper	    CD	3	    4
            absolute,X	    CMP oper,X	    DD	3	    4*
            absolute,Y	    CMP oper,Y	    D9	3	    4*
            (indirect,X)	CMP (oper,X)	C1	2	    6
            (indirect),Y	CMP (oper),Y	D1	2	    5*
        */
        iTable[0xC9] = new Instruction((byte) 0xC9, CMP, Addressing.Mode.IMM,2,2,false,cpu::compare);
        iTable[0xC5] = new Instruction((byte) 0xC5, CMP, Addressing.Mode.ZRP,2,3,false,cpu::compare);
        iTable[0xD5] = new Instruction((byte) 0xD5, CMP, Addressing.Mode.ZPX,2,4,false,cpu::compare);
        iTable[0xCD] = new Instruction((byte) 0xCD, CMP, Addressing.Mode.ABS,3,4,false,cpu::compare);
        iTable[0xDD] = new Instruction((byte) 0xDD, CMP, Addressing.Mode.ABX,3,4,true,cpu::compare);
        iTable[0xD9] = new Instruction((byte) 0xD9, CMP, Addressing.Mode.ABY,3,4,true,cpu::compare);
        iTable[0xC1] = new Instruction((byte) 0xC1, CMP, Addressing.Mode.INX,2,6,false,cpu::compare);
        iTable[0xD1] = new Instruction((byte) 0xD1, CMP, Addressing.Mode.INY,2,5,true,cpu::compare);

        /*  CPX - Compare Memory and Index X
            X - M

            Affected flags:
            N	Z	C	I	D	V
            +	+	+	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            immediate	CPX #oper	E0	2	    2
            zeropage	CPX oper	E4	2	    3
            absolute	CPX oper	EC	3	    4
        */
        iTable[0xE0] = new Instruction((byte) 0xE0, CPX, Addressing.Mode.IMM,2,2,false,cpu::compare);
        iTable[0xE4] = new Instruction((byte) 0xE4, CPX, Addressing.Mode.ZRP,2,3,false,cpu::compare);
        iTable[0xEC] = new Instruction((byte) 0xEC, CPX, Addressing.Mode.ABS,3,4,false,cpu::compare);

        /*  CPY - Compare Memory and Index Y
            Y - M

            Affected flags:
            N	Z	C	I	D	V
            +	+	+	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            immediate	CPY #oper	C0	2	    2
            zeropage	CPY oper	C4	2	    3
            absolute	CPY oper	CC	3	    4
        */
        iTable[0xC0] = new Instruction((byte) 0xC0, CPY, Addressing.Mode.IMM,2,2,false,cpu::compare);
        iTable[0xC4] = new Instruction((byte) 0xC4, CPY, Addressing.Mode.ZRP,2,3,false,cpu::compare);
        iTable[0xCC] = new Instruction((byte) 0xCC, CPY, Addressing.Mode.ABS,3,4,false,cpu::compare);

        /*  DEC - Decrement Memory by One
            M - 1 -> M

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            zeropage	DEC oper	C6	2	    5
            zeropage,X	DEC oper,X	D6	2	    6
            absolute	DEC oper	CE	3	    6
            absolute,X	DEC oper,X	DE	3	    7
        */
        iTable[0xC6] = new Instruction((byte) 0xC6, DEC, Addressing.Mode.ZRP,2,5,false,cpu::dec);
        iTable[0xD6] = new Instruction((byte) 0xD6, DEC, Addressing.Mode.ZPX,2,6,false,cpu::dec);
        iTable[0xCE] = new Instruction((byte) 0xCE, DEC, Addressing.Mode.ABS,3,6,false,cpu::dec);
        iTable[0xDE] = new Instruction((byte) 0xDE, DEC, Addressing.Mode.ABX,3,7,false,cpu::dec);

        /*  DEX - Decrement Index X by One
            X - 1 -> X

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    DEX	        CA	1	    2
        */
        iTable[0xCA] = new Instruction((byte) 0xCA, DEX, Addressing.Mode.IMP,1,2,false,cpu::dex);

        /*  DEY - Decrement Index Y by One
            Y - 1 -> Y

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    DEY	        88	1	    2
        */
        iTable[0x88] = new Instruction((byte) 0x88, DEY, Addressing.Mode.IMP,1,2,false,cpu::dey);

        /*  EOR - Exclusive-OR Memory with Accumulator
            A EOR M -> A

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	    assembler	    opc	bytes	cycles
            --------------------------------------------------
            immediate	    EOR #oper	    49	2	    2
            zeropage	    EOR oper	    45	2	    3
            zeropage,X	    EOR oper,X	    55	2	    4
            absolute	    EOR oper	    4D	3	    4
            absolute,X	    EOR oper,X	    5D	3	    4*
            absolute,Y	    EOR oper,Y	    59	3	    4*
            (indirect,X)	EOR (oper,X)	41	2	    6
            (indirect),Y	EOR (oper),Y	51	2	    5*
        */
        iTable[0x49] = new Instruction((byte) 0x49, EOR, Addressing.Mode.IMM,2,2,false, cpu::eor);
        iTable[0x45] = new Instruction((byte) 0x45, EOR, Addressing.Mode.ZRP,2,3,false, cpu::eor);
        iTable[0x55] = new Instruction((byte) 0x55, EOR, Addressing.Mode.ZPX,2,4,false, cpu::eor);
        iTable[0x4D] = new Instruction((byte) 0x4D, EOR, Addressing.Mode.ABS,3,4,false, cpu::eor);
        iTable[0x5D] = new Instruction((byte) 0x5D, EOR, Addressing.Mode.ABX,3,4,true, cpu::eor);
        iTable[0x59] = new Instruction((byte) 0x59, EOR, Addressing.Mode.ABY,3,4,true, cpu::eor);
        iTable[0x41] = new Instruction((byte) 0x41, EOR, Addressing.Mode.INX,2,6,false, cpu::eor);
        iTable[0x51] = new Instruction((byte) 0x51, EOR, Addressing.Mode.INY,2,5,true, cpu::eor);

        /*  INC - Increment Memory by One
            M + 1 -> M

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            zeropage	INC oper	E6	2	    5
            zeropage,X	INC oper,X	F6	2	    6
            absolute	INC oper	EE	3	    6
            absolute,X	INC oper,X	FE	3	    7
        */
        iTable[0xE6] = new Instruction((byte) 0xE6, INC, Addressing.Mode.ZRP,2,5,false, cpu::inc);
        iTable[0xF6] = new Instruction((byte) 0xF6, INC, Addressing.Mode.ZPX,2,6,false, cpu::inc);
        iTable[0xEE] = new Instruction((byte) 0xEE, INC, Addressing.Mode.ABS,3,6,false, cpu::inc);
        iTable[0xFE] = new Instruction((byte) 0xFE, INC, Addressing.Mode.ABX,3,7,false, cpu::inc);

        /*  INX - Increment Index X by One
            X + 1 -> X

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    INX	        E8	1	    2
        */
        iTable[0xE8] = new Instruction((byte) 0xE8, INX, Addressing.Mode.IMP,1,2,false,cpu::inx);

        /*  INY - Increment Index Y by One
            Y + 1 -> Y

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    INY	        C8	1	    2
        */
        iTable[0xC8] = new Instruction((byte) 0xC8, INY, Addressing.Mode.IMP,1,2,false,cpu::iny);

        /*  JMP - Jump to New Location
            (PC+1) -> PCL
            (PC+2) -> PCH

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            absolute	JMP oper	4C	3	    3
            indirect	JMP (oper)	6C	3	    5

        */
        iTable[0x4C] = new Instruction((byte) 0x4C, JMP, Addressing.Mode.ABS,3,3,false,cpu::jmp);
        iTable[0x6C] = new Instruction((byte) 0x6C, JMP, Addressing.Mode.IND,3,5,false,cpu::jmp);

        /*  JSR - Jump to New Location Saving Return Address
            push (PC+2),
            (PC+1) -> PCL
            (PC+2) -> PCH

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            absolute	JSR oper	20	3	    6
        */
        iTable[0x20] = new Instruction((byte) 0x20, JSR, Addressing.Mode.ABS,3,6,false,cpu::jsr);

        /*  LDA - Load Accumulator with Memory
            M -> A

            Affected flags:

            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	    assembler	    opc	bytes	cycles
            --------------------------------------------------
            immediate	    LDA #oper	    A9	2	    2
            zeropage	    LDA oper	    A5	2	    3
            zeropage,X	    LDA oper,X	    B5	2	    4
            absolute	    LDA oper	    AD	3	    4
            absolute,X	    LDA oper,X	    BD	3	    4*
            absolute,Y	    LDA oper,Y	    B9	3	    4*
            (indirect,X)	LDA (oper,X)	A1	2	    6
            (indirect),Y	LDA (oper),Y	B1	2	    5*
        */
        iTable[0xA9] = new Instruction((byte) 0xA9, LDA, Addressing.Mode.IMM,2,2,false, cpu::ld);
        iTable[0xA5] = new Instruction((byte) 0xA5, LDA, Addressing.Mode.ZRP,2,3,false, cpu::ld);
        iTable[0xB5] = new Instruction((byte) 0xB5, LDA, Addressing.Mode.ZPX,2,4,false, cpu::ld);
        iTable[0xAD] = new Instruction((byte) 0xAD, LDA, Addressing.Mode.ABS,3,4,false, cpu::ld);
        iTable[0xBD] = new Instruction((byte) 0xBD, LDA, Addressing.Mode.ABX,3,4,true, cpu::ld);
        iTable[0xB9] = new Instruction((byte) 0xB9, LDA, Addressing.Mode.ABY,3,4,true, cpu::ld);
        iTable[0xA1] = new Instruction((byte) 0xA1, LDA, Addressing.Mode.INX,2,6,false, cpu::ld);
        iTable[0xB1] = new Instruction((byte) 0xB1, LDA, Addressing.Mode.INY,2,5,true, cpu::ld);

        /*  LDX - Load Index X with Memory
            M -> X

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            immediate	LDX #oper	A2	2	    2
            zeropage	LDX oper	A6	2	    3
            zeropage,Y	LDX oper,Y	B6	2	    4
            absolute	LDX oper	AE	3	    4
            absolute,Y	LDX oper,Y	BE	3	    4*
        */
        iTable[0xA2] = new Instruction((byte) 0xA2, LDX, Addressing.Mode.IMM,2,2,false, cpu::ld);
        iTable[0xA6] = new Instruction((byte) 0xA6, LDX, Addressing.Mode.ZRP,2,3,false, cpu::ld);
        iTable[0xB6] = new Instruction((byte) 0xB6, LDX, Addressing.Mode.ZPX,2,4,false, cpu::ld);
        iTable[0xAE] = new Instruction((byte) 0xAE, LDX, Addressing.Mode.ABS,3,4,false, cpu::ld);
        iTable[0xBE] = new Instruction((byte) 0xBE, LDX, Addressing.Mode.ABX,3,4,true, cpu::ld);

        /*  LDY - Load Index Y with Memory
            M -> Y

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            immediate	LDY #oper	A0	2	    2
            zeropage	LDY oper	A4	2	    3
            zeropage,X	LDY oper,X	B4	2	    4
            absolute	LDY oper	AC	3	    4
            absolute,X	LDY oper,X	BC	3	    4*
        */
        iTable[0xA0] = new Instruction((byte) 0xA0, LDY, Addressing.Mode.IMM,2,2,false, cpu::ld);
        iTable[0xA4] = new Instruction((byte) 0xA4, LDY, Addressing.Mode.ZRP,2,3,false, cpu::ld);
        iTable[0xB4] = new Instruction((byte) 0xB4, LDY, Addressing.Mode.ZPX,2,4,false, cpu::ld);
        iTable[0xAC] = new Instruction((byte) 0xAC, LDY, Addressing.Mode.ABS,3,4,false, cpu::ld);
        iTable[0xBC] = new Instruction((byte) 0xBC, LDY, Addressing.Mode.ABX,3,4,true, cpu::ld);

        /*  LSR - Shift One Bit Right (Memory or Accumulator)
            0 -> [76543210] -> C

            Affected flags:
            N	Z	C	I	D	V
            0	+	+	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            accumulator	LSR A	    4A	1	    2
            zeropage	LSR oper	46	2	    5
            zeropage,X	LSR oper,X	56	2	    6
            absolute	LSR oper	4E	3	    6
            absolute,X	LSR oper,X	5E	3	    7
        */
        iTable[0x4A] = new Instruction((byte) 0x4A, LSR, Addressing.Mode.ACC,1,2,false, cpu::lsr);
        iTable[0x46] = new Instruction((byte) 0x46, LSR, Addressing.Mode.ZRP,2,5,false, cpu::lsr);
        iTable[0x56] = new Instruction((byte) 0x56, LSR, Addressing.Mode.ZPX,2,6,false, cpu::lsr);
        iTable[0x4E] = new Instruction((byte) 0x4E, LSR, Addressing.Mode.ABS,3,6,false, cpu::lsr);
        iTable[0x5E] = new Instruction((byte) 0x5E, LSR, Addressing.Mode.ABX,3,7,true, cpu::lsr);

        /*  NOP - No Operation
            ---

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    NOP	        EA	1	    2

        */
        iTable[0xEA] = new Instruction((byte) 0xEA, NOP, Addressing.Mode.IMP,1,2,false, cpu::nop);

        /*  ORA - OR Memory with Accumulator
            A OR M -> A

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	        opc	bytes   cycles
            --------------------------------------------------
            immediate	    ORA #oper	    09	2	    2
            zeropage	    ORA oper	    05	2	    3
            zeropage,X	    ORA oper,X	    15	2	    4
            absolute	    ORA oper	    0D	3	    4
            absolute,X	    ORA oper,X	    1D	3	    4*
            absolute,Y	    ORA oper,Y	    19	3	    4*
            (indirect,X)	ORA (oper,X)	01	2	    6
            (indirect),Y	ORA (oper),Y	11	2	    5*
         */
        iTable[0x09] = new Instruction((byte) 0x09, ORA, Addressing.Mode.IMM,2,2,false, cpu::ora);
        iTable[0x05] = new Instruction((byte) 0x05, ORA, Addressing.Mode.ZRP,2,3,false, cpu::ora);
        iTable[0x15] = new Instruction((byte) 0x15, ORA, Addressing.Mode.ZPX,2,4,false, cpu::ora);
        iTable[0x0D] = new Instruction((byte) 0x0D, ORA, Addressing.Mode.ABS,3,4,false, cpu::ora);
        iTable[0x1D] = new Instruction((byte) 0x1D, ORA, Addressing.Mode.ABX,3,4,true, cpu::ora);
        iTable[0x19] = new Instruction((byte) 0x19, ORA, Addressing.Mode.ABY,3,4,true, cpu::ora);
        iTable[0x01] = new Instruction((byte) 0x01, ORA, Addressing.Mode.INX,2,6,false, cpu::ora);
        iTable[0x11] = new Instruction((byte) 0x11, ORA, Addressing.Mode.INY,2,5,true, cpu::ora);


        /*  PHA - Push Accumulator on Stack
            push A

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    PHA	        48	1	    3
        */
            iTable[0x48] = new Instruction((byte) 0x48, PHA, Addressing.Mode.IMP,1,3,false, cpu::pha);

        /*  PHP - Push Processor Status on Stack
            The status register will be pushed with the break
            flag and bit 5 set to 1.

            push SR

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    PHP	        08	1	    3
        */
        iTable[0x08] = new Instruction((byte) 0x08, PHP, Addressing.Mode.IMP,1,3,false, cpu::php);

        /*  PLA - Pull Accumulator from Stack
            pull A

            Affected flags:
            N	Z	C	I	D	V
            +	+	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    PLA	        68	1	    4
        */
        iTable[0x68] = new Instruction((byte) 0x68, PLA, Addressing.Mode.IMP,1,4,false, cpu::pla);

        /*  PLP - Pull Processor Status from Stack
            The status register will be pulled with the break
            flag and bit 5 ignored.

            pull SR

            Affected flags:
            N	Z	C	I	D	V
            from stack

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    PLP	        28	1	    4
        */
        iTable[0x28] = new Instruction((byte) 0x28, PLP, Addressing.Mode.IMP,1,4,false, cpu::plp);


        /*  ROL - Rotate One Bit Left (Memory or Accumulator)
            C <- [76543210] <- C

            N	Z	C	I	D	V
            +	+	+	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            accumulator	ROL A	    2A	1	    2
            zeropage	ROL oper	26	2	    5
            zeropage,X	ROL oper,X	36	2	    6
            absolute	ROL oper	2E	3	    6
            absolute,X	ROL oper,X	3E	3	    7
        */
        iTable[0x2A] = new Instruction((byte) 0x2A, ROL, Addressing.Mode.ACC,1,2,false, cpu::rol);
        iTable[0x26] = new Instruction((byte) 0x26, ROL, Addressing.Mode.ZRP,2,5,false, cpu::rol);
        iTable[0x36] = new Instruction((byte) 0x36, ROL, Addressing.Mode.ZPX,2,6,false, cpu::rol);
        iTable[0x2E] = new Instruction((byte) 0x2E, ROL, Addressing.Mode.ABS,3,6,false, cpu::rol);
        iTable[0x3E] = new Instruction((byte) 0x3E, ROL, Addressing.Mode.ABX,3,7,false, cpu::rol);


        /*  ROR - Rotate One Bit Right (Memory or Accumulator)
            C -> [76543210] -> C

            Affected flags:

            N	Z	C	I	D	V
            +	+	+	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            accumulator	ROR A	    6A	1	    2
            zeropage	ROR oper	66	2	    5
            zeropage,X	ROR oper,X	76	2	    6
            absolute	ROR oper	6E	3	    6
            absolute,X	ROR oper,X	7E	3	    7
        */
        iTable[0x6A] = new Instruction((byte) 0x6A, ROR, Addressing.Mode.ACC,1,2,false, cpu::ror);
        iTable[0x66] = new Instruction((byte) 0x66, ROR, Addressing.Mode.ZRP,2,5,false, cpu::ror);
        iTable[0x76] = new Instruction((byte) 0x76, ROR, Addressing.Mode.ZPX,2,6,false, cpu::ror);
        iTable[0x6E] = new Instruction((byte) 0x6E, ROR, Addressing.Mode.ABS,3,6,false, cpu::ror);
        iTable[0x7E] = new Instruction((byte) 0x7E, ROR, Addressing.Mode.ABX,3,7,false, cpu::ror);

        /*  RTI - Return from Interrupt
            The status register is pulled with the break flag
            and bit 5 ignored. Then PC is pulled from the stack.

            pull SR, pull PC

            Affected flags:
            N	Z	C	I	D	V
            from stack

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    RTI	        40	1	    6
        */
        iTable[0x40] = new Instruction((byte) 0x00, RTI, Addressing.Mode.IMM,1,6,false,cpu::rti);

        /*  RTS - Return from Subroutine
            pull PC, PC+1 -> PC

            Affected flags:
            N	Z	C	I	D	V
            -	-	-	-	-	-

            addressing	assembler	opc	bytes	cycles
            ------------------------------------------
            implied	    RTS	        60	1	    6
         */
        iTable[0x60] = new Instruction((byte) 0x00, RTS, Addressing.Mode.IMM,1,6,false,cpu::rts);



    }

    private static void initTable(Instruction[] iTable, Cpu65xxCore cpu){
        for (int i = 0; i < iTable.length; i++) {
            iTable[i] = new Instruction((byte) i, XXX, Addressing.Mode.IMP,0,0,true,cpu::unsupportedOperation);
        }
    }

}
