package hu.tassiviktor.emulator.retro6502;

import hu.tassiviktor.emulator.retro6502.component.chip.*;
import hu.tassiviktor.emulator.retro6502.component.wireing.Board;
import hu.tassiviktor.emulator.retro6502.component.wireing.Bus;
import hu.tassiviktor.emulator.retro6502.component.wireing.Line;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Emulator {

    private static Logger logger;

    static {
        InputStream stream = Emulator.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            logger = Logger.getLogger(Emulator.class.getSimpleName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        logger.info("Initializing emulator engine.");

        logger.info("Creating components.");

        Board mainBoard = new Board()
                .addBus("address", new Bus(16))
                .addBus("data", new Bus(8))

                .addLine("RW", new Line())
                .addLine("IRQ", new Line())
                .addLine("NMI", new Line())
                .addLine("RESET", new Line())

                .addLine("PLA_TO_RAM_CS", new Line())
                .addLine("PLA_TO_KERNAL_OE", new Line())
                .addLine("PLA_TO_DOS_OE", new Line())
                .addLine("PLA_TO_CHAR_OE", new Line())

                .addLine("PLA_TO_SER_IO1", new Line())
                .addLine("PLA_TO_SER_IO2", new Line())
                .addLine("PLA_TO_PAR_IO1", new Line())
                .addLine("PLA_TO_PAR_IO2", new Line())
                .addLine("PLA_TO_INT_CTRL", new Line())
                .addLine("CPU_CLK2_TO_PLA", new Line())
                ;

        StaticRam64Kx8 sRam = new StaticRam64Kx8();
        sRam.setAddressBus(mainBoard.getBus("address"));
        sRam.setDataBus(mainBoard.getBus("data"));
        sRam.setRwLine(mainBoard.getLine("RW"));
        sRam.setCsLine(mainBoard.getLine("PLA_TO_RAM_CS"));

        Rom8kx8 kernal = new Rom8kx8("fakeKernal.rom");
        kernal.setAddressBus(mainBoard.getBus("address"));
        kernal.setDataBus(mainBoard.getBus("data"));
        kernal.setOe(mainBoard.getLine("PLA_TO_KERNAL_OE"));

        Rom8kx8 dos = new Rom8kx8("fakeDos.rom");
        dos.setAddressBus(mainBoard.getBus("address"));
        dos.setDataBus(mainBoard.getBus("data"));
        dos.setOe(mainBoard.getLine("PLA_TO_DOS_OE"));

        Rom2kx8 charSet = new Rom2kx8("cbm-hungarian.bin");
        charSet.setAddressBus(mainBoard.getBus("address"));
        charSet.setDataBus(mainBoard.getBus("data"));
        charSet.setOe(mainBoard.getLine("PLA_TO_CHAR_OE"));

        Pla pla = new Pla();
        pla.setRw(mainBoard.getLine("RW"));
        pla.setAddressBus(mainBoard.getBus("address"));
        pla.setDataBus(mainBoard.getBus("data"));
        pla.setClk2((mainBoard.getLine("CPU_CLK2_TO_PLA")));
        //
        pla.setP01(mainBoard.getLine("PLA_TO_RAM_CS"));
        pla.setP02(mainBoard.getLine("PLA_TO_KERNAL_OE"));
        pla.setP03(mainBoard.getLine("PLA_TO_DOS_OE"));
        pla.setP04(mainBoard.getLine("PLA_TO_CHAR_OE"));

        pla.setP05(mainBoard.getLine("PLA_TO_SER_IO1"));
        pla.setP06(mainBoard.getLine("PLA_TO_SER_IO2"));
        pla.setP07(mainBoard.getLine("PLA_TO_PAR_IO1"));
        pla.setP08(mainBoard.getLine("PLA_TO_PAR_IO2"));
        pla.setP09(mainBoard.getLine("PLA_TO_INT_CTRL"));

        //CPU
        Cpu6502 cpu6502 = new Cpu6502();
        cpu6502.setAddressBus(mainBoard.getBus("address"));
        cpu6502.setDataBus(mainBoard.getBus("data"));
        cpu6502.setRw(mainBoard.getLine("RW"));
        cpu6502.setIrq(mainBoard.getLine("IRQ"));
        cpu6502.setNmi(mainBoard.getLine("NMI"));
        cpu6502.setReset(mainBoard.getLine("RESET"));

        cpu6502.setClk2(mainBoard.getLine("CPU_CLK2_TO_PLA"));

        //
        mainBoard.addDevice(sRam);
        mainBoard.addDevice(kernal);
        mainBoard.addDevice(dos);
        mainBoard.addDevice(charSet);
        mainBoard.addDevice(pla);
        mainBoard.addDevice(cpu6502);

        //
        mainBoard.installComponents();
        mainBoard.powerUp();

        cpu6502.run();
    }
}
