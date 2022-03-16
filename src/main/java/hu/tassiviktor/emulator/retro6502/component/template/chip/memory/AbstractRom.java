package hu.tassiviktor.emulator.retro6502.component.template.chip.memory;

import hu.tassiviktor.emulator.retro6502.component.wireing.Line;

import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.LOW;

public class AbstractRom  extends AbstractMemory{

    public Line oe = new Line();  // /OE - output enable

    public AbstractRom(int addressLines, int dataWidth, String imageFile) {
        super(addressLines, dataWidth);
        loadFromFile(imageFile);
    }

    @Override
    public void install() {
        oe.setLineListener(LOW, this::operate);
    }

    @Override
    public void powerUp() {
        //Nothing to do
    }

    private void operate() {
        int address = (int) addressBus.readAsNumber() & 2^getAddressLines();
        dataBus.writeAsNumber(read(address));
    }

    @Override
    protected void write(int address, long data) {
        //Do nothing.
    }

    public Line getOe() {
        return oe;
    }

    public void setOe(Line oe) {
        this.oe = oe;
    }
}
