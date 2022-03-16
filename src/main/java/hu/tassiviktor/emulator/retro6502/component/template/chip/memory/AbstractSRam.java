package hu.tassiviktor.emulator.retro6502.component.template.chip.memory;

import hu.tassiviktor.emulator.retro6502.common.Power2;
import hu.tassiviktor.emulator.retro6502.component.wireing.Line;

import java.security.SecureRandom;

import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.LOW;

public class AbstractSRam extends AbstractMemory {

    protected Line rwLine;  // R/W
    protected Line csLine;  // /CS

    public AbstractSRam(int addressLines, int dataWidth) {
        super(addressLines, dataWidth);
    }

    @Override
    public void powerUp() {
        super.powerUp();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < storage.length; i++) {
            // storage[i] = random.nextLong() & (Power2.of(dataWidth)-1);
            storage[i] = 0;
        }
    }

    @Override
    public void install() {
        logger.info("Installing /CS line listener.");
        csLine.setLineListener(LOW, this::operate);
    }

    public void operate() {
        int address = (int) addressBus.readAsNumber();
        if (rwLine.isHigh()) { // read
            dataBus.writeAsNumber(read(address));
        } else {  //write
            write(address, dataBus.readAsNumber());
        }
    }

    public Line getRwLine() {
        return rwLine;
    }

    public void setRwLine(Line rwLine) {
        this.rwLine = rwLine;
    }

    public Line getCsLine() {
        return csLine;
    }

    public void setCsLine(Line csLine) {
        this.csLine = csLine;
    }
}
