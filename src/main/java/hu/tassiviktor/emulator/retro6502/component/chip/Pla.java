package hu.tassiviktor.emulator.retro6502.component.chip;

import hu.tassiviktor.emulator.retro6502.component.template.Component;
import hu.tassiviktor.emulator.retro6502.component.wireing.Bus;
import hu.tassiviktor.emulator.retro6502.component.wireing.Line;

import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.HIGH;
import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.LOW;

public class Pla implements Component {

    protected Bus addressBus;
    protected Bus dataBus;

    protected Line rw;

    protected Line clk2;

    protected Line p01; // /CS - RAM
    protected Line p02; // /OE - kernal ROM - High
    protected Line p03; // /OE - DOS ROM
    protected Line p04; // /OE - Char ROM
    protected Line p05; // /CS - Serio - 1
    protected Line p06; // /CS - Serio - 2
    protected Line p07; // /CS - Parallel IO - 1
    protected Line p08; // /CS - Parallel IO - 2
    protected Line p09; // /CS - INT Controller

    private long register = 0b111111111111;

    /**
     * Simplified implementation
     * Bits of address 0 sets the chips
     */
    public Pla() {

    }

    @Override
    public void install() {
        clk2.setLineListener(HIGH, this::operate);
        clk2.setLineListener(HIGH, this::clearLines);
    }

    private void clearLines() {
        p01.setState(HIGH);
        p02.setState(HIGH);
        p03.setState(HIGH);
        p04.setState(HIGH);
        p05.setState(HIGH);
        p06.setState(HIGH);
        p07.setState(HIGH);
        p08.setState(HIGH);
        p09.setState(HIGH);
    }

    @Override
    public void powerUp() {
        p01.setState(HIGH);
        p02.setState(HIGH);
        p03.setState(HIGH);
        p04.setState(HIGH);
        p05.setState(HIGH);
        p06.setState(HIGH);
        p07.setState(HIGH);
        p08.setState(HIGH);
        p09.setState(HIGH);
    }

    public void operate(){
        p01.setState(LOW);
    }

    public Bus getAddressBus() {
        return addressBus;
    }

    public void setAddressBus(Bus addressBus) {
        this.addressBus = addressBus;
    }

    public Bus getDataBus() {
        return dataBus;
    }

    public void setDataBus(Bus dataBus) {
        this.dataBus = dataBus;
    }

    public Line getRw() {
        return rw;
    }

    public void setRw(Line rw) {
        this.rw = rw;
    }

    public Line getP01() {
        return p01;
    }

    public void setP01(Line p01) {
        this.p01 = p01;
    }

    public Line getP02() {
        return p02;
    }

    public void setP02(Line p02) {
        this.p02 = p02;
    }

    public Line getP03() {
        return p03;
    }

    public void setP03(Line p03) {
        this.p03 = p03;
    }

    public Line getP04() {
        return p04;
    }

    public void setP04(Line p04) {
        this.p04 = p04;
    }

    public Line getP05() {
        return p05;
    }

    public void setP05(Line p05) {
        this.p05 = p05;
    }

    public Line getP06() {
        return p06;
    }

    public void setP06(Line p06) {
        this.p06 = p06;
    }

    public Line getP07() {
        return p07;
    }

    public void setP07(Line p07) {
        this.p07 = p07;
    }

    public Line getP08() {
        return p08;
    }

    public void setP08(Line p08) {
        this.p08 = p08;
    }

    public Line getP09() {
        return p09;
    }

    public void setP09(Line p09) {
        this.p09 = p09;
    }

    public Line getClk2() {
        return clk2;
    }

    public void setClk2(Line clk2) {
        this.clk2 = clk2;
    }
}
